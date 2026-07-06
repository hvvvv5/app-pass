package com.passgo.app.feature.autofill.session

import android.service.autofill.FillCallback
import android.service.autofill.FillRequest
import android.service.autofill.FillResponse
import android.service.autofill.SaveCallback
import android.service.autofill.SaveRequest
import com.passgo.app.core.logging.PassGoLogger
import com.passgo.app.feature.autofill.domain.DomainHandler
import com.passgo.app.feature.autofill.matcher.FieldMatcher
import com.passgo.app.feature.autofill.model.AutofillRequest
import com.passgo.app.feature.autofill.model.SessionState
import com.passgo.app.feature.autofill.parser.RequestParser
import com.passgo.app.feature.autofill.response.ResponseBuilder
import javax.inject.Inject

class AutofillSession @Inject constructor(
    private val requestParser: RequestParser,
    private val responseBuilder: ResponseBuilder,
    private val domainHandler: DomainHandler,
    private val fieldMatcher: FieldMatcher,
    private val logger: PassGoLogger
) {

    private var state: SessionState = SessionState.CREATED
        private set

    private var currentRequest: AutofillRequest? = null
        private set

    private var parsedPackageName: String = ""
        private set

    fun onFillRequest(request: FillRequest, callback: FillCallback) {
        logger.info("AutofillSession", "Session started for request: ${request.id}")
        state = SessionState.PARSING

        val parsedRequest = requestParser.parse(request)
        currentRequest = parsedRequest
        parsedPackageName = parsedRequest.packageName

        if (parsedRequest.detectedFields.isEmpty()) {
            logger.info("AutofillSession", "No autofillable fields detected")
            state = SessionState.FINISHED
            callback.onSuccess(responseBuilder.buildEmptyResponse())
            return
        }

        if (!fieldMatcher.hasLoginFields(parsedRequest.detectedFields)) {
            logger.info("AutofillSession", "Unsupported screen — no login fields detected")
            state = SessionState.FINISHED
            callback.onSuccess(responseBuilder.buildEmptyResponse())
            return
        }

        state = SessionState.PARSED

        state = SessionState.RESPONDING
        val usernameField = parsedRequest.detectedFields.firstOrNull { it.fieldType == com.passgo.app.feature.autofill.model.FieldType.USERNAME }
        val emailField = parsedRequest.detectedFields.firstOrNull { it.fieldType == com.passgo.app.feature.autofill.model.FieldType.EMAIL }
        val passwordField = parsedRequest.detectedFields.firstOrNull { it.fieldType == com.passgo.app.feature.autofill.model.FieldType.PASSWORD }

        logger.info("AutofillSession", "Matching attempt for package: ${parsedRequest.packageName}")

        val fillResponse = responseBuilder.buildResponse(
            credentials = emptyList(),
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
        logger.info("AutofillSession", "Save session started")
        callback.onSuccess()
        logger.info("AutofillSession", "Save session finished")
    }

    fun cancel() {
        state = SessionState.CANCELLED
        logger.info("AutofillSession", "Session cancelled")
    }

    fun getState(): SessionState = state
}
