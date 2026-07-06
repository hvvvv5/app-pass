package com.passgo.app.feature.autofill.model

data class AutofillRequest(
    val requestId: Int,
    val packageName: String,
    val domain: String?,
    val focusedField: AutofillField?,
    val detectedFields: List<AutofillField>
)
