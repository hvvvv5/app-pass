package com.passgo.app.feature.autofill.parser

import android.app.assist.AssistStructure
import android.os.Build
import android.service.autofill.FillRequest
import android.view.View
import android.view.autofill.AutofillId
import android.view.inputmethod.EditorInfo
import com.passgo.app.feature.autofill.domain.DomainHandler
import com.passgo.app.feature.autofill.matcher.FieldMatcher
import com.passgo.app.feature.autofill.model.AutofillField
import com.passgo.app.feature.autofill.model.AutofillRequest
import com.passgo.app.feature.autofill.model.FieldType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RequestParser @Inject constructor(
    private val fieldMatcher: FieldMatcher,
    private val domainHandler: DomainHandler
) {

    fun parse(request: FillRequest): AutofillRequest {
        val structure = request.fillContexts
            .lastOrNull()
            ?.structure
            ?: return AutofillRequest(
                requestId = request.id,
                packageName = "",
                domain = null,
                focusedField = null,
                detectedFields = emptyList()
            )

        val packageName = structure.activityComponent?.packageName ?: ""
        val domain = extractDomain(structure, packageName)

        val detectedFields = mutableListOf<AutofillField>()
        val focusedIds = getFocusedIds(request)

        for (i in 0 until structure.windowNodeCount) {
            val windowNode = structure.getWindowNodeAt(i)
            traverseNode(windowNode.rootViewNode) { node ->
                val autofillId = node.autofillId ?: return@traverseNode
                if (!isFillable(node)) return@traverseNode
                if (!isTextField(node)) return@traverseNode

                val field = buildField(node, autofillId)
                detectedFields.add(field)
            }
        }

        val focusedField = detectedFields.firstOrNull { it.autofillId in focusedIds }
        val focusedFieldByType = focusedField ?: detectedFields.firstOrNull { it.fieldType == FieldType.PASSWORD }

        return AutofillRequest(
            requestId = request.id,
            packageName = packageName,
            domain = domain,
            focusedField = focusedFieldByType,
            detectedFields = detectedFields
        )
    }

    private fun getFocusedIds(request: FillRequest): Set<AutofillId> {
        return emptySet()
    }

    private fun extractDomain(structure: AssistStructure, packageName: String): String? {
        val fromPackage = domainHandler.extractDomainFromPackageName(packageName)

        for (i in 0 until structure.windowNodeCount) {
            val windowNode = structure.getWindowNodeAt(i)
            val title = windowNode.title?.toString() ?: continue
            domainHandler.extractDomain(title)?.let { return it }
        }

        return fromPackage ?: packageName
    }

    private fun traverseNode(
        node: AssistStructure.ViewNode,
        onNode: (AssistStructure.ViewNode) -> Unit
    ) {
        onNode(node)
        for (i in 0 until node.childCount) {
            traverseNode(node.getChildAt(i), onNode)
        }
    }

    private fun isFillable(node: AssistStructure.ViewNode): Boolean {
        if (node.autofillId == null) return false
        if (node.autofillType == View.AUTOFILL_TYPE_NONE) return false
        if (!node.isEnabled) return false
        if (node.visibility != View.VISIBLE) return false
        return true
    }

    private fun isTextField(node: AssistStructure.ViewNode): Boolean {
        val className = node.className?.lowercase() ?: return false
        if ("edittext" !in className && "textview" !in className) return false
        val inputType = node.inputType and EditorInfo.TYPE_MASK_CLASS
        return inputType == EditorInfo.TYPE_CLASS_TEXT || inputType == EditorInfo.TYPE_CLASS_NUMBER
    }

    private fun buildField(node: AssistStructure.ViewNode, autofillId: AutofillId): AutofillField {
        return fieldMatcher.classifyField(
            autofillId = autofillId,
            hints = node.autofillHints,
            inputType = node.inputType,
            text = node.text?.toString() ?: "",
            htmlAutofillType = node.autofillType
        )
    }
}
