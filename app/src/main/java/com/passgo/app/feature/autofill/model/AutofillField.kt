package com.passgo.app.feature.autofill.model

import android.view.autofill.AutofillId

enum class FieldType {
    USERNAME,
    EMAIL,
    PASSWORD,
    UNKNOWN
}

data class AutofillField(
    val autofillId: AutofillId,
    val fieldType: FieldType,
    val hints: List<String>,
    val inputType: Int,
    val isRequired: Boolean,
    val htmlAutofillType: Int
)
