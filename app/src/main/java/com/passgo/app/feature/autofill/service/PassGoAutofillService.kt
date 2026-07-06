package com.passgo.app.feature.autofill.service

import android.os.CancellationSignal
import android.service.autofill.AutofillService
import android.service.autofill.FillCallback
import android.service.autofill.FillRequest
import android.service.autofill.SaveCallback
import android.service.autofill.SaveRequest
import com.passgo.app.core.logging.PassGoLogger
import com.passgo.app.feature.autofill.session.AutofillSession
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Provider

@AndroidEntryPoint
class PassGoAutofillService : AutofillService() {

    @Inject
    lateinit var sessionProvider: Provider<AutofillSession>

    @Inject
    lateinit var logger: PassGoLogger

    override fun onConnected() {
        super.onConnected()
        logger.info("PassGoAutofillService", "Autofill service connected")
    }

    override fun onDisconnected() {
        super.onDisconnected()
        logger.info("PassGoAutofillService", "Autofill service disconnected")
    }

    override fun onFillRequest(request: FillRequest, cancellationSignal: CancellationSignal, callback: FillCallback) {
        val session = sessionProvider.get()
        cancellationSignal.setOnCancelListener { session.cancel() }
        session.onFillRequest(request, callback)
    }

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        val session = sessionProvider.get()
        session.onSaveRequest(request, callback)
    }
}
