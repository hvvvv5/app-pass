package com.passgo.app.feature.autofill.response

import android.service.autofill.FillResponse
import android.service.autofill.SaveInfo
import com.passgo.app.feature.autofill.dataset.DatasetBuilder
import com.passgo.app.feature.autofill.model.AutofillCredential
import com.passgo.app.feature.autofill.model.AutofillField
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResponseBuilder @Inject constructor(
    private val datasetBuilder: DatasetBuilder
) {

    fun buildResponse(
        credentials: List<AutofillCredential>,
        usernameField: AutofillField?,
        emailField: AutofillField?,
        passwordField: AutofillField?
    ): FillResponse {
        val responseBuilder = FillResponse.Builder()

        credentials.forEach { credential ->
            datasetBuilder.buildFillDataset(credential, usernameField, emailField, passwordField)
                ?.let { responseBuilder.addDataset(it) }
        }

        val saveInfo = datasetBuilder.buildSaveDataset(
            usernameField = usernameField,
            emailField = emailField,
            passwordField = passwordField
        )
        responseBuilder.setSaveInfo(saveInfo)

        return responseBuilder.build()
    }

    fun buildEmptyResponse(): FillResponse {
        return FillResponse.Builder().build()
    }

    fun buildErrorResponse(): FillResponse {
        return FillResponse.Builder().build()
    }
}
