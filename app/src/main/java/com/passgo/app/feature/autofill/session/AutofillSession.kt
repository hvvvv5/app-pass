package com.passgo.app.feature.autofill.session

import android.app.assist.AssistStructure
import android.service.autofill.FillCallback
import android.service.autofill.FillRequest
import android.service.autofill.SaveCallback
import android.service.autofill.SaveRequest
import android.view.View
import com.passgo.app.core.logging.PassGoLogger
import com.passgo.app.feature.autofill.domain.DomainHandler
import com.passgo.app.feature.autofill.matcher.CredentialMatcher
import com.passgo.app.feature.autofill.matcher.FieldMatcher
import com.passgo.app.feature.autofill.model.FieldType
import com.passgo.app.feature.autofill.model.SessionState
import com.passgo.app.feature.autofill.parser.RequestParser
import com.passgo.app.feature.autofill.repository.AutofillRepository
import com.passgo.app.feature.autofill.response.ResponseBuilder
import javax.inject.Inject

class AutofillSession @Inject constructor(
    private val requestParser: RequestParser,
    private val responseBuilder: ResponseBuilder,
    private val domainHandler: DomainHandler,
    private val fieldMatcher: FieldMatcher,
    private val credentialMatcher: CredentialMatcher,
    private val autofillRepository: AutofillRepository,
    private val logger: PassGoLogger
) {
    private var state: SessionState = SessionState.CREATED
        private set

    private var currentPackageName: String = ""
        private set

    private var currentDomain: String? = null
        private set

    fun onFillRequest(request: FillRequest, callback: FillCallback) {
        logger.info("AutofillSession", "Matching started for request: ${request.id}")
        state = SessionState.PARSING

        val parsedRequest = requestParser.parse(request)
        currentPackageName = parsedRequest.packageName
        currentDomain = parsedRequest.domain

        if (parsedRequest.detectedFields.isEmpty()) {
            logger.info("AutofillSession", "No autofillable fields detected")
            completeWithEmpty(callback)
            return
        }

        if (!fieldMatcher.hasLoginFields(parsedRequest.detectedFields)) {
            logger.info("AutofillSession", "Unsupported screen — no login fields detected")
            completeWithEmpty(callback)
            return
        }

        state = SessionState.PARSED

        if (!autofillRepository.isVaultUnlocked()) {
            logger.info("AutofillSession", "Authentication required — vault locked")
            completeWithEmpty(callback)
            return
        }

        state = SessionState.RESPONDING

        val usernameField = parsedRequest.detectedFields.firstOrNull { it.fieldType == FieldType.USERNAME }
        val emailField = parsedRequest.detectedFields.firstOrNull { it.fieldType == FieldType.EMAIL }
        val passwordField = parsedRequest.detectedFields.firstOrNull { it.fieldType == FieldType.PASSWORD }

        logger.info("AutofillSession", "Matching attempt for package: ${parsedRequest.packageName}")

        val allCredentials = autofillRepository.getAllAvailableCredentials()

        val matchedCredentials = credentialMatcher.findMatchingCredentials(
            credentials = allCredentials,
            packageName = currentPackageName,
            domain = currentDomain
        )

        logger.info("AutofillSession", "Matching completed — ${matchedCredentials.size} datasets")

        val fillResponse = responseBuilder.buildResponse(
            credentials = matchedCredentials,
            usernameField = usernameField,
            emailField = emailField,
            passwordField = passwordField
        )

        state = SessionState.RESPONDED
        callback.onSuccess(fillResponse)
        state = SessionState.FINISHED
        logger.info("AutofillSession", "Session finished for request: ${request.id}")
    }

    fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        logger.info("AutofillSession", "SaveRequest received")

        val fillContexts = request.fillContexts
        if (fillContexts.isEmpty()) {
            callback.onSuccess()
            return
        }

        val structure = fillContexts.last().structure
        val requestPackageName = structure.activityComponent?.packageName ?: currentPackageName
        val requestDomain = extractDomainFromStructure(structure) ?: currentDomain

        if (!autofillRepository.isVaultUnlocked()) {
            logger.info("AutofillSession", "SaveRequest — vault locked, skipping save")
            callback.onSuccess()
            return
        }

        val usernameValue = extractFieldValue(structure, FieldType.USERNAME) ?: ""
        val emailValue = extractFieldValue(structure, FieldType.EMAIL) ?: ""
        val passwordValue = extractFieldValue(structure, FieldType.PASSWORD)

        if (passwordValue != null && passwordValue.isNotEmpty()) {
            autofillRepository.performSave(
                packageName = requestPackageName,
                domain = requestDomain,
                username = usernameValue,
                email = emailValue,
                password = passwordValue
            )
        } else {
            logger.info("AutofillSession", "SaveRequest — no password value found")
        }

        callback.onSuccess()
        logger.info("AutofillSession", "SaveRequest completed")
    }

    fun cancel() {
        if (state == SessionState.CANCELLED || state == SessionState.FINISHED) return
        state = SessionState.CANCELLED
        logger.info("AutofillSession", "Session cancelled")
    }

    fun getState(): SessionState = state

    private fun completeWithEmpty(callback: FillCallback) {
        state = SessionState.FINISHED
        callback.onSuccess(responseBuilder.buildEmptyResponse())
    }

    private fun extractFieldValue(structure: AssistStructure, fieldType: FieldType): String? {
        for (i in 0 until structure.windowNodeCount) {
            val windowNode = structure.getWindowNodeAt(i)
            val value = findFieldInNode(windowNode.rootViewNode, fieldType)
            if (value != null) return value
        }
        return null
    }

    private fun findFieldInNode(node: AssistStructure.ViewNode, fieldType: FieldType): String? {
        if (isMatchingField(node, fieldType)) {
            val value = node.autofillValue
            if (value != null && value.isText) {
                return value.textValue.toString()
            }
        }
        for (i in 0 until node.childCount) {
            val value = findFieldInNode(node.getChildAt(i), fieldType)
            if (value != null) return value
        }
        return null
    }

    private fun isMatchingField(node: AssistStructure.ViewNode, fieldType: FieldType): Boolean {
        val autofillId = node.autofillId ?: return false
        if (node.autofillType == View.AUTOFILL_TYPE_NONE) return false
        val field = fieldMatcher.classifyField(
            autofillId = autofillId,
            hints = node.autofillHints,
            inputType = node.inputType,
            text = node.text?.toString() ?: "",
            htmlAutofillType = node.autofillType
        )
        return field.fieldType == fieldType
    }

    private fun extractDomainFromStructure(structure: AssistStructure): String? {
        val packageName = structure.activityComponent?.packageName ?: return null
        val fromPackage = domainHandler.extractDomainFromPackageName(packageName)

        for (i in 0 until structure.windowNodeCount) {
            val windowNode = structure.getWindowNodeAt(i)
            val title = windowNode.title?.toString() ?: continue
            domainHandler.extractDomain(title)?.let { return it }
        }

        return fromPackage ?: packageName
    }
}
