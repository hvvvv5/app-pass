package com.passgo.app.feature.autofill.matcher

import android.view.inputmethod.EditorInfo
import com.passgo.app.feature.autofill.model.AutofillField
import com.passgo.app.feature.autofill.model.FieldType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FieldMatcher @Inject constructor() {

    companion object {
        private val USERNAME_HINTS = setOf(
            "username", "user", "login", "signin", "sign_in",
            "sign-on", "nickname", "handle", "account"
        )
        private val EMAIL_HINTS = setOf(
            "email", "emailaddress", "email_address", "mail",
            "e-mail"
        )
        private val PASSWORD_HINTS = setOf(
            "password", "pass", "passwd", "pwd", "secret",
            "current-password", "new-password"
        )
    }

    fun classifyField(
        autofillId: android.view.autofill.AutofillId,
        hints: Array<String>?,
        inputType: Int,
        text: String,
        htmlAutofillType: Int
    ): AutofillField {
        val hintList = hints?.map { it.lowercase() } ?: emptyList()
        val fieldType = determineFieldType(hintList, inputType)

        return AutofillField(
            autofillId = autofillId,
            fieldType = fieldType,
            hints = hintList,
            inputType = inputType,
            isRequired = isLikelyRequired(text),
            htmlAutofillType = htmlAutofillType
        )
    }

    private fun determineFieldType(
        hints: List<String>,
        inputType: Int
    ): FieldType {
        val maskedInputType = inputType and EditorInfo.TYPE_MASK_CLASS
        if (maskedInputType == EditorInfo.TYPE_CLASS_TEXT) {
            val variation = inputType and EditorInfo.TYPE_MASK_VARIATION
            when (variation) {
                EditorInfo.TYPE_TEXT_VARIATION_PASSWORD,
                EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD,
                EditorInfo.TYPE_TEXT_VARIATION_WEB_PASSWORD -> return FieldType.PASSWORD
                EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
                EditorInfo.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS -> return FieldType.EMAIL
                EditorInfo.TYPE_TEXT_VARIATION_PERSON_NAME -> {
                    if (hints.any { it in USERNAME_HINTS }) return FieldType.USERNAME
                }
            }
        }

        for (hint in hints) {
            when {
                hint in USERNAME_HINTS -> return FieldType.USERNAME
                hint in EMAIL_HINTS -> return FieldType.EMAIL
                hint in PASSWORD_HINTS -> return FieldType.PASSWORD
            }
        }

        for (hint in hints) {
            if (USERNAME_HINTS.any { hint.contains(it) } ||
                EMAIL_HINTS.any { hint.contains(it) } ||
                PASSWORD_HINTS.any { hint.contains(it) }
            ) {
                return if (hint in PASSWORD_HINTS || PASSWORD_HINTS.any { it in hint }) {
                    FieldType.PASSWORD
                } else if (hint in EMAIL_HINTS || EMAIL_HINTS.any { it in hint }) {
                    FieldType.EMAIL
                } else {
                    FieldType.USERNAME
                }
            }
        }

        return FieldType.UNKNOWN
    }

    fun hasLoginFields(fields: List<AutofillField>): Boolean {
        val passwordFields = fields.count { it.fieldType == FieldType.PASSWORD }
        val usernameOrEmailFields = fields.count {
            it.fieldType == FieldType.USERNAME || it.fieldType == FieldType.EMAIL
        }
        return passwordFields >= 1 && usernameOrEmailFields >= 1
    }

    private fun isLikelyRequired(text: String): Boolean {
        val lower = text.lowercase()
        return lower.contains("required") || lower.contains("*")
    }
}
