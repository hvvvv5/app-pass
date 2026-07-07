package com.passgo.app.core.model

enum class FieldInputType {
    TEXT, NUMBER, PASSWORD, EMAIL, URL, DATE, PHONE
}

sealed class FieldValidationResult {
    data object Valid : FieldValidationResult()
    data class Invalid(val message: String) : FieldValidationResult()
}

sealed class FieldDefinition(
    open val fieldId: FieldId,
    open val label: String,
    open val iconLabel: String,
    open val inputType: FieldInputType,
    open val maxLength: Int = 0,
    val autofillHint: String? = null
) {
    abstract fun validate(value: String): FieldValidationResult

    open fun format(value: String): String = value.trim()

    open fun parse(raw: String): String = raw.trim()

    // ── Standard Vault Item Fields ──────────────────────────

    data object ItemName : FieldDefinition(
        fieldId = FieldId.NAME,
        label = "Name",
        iconLabel = "Name",
        inputType = FieldInputType.TEXT,
        maxLength = 200,
        autofillHint = null
    ) {
        override fun validate(value: String): FieldValidationResult {
            if (value.isBlank()) return FieldValidationResult.Invalid("Name is required")
            return FieldValidationResult.Valid
        }
    }

    data object ItemUsername : FieldDefinition(
        fieldId = FieldId.USERNAME,
        label = "Username",
        iconLabel = "Username",
        inputType = FieldInputType.TEXT,
        maxLength = 200,
        autofillHint = "username"
    ) {
        override fun validate(value: String): FieldValidationResult {
            return FieldValidationResult.Valid
        }
    }

    data object ItemPassword : FieldDefinition(
        fieldId = FieldId.PASSWORD,
        label = "Password",
        iconLabel = "Password",
        inputType = FieldInputType.PASSWORD,
        maxLength = 512,
        autofillHint = "password"
    ) {
        override fun validate(value: String): FieldValidationResult {
            if (value.isBlank()) return FieldValidationResult.Invalid("Password is required")
            return FieldValidationResult.Valid
        }
    }

    data object ItemUrl : FieldDefinition(
        fieldId = FieldId.URL,
        label = "Website",
        iconLabel = "URL",
        inputType = FieldInputType.URL,
        maxLength = 2048,
        autofillHint = "url"
    ) {
        override fun validate(value: String): FieldValidationResult {
            return FieldValidationResult.Valid
        }
    }

    data object ItemNotes : FieldDefinition(
        fieldId = FieldId.NOTES,
        label = "Notes",
        iconLabel = "Notes",
        inputType = FieldInputType.TEXT,
        maxLength = 8192,
        autofillHint = null
    ) {
        override fun validate(value: String): FieldValidationResult {
            return FieldValidationResult.Valid
        }
    }

    // ── Credit Card ──────────────────────────────────────────

    data object CreditCardNumber : FieldDefinition(
        fieldId = FieldId.CREDIT_CARD_NUMBER,
        label = "Card Number",
        iconLabel = "Card",
        inputType = FieldInputType.NUMBER,
        maxLength = 19,
        autofillHint = "creditCardNumber"
    ) {
        override fun validate(value: String): FieldValidationResult {
            val cleaned = value.filter { it.isDigit() }
            if (cleaned.length < 13 || cleaned.length > 19)
                return FieldValidationResult.Invalid("Card number must be 13-19 digits")
            if (!luhnCheck(cleaned))
                return FieldValidationResult.Invalid("Invalid card number")
            return FieldValidationResult.Valid
        }

        override fun format(value: String): String {
            val cleaned = value.filter { it.isDigit() }
            return cleaned.chunked(4).joinToString(" ")
        }

        override fun parse(raw: String): String = raw.filter { it.isDigit() }

        private fun luhnCheck(digits: String): Boolean {
            var sum = 0
            var alternate = false
            for (i in digits.indices.reversed()) {
                var n = digits[i] - '0'
                if (alternate) {
                    n *= 2
                    if (n > 9) n -= 9
                }
                sum += n
                alternate = !alternate
            }
            return sum % 10 == 0
        }
    }

    data object CreditCardCvv : FieldDefinition(
        fieldId = FieldId.CREDIT_CARD_CVV,
        label = "CVV",
        iconLabel = "CVV",
        inputType = FieldInputType.NUMBER,
        maxLength = 4,
        autofillHint = "cvv"
    ) {
        override fun validate(value: String): FieldValidationResult {
            val cleaned = value.filter { it.isDigit() }
            if (cleaned.length !in 3..4)
                return FieldValidationResult.Invalid("CVV must be 3 or 4 digits")
            return FieldValidationResult.Valid
        }
    }

    data object CreditCardExpiry : FieldDefinition(
        fieldId = FieldId.CREDIT_CARD_EXPIRY,
        label = "Expiry Date",
        iconLabel = "Expiry",
        inputType = FieldInputType.TEXT,
        maxLength = 5,
        autofillHint = "creditCardExpiryDate"
    ) {
        override fun validate(value: String): FieldValidationResult {
            val cleaned = value.filter { it.isDigit() }
            if (cleaned.length != 4)
                return FieldValidationResult.Invalid("Expiry must be MMYY")
            val month = cleaned.substring(0, 2).toIntOrNull() ?: return FieldValidationResult.Invalid("Invalid month")
            val year = cleaned.substring(2, 4).toIntOrNull() ?: return FieldValidationResult.Invalid("Invalid year")
            if (month !in 1..12)
                return FieldValidationResult.Invalid("Month must be 01-12")
            return FieldValidationResult.Valid
        }

        override fun format(value: String): String {
            val cleaned = value.filter { it.isDigit() }
            if (cleaned.length >= 2) return "${cleaned.substring(0, 2)}/${cleaned.substring(2)}"
            return cleaned
        }

        override fun parse(raw: String): String = raw.filter { it.isDigit() }.let {
            when {
                it.length == 4 -> it
                it.length == 6 -> it.substring(0, 4)
                else -> it
            }
        }
    }

    data object CreditCardHolderName : FieldDefinition(
        fieldId = FieldId.CREDIT_CARD_HOLDER_NAME,
        label = "Cardholder Name",
        iconLabel = "Holder",
        inputType = FieldInputType.TEXT,
        maxLength = 100,
        autofillHint = "cardholderName"
    ) {
        override fun validate(value: String): FieldValidationResult {
            if (value.isBlank()) return FieldValidationResult.Invalid("Name is required")
            return FieldValidationResult.Valid
        }
    }

    // ── Bank ─────────────────────────────────────────────────

    data object Iban : FieldDefinition(
        fieldId = FieldId.IBAN,
        label = "IBAN",
        iconLabel = "IBAN",
        inputType = FieldInputType.TEXT,
        maxLength = 34,
        autofillHint = "iban"
    ) {
        override fun validate(value: String): FieldValidationResult {
            val cleaned = value.filter { it.isLetterOrDigit() }.uppercase()
            if (cleaned.length < 15 || cleaned.length > 34)
                return FieldValidationResult.Invalid("IBAN must be 15-34 characters")
            if (cleaned.length < 2 || !cleaned.take(2).all { it.isLetter() })
                return FieldValidationResult.Invalid("IBAN must start with a country code")
            return FieldValidationResult.Valid
        }

        override fun format(value: String): String {
            val cleaned = value.filter { it.isLetterOrDigit() }.uppercase()
            return cleaned.chunked(4).joinToString(" ")
        }

        override fun parse(raw: String): String = raw.filter { it.isLetterOrDigit() }.uppercase()
    }

    data object BankAccountNumber : FieldDefinition(
        fieldId = FieldId.BANK_ACCOUNT_NUMBER,
        label = "Account Number",
        iconLabel = "Account",
        inputType = FieldInputType.NUMBER,
        maxLength = 20,
        autofillHint = "bankAccountNumber"
    ) {
        override fun validate(value: String): FieldValidationResult {
            val cleaned = value.filter { it.isDigit() }
            if (cleaned.isEmpty()) return FieldValidationResult.Invalid("Account number is required")
            return FieldValidationResult.Valid
        }
    }

    data object BankRoutingNumber : FieldDefinition(
        fieldId = FieldId.BANK_ROUTING_NUMBER,
        label = "Routing Number",
        iconLabel = "Routing",
        inputType = FieldInputType.NUMBER,
        maxLength = 9,
        autofillHint = "routingNumber"
    ) {
        override fun validate(value: String): FieldValidationResult {
            val cleaned = value.filter { it.isDigit() }
            if (cleaned.length != 9)
                return FieldValidationResult.Invalid("Routing number must be 9 digits")
            return FieldValidationResult.Valid
        }
    }

    // ── Identity ─────────────────────────────────────────────

    data object PassportNumber : FieldDefinition(
        fieldId = FieldId.PASSPORT_NUMBER,
        label = "Passport Number",
        iconLabel = "Passport",
        inputType = FieldInputType.TEXT,
        maxLength = 20,
        autofillHint = null
    ) {
        override fun validate(value: String): FieldValidationResult {
            if (value.isBlank()) return FieldValidationResult.Invalid("Passport number is required")
            if (!value.all { it.isLetterOrDigit() })
                return FieldValidationResult.Invalid("Passport number must be alphanumeric")
            return FieldValidationResult.Valid
        }
    }

    data object NationalIdNumber : FieldDefinition(
        fieldId = FieldId.NATIONAL_ID_NUMBER,
        label = "National ID Number",
        iconLabel = "National ID",
        inputType = FieldInputType.TEXT,
        maxLength = 30,
        autofillHint = null
    ) {
        override fun validate(value: String): FieldValidationResult {
            if (value.isBlank()) return FieldValidationResult.Invalid("National ID is required")
            return FieldValidationResult.Valid
        }
    }

    data object DriverLicenseNumber : FieldDefinition(
        fieldId = FieldId.DRIVER_LICENSE_NUMBER,
        label = "Driver License Number",
        iconLabel = "License",
        inputType = FieldInputType.TEXT,
        maxLength = 30,
        autofillHint = null
    ) {
        override fun validate(value: String): FieldValidationResult {
            if (value.isBlank()) return FieldValidationResult.Invalid("License number is required")
            return FieldValidationResult.Valid
        }
    }

    data object FullName : FieldDefinition(
        fieldId = FieldId.FULL_NAME,
        label = "Full Name",
        iconLabel = "Name",
        inputType = FieldInputType.TEXT,
        maxLength = 100,
        autofillHint = "name"
    ) {
        override fun validate(value: String): FieldValidationResult {
            if (value.isBlank()) return FieldValidationResult.Invalid("Name is required")
            return FieldValidationResult.Valid
        }
    }

    data object DateOfBirth : FieldDefinition(
        fieldId = FieldId.DATE_OF_BIRTH,
        label = "Date of Birth",
        iconLabel = "Birthday",
        inputType = FieldInputType.DATE,
        maxLength = 10,
        autofillHint = "birthdate"
    ) {
        override fun validate(value: String): FieldValidationResult {
            val cleaned = value.filter { it.isDigit() }
            if (cleaned.length != 8)
                return FieldValidationResult.Invalid("Date must be YYYYMMDD")
            return FieldValidationResult.Valid
        }

        override fun format(value: String): String {
            val cleaned = value.filter { it.isDigit() }
            if (cleaned.length == 8) return "${cleaned.substring(0, 4)}-${cleaned.substring(4, 6)}-${cleaned.substring(6, 8)}"
            return cleaned
        }

        override fun parse(raw: String): String = raw.filter { it.isDigit() }.let {
            when {
                it.length == 8 -> it
                it.startsWith("19") || it.startsWith("20") -> it.take(8)
                else -> it
            }
        }
    }

    // ── API ──────────────────────────────────────────────────

    data object ApiKey : FieldDefinition(
        fieldId = FieldId.API_KEY,
        label = "API Key",
        iconLabel = "Key",
        inputType = FieldInputType.PASSWORD,
        maxLength = 256,
        autofillHint = null
    ) {
        override fun validate(value: String): FieldValidationResult {
            if (value.isBlank()) return FieldValidationResult.Invalid("API key is required")
            if (value.length < 8) return FieldValidationResult.Invalid("API key must be at least 8 characters")
            return FieldValidationResult.Valid
        }
    }

    data object ApiSecret : FieldDefinition(
        fieldId = FieldId.API_SECRET,
        label = "API Secret",
        iconLabel = "Secret",
        inputType = FieldInputType.PASSWORD,
        maxLength = 512,
        autofillHint = null
    ) {
        override fun validate(value: String): FieldValidationResult {
            if (value.isBlank()) return FieldValidationResult.Invalid("API secret is required")
            return FieldValidationResult.Valid
        }
    }

    // ── Wi-Fi ────────────────────────────────────────────────

    data object WifiSsid : FieldDefinition(
        fieldId = FieldId.WIFI_SSID,
        label = "Network Name (SSID)",
        iconLabel = "Wi-Fi",
        inputType = FieldInputType.TEXT,
        maxLength = 32,
        autofillHint = null
    ) {
        override fun validate(value: String): FieldValidationResult {
            if (value.isBlank()) return FieldValidationResult.Invalid("SSID is required")
            return FieldValidationResult.Valid
        }
    }

    data object WifiPassword : FieldDefinition(
        fieldId = FieldId.WIFI_PASSWORD,
        label = "Password",
        iconLabel = "Password",
        inputType = FieldInputType.PASSWORD,
        maxLength = 64,
        autofillHint = null
    ) {
        override fun validate(value: String): FieldValidationResult {
            if (value.isBlank()) return FieldValidationResult.Invalid("Password is required")
            return FieldValidationResult.Valid
        }
    }

    // ── License ──────────────────────────────────────────────

    data object LicenseKey : FieldDefinition(
        fieldId = FieldId.LICENSE_KEY,
        label = "License Key",
        iconLabel = "License",
        inputType = FieldInputType.TEXT,
        maxLength = 128,
        autofillHint = null
    ) {
        override fun validate(value: String): FieldValidationResult {
            if (value.isBlank()) return FieldValidationResult.Invalid("License key is required")
            return FieldValidationResult.Valid
        }
    }

    // ── Address ─────────────────────────────────────────────

    data object AddressLine1 : FieldDefinition(
        fieldId = FieldId.ADDRESS_LINE_1,
        label = "Address Line 1",
        iconLabel = "Address",
        inputType = FieldInputType.TEXT,
        maxLength = 256,
        autofillHint = "addressLine1"
    ) {
        override fun validate(value: String): FieldValidationResult {
            if (value.isBlank()) return FieldValidationResult.Invalid("Address is required")
            return FieldValidationResult.Valid
        }
    }

    data object AddressLine2 : FieldDefinition(
        fieldId = FieldId.ADDRESS_LINE_2,
        label = "Address Line 2",
        iconLabel = "Address",
        inputType = FieldInputType.TEXT,
        maxLength = 256,
        autofillHint = "addressLine2"
    ) {
        override fun validate(value: String): FieldValidationResult {
            return FieldValidationResult.Valid
        }
    }

    data object City : FieldDefinition(
        fieldId = FieldId.CITY,
        label = "City",
        iconLabel = "City",
        inputType = FieldInputType.TEXT,
        maxLength = 128,
        autofillHint = "addressCity"
    ) {
        override fun validate(value: String): FieldValidationResult {
            if (value.isBlank()) return FieldValidationResult.Invalid("City is required")
            return FieldValidationResult.Valid
        }
    }

    data object PostalCode : FieldDefinition(
        fieldId = FieldId.POSTAL_CODE,
        label = "Postal Code",
        iconLabel = "Postal",
        inputType = FieldInputType.TEXT,
        maxLength = 20,
        autofillHint = "postalCode"
    ) {
        override fun validate(value: String): FieldValidationResult {
            if (value.isBlank()) return FieldValidationResult.Invalid("Postal code is required")
            return FieldValidationResult.Valid
        }
    }

    data object Country : FieldDefinition(
        fieldId = FieldId.COUNTRY,
        label = "Country",
        iconLabel = "Country",
        inputType = FieldInputType.TEXT,
        maxLength = 128,
        autofillHint = "countryName"
    ) {
        override fun validate(value: String): FieldValidationResult {
            if (value.isBlank()) return FieldValidationResult.Invalid("Country is required")
            return FieldValidationResult.Valid
        }
    }

    data object PhoneNumber : FieldDefinition(
        fieldId = FieldId.PHONE_NUMBER,
        label = "Phone Number",
        iconLabel = "Phone",
        inputType = FieldInputType.PHONE,
        maxLength = 30,
        autofillHint = "phone"
    ) {
        override fun validate(value: String): FieldValidationResult {
            return FieldValidationResult.Valid
        }
    }

    // ── Bank Details ─────────────────────────────────────────

    data object SwiftBic : FieldDefinition(
        fieldId = FieldId.SWIFT_BIC,
        label = "SWIFT/BIC",
        iconLabel = "SWIFT",
        inputType = FieldInputType.TEXT,
        maxLength = 11,
        autofillHint = null
    ) {
        override fun validate(value: String): FieldValidationResult {
            if (value.isBlank()) return FieldValidationResult.Invalid("SWIFT/BIC is required")
            if (value.length < 8 || value.length > 11)
                return FieldValidationResult.Invalid("SWIFT/BIC must be 8-11 characters")
            return FieldValidationResult.Valid
        }

        override fun format(value: String): String = value.uppercase()
        override fun parse(raw: String): String = raw.uppercase()
    }

    // ── License Details ──────────────────────────────────────

    data object LicenseType : FieldDefinition(
        fieldId = FieldId.LICENSE_TYPE,
        label = "License Type",
        iconLabel = "Type",
        inputType = FieldInputType.TEXT,
        maxLength = 100,
        autofillHint = null
    ) {
        override fun validate(value: String): FieldValidationResult {
            return FieldValidationResult.Valid
        }
    }

    data object LicenseRegisteredEmail : FieldDefinition(
        fieldId = FieldId.LICENSE_REGISTERED_EMAIL,
        label = "Registered Email",
        iconLabel = "Email",
        inputType = FieldInputType.EMAIL,
        maxLength = 256,
        autofillHint = "email"
    ) {
        override fun validate(value: String): FieldValidationResult {
            if (value.isNotBlank() && !value.contains("@"))
                return FieldValidationResult.Invalid("Must be a valid email")
            return FieldValidationResult.Valid
        }
    }

    data object LicensePurchaseDate : FieldDefinition(
        fieldId = FieldId.LICENSE_PURCHASE_DATE,
        label = "Purchase Date",
        iconLabel = "Date",
        inputType = FieldInputType.DATE,
        maxLength = 10,
        autofillHint = null
    ) {
        override fun validate(value: String): FieldValidationResult {
            if (value.isBlank()) return FieldValidationResult.Valid
            val cleaned = value.filter { it.isDigit() }
            if (cleaned.length != 8)
                return FieldValidationResult.Invalid("Date must be YYYYMMDD")
            return FieldValidationResult.Valid
        }

        override fun format(value: String): String {
            val cleaned = value.filter { it.isDigit() }
            if (cleaned.length == 8) return "${cleaned.substring(0, 4)}-${cleaned.substring(4, 6)}-${cleaned.substring(6, 8)}"
            return cleaned
        }

        override fun parse(raw: String): String = raw.filter { it.isDigit() }.let {
            when {
                it.length == 8 -> it
                it.startsWith("19") || it.startsWith("20") -> it.take(8)
                else -> it
            }
        }
    }

    data object LicenseExpiryDate : FieldDefinition(
        fieldId = FieldId.LICENSE_EXPIRY_DATE,
        label = "Expiry Date",
        iconLabel = "Expiry",
        inputType = FieldInputType.DATE,
        maxLength = 10,
        autofillHint = null
    ) {
        override fun validate(value: String): FieldValidationResult {
            if (value.isBlank()) return FieldValidationResult.Valid
            val cleaned = value.filter { it.isDigit() }
            if (cleaned.length != 8)
                return FieldValidationResult.Invalid("Date must be YYYYMMDD")
            return FieldValidationResult.Valid
        }

        override fun format(value: String): String {
            val cleaned = value.filter { it.isDigit() }
            if (cleaned.length == 8) return "${cleaned.substring(0, 4)}-${cleaned.substring(4, 6)}-${cleaned.substring(6, 8)}"
            return cleaned
        }

        override fun parse(raw: String): String = raw.filter { it.isDigit() }.let {
            when {
                it.length == 8 -> it
                it.startsWith("19") || it.startsWith("20") -> it.take(8)
                else -> it
            }
        }
    }

    // ── Server ───────────────────────────────────────────────

    data object ServerHostname : FieldDefinition(
        fieldId = FieldId.SERVER_HOSTNAME,
        label = "Hostname",
        iconLabel = "Server",
        inputType = FieldInputType.URL,
        maxLength = 255,
        autofillHint = null
    ) {
        override fun validate(value: String): FieldValidationResult {
            if (value.isBlank()) return FieldValidationResult.Invalid("Hostname is required")
            return FieldValidationResult.Valid
        }
    }

    data object ServerPort : FieldDefinition(
        fieldId = FieldId.SERVER_PORT,
        label = "Port",
        iconLabel = "Port",
        inputType = FieldInputType.NUMBER,
        maxLength = 5,
        autofillHint = null
    ) {
        override fun validate(value: String): FieldValidationResult {
            val cleaned = value.filter { it.isDigit() }
            val port = cleaned.toIntOrNull()
            if (port == null || port !in 1..65535)
                return FieldValidationResult.Invalid("Port must be 1-65535")
            return FieldValidationResult.Valid
        }
    }

    // ── Custom ───────────────────────────────────────────────

    data class CustomLabel(
        override val fieldId: FieldId,
        override val label: String = "Label",
        override val iconLabel: String = "Custom",
        override val inputType: FieldInputType = FieldInputType.TEXT,
        override val maxLength: Int = 100
    ) : FieldDefinition(
        fieldId = fieldId,
        label = label,
        iconLabel = iconLabel,
        inputType = inputType,
        maxLength = maxLength
    ) {
        override fun validate(value: String): FieldValidationResult {
            if (value.isBlank()) return FieldValidationResult.Invalid("Label is required")
            return FieldValidationResult.Valid
        }
    }

    data class CustomText(
        override val fieldId: FieldId,
        override val label: String = "Text",
        override val iconLabel: String = "Text",
        override val inputType: FieldInputType = FieldInputType.TEXT,
        override val maxLength: Int = 1024
    ) : FieldDefinition(
        fieldId = fieldId,
        label = label,
        iconLabel = iconLabel,
        inputType = inputType,
        maxLength = maxLength
    ) {
        override fun validate(value: String): FieldValidationResult {
            return FieldValidationResult.Valid
        }
    }

    data class CustomNumber(
        override val fieldId: FieldId,
        override val label: String = "Number",
        override val iconLabel: String = "Number",
        override val inputType: FieldInputType = FieldInputType.NUMBER,
        override val maxLength: Int = 50
    ) : FieldDefinition(
        fieldId = fieldId,
        label = label,
        iconLabel = iconLabel,
        inputType = inputType,
        maxLength = maxLength
    ) {
        override fun validate(value: String): FieldValidationResult {
            if (value.isNotBlank() && value.toDoubleOrNull() == null)
                return FieldValidationResult.Invalid("Must be a valid number")
            return FieldValidationResult.Valid
        }
    }

    // ── Lookup ───────────────────────────────────────────────

    companion object {
        private val allDefinitions: Map<FieldId, FieldDefinition> by lazy {
            listOf(
            ItemName, ItemUsername, ItemPassword, ItemUrl, ItemNotes,
            CreditCardNumber, CreditCardCvv, CreditCardExpiry, CreditCardHolderName,
            Iban, BankAccountNumber, BankRoutingNumber,
            PassportNumber, NationalIdNumber, DriverLicenseNumber, FullName, DateOfBirth,
            ApiKey, ApiSecret,
            WifiSsid, WifiPassword,
            LicenseKey,
            LicenseType, LicenseRegisteredEmail, LicensePurchaseDate, LicenseExpiryDate,
            AddressLine1, AddressLine2, City, PostalCode, Country,
            PhoneNumber, SwiftBic,
            ServerHostname, ServerPort,
            CustomLabel(FieldId.CUSTOM_LABEL),
            CustomText(FieldId.CUSTOM_TEXT),
            CustomNumber(FieldId.CUSTOM_NUMBER),
            CustomText(FieldId.CUSTOM_URL, inputType = FieldInputType.URL),
            CustomText(FieldId.CUSTOM_DATE, inputType = FieldInputType.DATE),
            CustomText(FieldId.CUSTOM_EMAIL, inputType = FieldInputType.EMAIL)
            ).associateBy { it.fieldId }
        }

        fun fromId(fieldId: FieldId): FieldDefinition =
            allDefinitions[fieldId] ?: CustomText(fieldId)
    }
}
