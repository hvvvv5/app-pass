package com.passgo.app.feature.autofill.dataset

import android.os.Build
import android.service.autofill.Dataset
import android.service.autofill.SaveInfo
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import com.passgo.app.feature.autofill.model.AutofillCredential
import com.passgo.app.feature.autofill.model.AutofillField
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatasetBuilder @Inject constructor() {

    private val packageName = "com.passgo.app"

    @Suppress("DEPRECATION")
    fun buildFillDataset(
        credential: AutofillCredential,
        usernameField: AutofillField?,
        emailField: AutofillField?,
        passwordField: AutofillField?
    ): Dataset? {
        if (passwordField == null) return null

        val presentation = createPresentation(credential)

        val dataset = Dataset.Builder(presentation)

        usernameField?.let { field ->
            if (credential.username.isNotEmpty()) {
                dataset.setValue(field.autofillId, AutofillValue.forText(credential.username))
            }
        }

        emailField?.let { field ->
            if (credential.email.isNotEmpty()) {
                dataset.setValue(field.autofillId, AutofillValue.forText(credential.email))
            }
        }

        dataset.setValue(passwordField.autofillId, AutofillValue.forText(credential.password))

        return dataset.build()
    }

    fun buildSaveDataset(
        usernameField: AutofillField?,
        emailField: AutofillField?,
        passwordField: AutofillField?
    ): SaveInfo {
        val requiredIds = mutableListOf<AutofillId>()
        usernameField?.let { requiredIds.add(it.autofillId) }
        passwordField?.let { requiredIds.add(it.autofillId) }

        val optionalIds = mutableListOf<AutofillId>()
        emailField?.let { optionalIds.add(it.autofillId) }

        return SaveInfo.Builder(
            SaveInfo.SAVE_DATA_TYPE_PASSWORD or SaveInfo.SAVE_DATA_TYPE_USERNAME,
            requiredIds.toTypedArray()
        ).apply {
            if (optionalIds.isNotEmpty()) {
                setOptionalIds(optionalIds.toTypedArray())
            }
        }.build()
    }

    private fun createPresentation(credential: AutofillCredential): RemoteViews {
        val displayLabel = credential.name.ifEmpty { credential.username.ifEmpty { credential.email } }
        val displaySubtext = credential.username.ifEmpty { credential.email }.let {
            if (it.isNotEmpty() && it != displayLabel) " ($it)" else ""
        }
        val displayText = "$displayLabel$displaySubtext"

        val layoutId = android.R.layout.simple_list_item_1
        val presentation = RemoteViews(packageName, layoutId)
        presentation.setTextViewText(android.R.id.text1, displayText)
        return presentation
    }
}
