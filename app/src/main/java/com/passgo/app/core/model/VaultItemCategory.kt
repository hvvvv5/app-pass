package com.passgo.app.core.model

enum class VaultItemCategory(
    val displayName: String,
    val description: String,
    val icon: CategoryIconIdentifier,
    val colorArgb: Long,
    val sortOrder: Int,
    val groups: List<FieldGroup>,
    val fields: List<FieldId>,
    val recommendedFields: List<FieldId> = emptyList(),
    val requiredFields: List<FieldId> = emptyList()
) {
    GOOGLE_ACCOUNT(
        displayName = "Google Account",
        description = "Google account credentials and recovery info",
        icon = CategoryIconIdentifier.GOOGLE,
        colorArgb = 0xFF4285F4,
        sortOrder = 1,
        groups = listOf(FieldGroup.GENERAL, FieldGroup.ACCOUNT, FieldGroup.CREDENTIALS, FieldGroup.CONTACT, FieldGroup.NOTES),
        fields = listOf(FieldId.NAME, FieldId.EMAIL_ADDRESS, FieldId.PASSWORD, FieldId.URL, FieldId.PHONE_NUMBER, FieldId.NOTES),
        requiredFields = listOf(FieldId.NAME, FieldId.EMAIL_ADDRESS, FieldId.PASSWORD),
        recommendedFields = listOf(FieldId.URL, FieldId.PHONE_NUMBER)
    ),
    EMAIL(
        displayName = "Email",
        description = "Email account login credentials",
        icon = CategoryIconIdentifier.EMAIL,
        colorArgb = 0xFFEA4335,
        sortOrder = 2,
        groups = listOf(FieldGroup.GENERAL, FieldGroup.ACCOUNT, FieldGroup.CREDENTIALS, FieldGroup.NOTES),
        fields = listOf(FieldId.NAME, FieldId.EMAIL_ADDRESS, FieldId.PASSWORD, FieldId.URL, FieldId.NOTES),
        requiredFields = listOf(FieldId.NAME, FieldId.EMAIL_ADDRESS, FieldId.PASSWORD)
    ),
    SOCIAL_MEDIA(
        displayName = "Social Media",
        description = "Social media account login",
        icon = CategoryIconIdentifier.SOCIAL_MEDIA,
        colorArgb = 0xFF1DA1F2,
        sortOrder = 3,
        groups = listOf(FieldGroup.GENERAL, FieldGroup.ACCOUNT, FieldGroup.CREDENTIALS, FieldGroup.NOTES),
        fields = listOf(FieldId.NAME, FieldId.USERNAME, FieldId.EMAIL_ADDRESS, FieldId.PASSWORD, FieldId.URL, FieldId.NOTES),
        requiredFields = listOf(FieldId.NAME, FieldId.PASSWORD),
        recommendedFields = listOf(FieldId.USERNAME, FieldId.EMAIL_ADDRESS, FieldId.URL)
    ),
    BANKING(
        displayName = "Banking",
        description = "Banking account and financial information",
        icon = CategoryIconIdentifier.BANKING,
        colorArgb = 0xFF34A853,
        sortOrder = 4,
        groups = listOf(FieldGroup.GENERAL, FieldGroup.ACCOUNT, FieldGroup.CREDENTIALS, FieldGroup.PERSONAL_INFO, FieldGroup.BANK_DETAILS, FieldGroup.NOTES),
        fields = listOf(FieldId.NAME, FieldId.USERNAME, FieldId.PASSWORD, FieldId.FULL_NAME, FieldId.BANK_ACCOUNT_NUMBER, FieldId.BANK_ROUTING_NUMBER, FieldId.IBAN, FieldId.SWIFT_BIC, FieldId.NOTES),
        requiredFields = listOf(FieldId.NAME),
        recommendedFields = listOf(FieldId.USERNAME, FieldId.PASSWORD, FieldId.FULL_NAME, FieldId.BANK_ACCOUNT_NUMBER)
    ),
    CREDIT_CARD(
        displayName = "Credit Card",
        description = "Credit card information and details",
        icon = CategoryIconIdentifier.CREDIT_CARD,
        colorArgb = 0xFF1565C0,
        sortOrder = 5,
        groups = listOf(FieldGroup.GENERAL, FieldGroup.PAYMENT, FieldGroup.SECURITY, FieldGroup.NOTES),
        fields = listOf(FieldId.NAME, FieldId.CREDIT_CARD_HOLDER_NAME, FieldId.CREDIT_CARD_NUMBER, FieldId.CREDIT_CARD_EXPIRY, FieldId.CREDIT_CARD_CVV, FieldId.CREDIT_CARD_PIN, FieldId.NOTES),
        requiredFields = listOf(FieldId.NAME, FieldId.CREDIT_CARD_NUMBER, FieldId.CREDIT_CARD_EXPIRY, FieldId.CREDIT_CARD_CVV),
        recommendedFields = listOf(FieldId.CREDIT_CARD_HOLDER_NAME)
    ),
    DEBIT_CARD(
        displayName = "Debit Card",
        description = "Debit card information and bank details",
        icon = CategoryIconIdentifier.DEBIT_CARD,
        colorArgb = 0xFF0D47A1,
        sortOrder = 6,
        groups = listOf(FieldGroup.GENERAL, FieldGroup.PAYMENT, FieldGroup.BANK_DETAILS, FieldGroup.SECURITY, FieldGroup.NOTES),
        fields = listOf(FieldId.NAME, FieldId.CREDIT_CARD_HOLDER_NAME, FieldId.CREDIT_CARD_NUMBER, FieldId.CREDIT_CARD_EXPIRY, FieldId.CREDIT_CARD_PIN, FieldId.BANK_NAME, FieldId.NOTES),
        requiredFields = listOf(FieldId.NAME, FieldId.CREDIT_CARD_NUMBER, FieldId.CREDIT_CARD_EXPIRY),
        recommendedFields = listOf(FieldId.CREDIT_CARD_HOLDER_NAME, FieldId.BANK_NAME)
    ),
    BANK_ACCOUNT(
        displayName = "Bank Account",
        description = "Bank account details for transfers and direct deposits",
        icon = CategoryIconIdentifier.BANK_ACCOUNT,
        colorArgb = 0xFF2E7D32,
        sortOrder = 7,
        groups = listOf(FieldGroup.GENERAL, FieldGroup.PERSONAL_INFO, FieldGroup.BANK_DETAILS, FieldGroup.NOTES),
        fields = listOf(FieldId.NAME, FieldId.ACCOUNT_HOLDER, FieldId.BANK_NAME, FieldId.BANK_ACCOUNT_NUMBER, FieldId.BANK_ROUTING_NUMBER, FieldId.IBAN, FieldId.SWIFT_BIC, FieldId.BRANCH, FieldId.CURRENCY, FieldId.NOTES),
        requiredFields = listOf(FieldId.NAME, FieldId.BANK_ACCOUNT_NUMBER),
        recommendedFields = listOf(FieldId.ACCOUNT_HOLDER, FieldId.BANK_NAME, FieldId.IBAN, FieldId.SWIFT_BIC)
    ),
    PAYPAL(
        displayName = "PayPal",
        description = "PayPal account login and payment details",
        icon = CategoryIconIdentifier.PAYPAL,
        colorArgb = 0xFF003087,
        sortOrder = 8,
        groups = listOf(FieldGroup.GENERAL, FieldGroup.ACCOUNT, FieldGroup.CREDENTIALS, FieldGroup.CONTACT, FieldGroup.NOTES),
        fields = listOf(FieldId.NAME, FieldId.PAYPAL_EMAIL, FieldId.PASSWORD, FieldId.PHONE_NUMBER, FieldId.NOTES),
        requiredFields = listOf(FieldId.NAME, FieldId.PAYPAL_EMAIL),
        recommendedFields = listOf(FieldId.PASSWORD, FieldId.PHONE_NUMBER)
    ),
    WISE(
        displayName = "Wise",
        description = "Wise (TransferWise) account and transfer details",
        icon = CategoryIconIdentifier.WISE,
        colorArgb = 0xFF00B9FF,
        sortOrder = 9,
        groups = listOf(FieldGroup.GENERAL, FieldGroup.PERSONAL_INFO, FieldGroup.BANK_DETAILS, FieldGroup.NOTES),
        fields = listOf(FieldId.NAME, FieldId.BENEFICIARY, FieldId.IBAN, FieldId.ACCOUNT_HOLDER, FieldId.BANK_NAME, FieldId.BANK_ACCOUNT_NUMBER, FieldId.SWIFT_BIC, FieldId.CURRENCY, FieldId.REFERENCE, FieldId.NOTES),
        requiredFields = listOf(FieldId.NAME),
        recommendedFields = listOf(FieldId.BENEFICIARY, FieldId.IBAN, FieldId.BANK_ACCOUNT_NUMBER, FieldId.CURRENCY, FieldId.REFERENCE)
    ),
    STRIPE(
        displayName = "Stripe",
        description = "Stripe payment processing account and API credentials",
        icon = CategoryIconIdentifier.STRIPE,
        colorArgb = 0xFF635BFF,
        sortOrder = 10,
        groups = listOf(FieldGroup.GENERAL, FieldGroup.ACCOUNT, FieldGroup.API, FieldGroup.CONTACT, FieldGroup.NOTES),
        fields = listOf(FieldId.NAME, FieldId.MERCHANT_ID, FieldId.CUSTOMER_ID, FieldId.API_KEY, FieldId.API_SECRET, FieldId.EMAIL_ADDRESS, FieldId.PHONE_NUMBER, FieldId.URL, FieldId.NOTES),
        requiredFields = listOf(FieldId.NAME),
        recommendedFields = listOf(FieldId.MERCHANT_ID, FieldId.API_KEY, FieldId.API_SECRET)
    ),
    SHOPPING(
        displayName = "Shopping",
        description = "Online shopping account and payment details",
        icon = CategoryIconIdentifier.SHOPPING,
        colorArgb = 0xFFFF6D00,
        sortOrder = 11,
        groups = listOf(FieldGroup.GENERAL, FieldGroup.ACCOUNT, FieldGroup.CREDENTIALS, FieldGroup.PERSONAL_INFO, FieldGroup.ADDRESS, FieldGroup.CONTACT, FieldGroup.NOTES),
        fields = listOf(FieldId.NAME, FieldId.USERNAME, FieldId.EMAIL_ADDRESS, FieldId.PASSWORD, FieldId.URL, FieldId.FULL_NAME, FieldId.ADDRESS_LINE_1, FieldId.ADDRESS_LINE_2, FieldId.CITY, FieldId.POSTAL_CODE, FieldId.COUNTRY, FieldId.PHONE_NUMBER, FieldId.NOTES),
        requiredFields = listOf(FieldId.NAME),
        recommendedFields = listOf(FieldId.EMAIL_ADDRESS, FieldId.PASSWORD, FieldId.URL, FieldId.FULL_NAME, FieldId.ADDRESS_LINE_1)
    ),
    WORK(
        displayName = "Work",
        description = "Work-related account credentials",
        icon = CategoryIconIdentifier.WORK,
        colorArgb = 0xFF5F6368,
        sortOrder = 12,
        groups = listOf(FieldGroup.GENERAL, FieldGroup.ACCOUNT, FieldGroup.CREDENTIALS, FieldGroup.NOTES),
        fields = listOf(FieldId.NAME, FieldId.USERNAME, FieldId.EMAIL_ADDRESS, FieldId.PASSWORD, FieldId.URL, FieldId.NOTES),
        requiredFields = listOf(FieldId.NAME, FieldId.PASSWORD),
        recommendedFields = listOf(FieldId.USERNAME, FieldId.EMAIL_ADDRESS, FieldId.URL)
    ),
    ENTERTAINMENT(
        displayName = "Entertainment",
        description = "Entertainment service login (streaming, media)",
        icon = CategoryIconIdentifier.ENTERTAINMENT,
        colorArgb = 0xFFE91E63,
        sortOrder = 13,
        groups = listOf(FieldGroup.GENERAL, FieldGroup.ACCOUNT, FieldGroup.CREDENTIALS, FieldGroup.NOTES),
        fields = listOf(FieldId.NAME, FieldId.USERNAME, FieldId.EMAIL_ADDRESS, FieldId.PASSWORD, FieldId.URL, FieldId.NOTES),
        requiredFields = listOf(FieldId.NAME, FieldId.PASSWORD),
        recommendedFields = listOf(FieldId.EMAIL_ADDRESS, FieldId.URL)
    ),
    GAMING(
        displayName = "Gaming",
        description = "Gaming platform account credentials",
        icon = CategoryIconIdentifier.GAMING,
        colorArgb = 0xFF7C4DFF,
        sortOrder = 14,
        groups = listOf(FieldGroup.GENERAL, FieldGroup.ACCOUNT, FieldGroup.CREDENTIALS, FieldGroup.NOTES),
        fields = listOf(FieldId.NAME, FieldId.USERNAME, FieldId.EMAIL_ADDRESS, FieldId.PASSWORD, FieldId.URL, FieldId.NOTES),
        requiredFields = listOf(FieldId.NAME, FieldId.PASSWORD),
        recommendedFields = listOf(FieldId.USERNAME, FieldId.EMAIL_ADDRESS)
    ),
    WIFI(
        displayName = "Wi-Fi",
        description = "Wi-Fi network credentials and configuration",
        icon = CategoryIconIdentifier.WIFI,
        colorArgb = 0xFF0097A7,
        sortOrder = 15,
        groups = listOf(FieldGroup.GENERAL, FieldGroup.NETWORK, FieldGroup.NOTES),
        fields = listOf(FieldId.NAME, FieldId.WIFI_SSID, FieldId.WIFI_PASSWORD, FieldId.NOTES),
        requiredFields = listOf(FieldId.NAME, FieldId.WIFI_SSID, FieldId.WIFI_PASSWORD)
    ),
    SOFTWARE_LICENSE(
        displayName = "Software License",
        description = "Software license key and registration details",
        icon = CategoryIconIdentifier.SOFTWARE_LICENSE,
        colorArgb = 0xFF00BCD4,
        sortOrder = 16,
        groups = listOf(FieldGroup.GENERAL, FieldGroup.LICENSE, FieldGroup.NOTES),
        fields = listOf(FieldId.NAME, FieldId.LICENSE_KEY, FieldId.LICENSE_TYPE, FieldId.LICENSE_REGISTERED_EMAIL, FieldId.LICENSE_PURCHASE_DATE, FieldId.LICENSE_EXPIRY_DATE, FieldId.URL, FieldId.NOTES),
        requiredFields = listOf(FieldId.NAME, FieldId.LICENSE_KEY),
        recommendedFields = listOf(FieldId.LICENSE_REGISTERED_EMAIL, FieldId.LICENSE_EXPIRY_DATE)
    ),
    SECURE_NOTE(
        displayName = "Secure Note",
        description = "Encrypted note for secure information",
        icon = CategoryIconIdentifier.SECURE_NOTE,
        colorArgb = 0xFF607D8B,
        sortOrder = 17,
        groups = listOf(FieldGroup.GENERAL, FieldGroup.NOTES),
        fields = listOf(FieldId.NAME, FieldId.NOTES),
        requiredFields = listOf(FieldId.NAME)
    ),
    PASSPORT(
        displayName = "Passport",
        description = "Passport information and travel document details",
        icon = CategoryIconIdentifier.PASSPORT,
        colorArgb = 0xFF1A237E,
        sortOrder = 19,
        groups = listOf(FieldGroup.GENERAL, FieldGroup.PERSONAL_INFO, FieldGroup.IDENTITY, FieldGroup.NOTES),
        fields = listOf(FieldId.NAME, FieldId.FULL_NAME, FieldId.PASSPORT_NUMBER, FieldId.PASSPORT_TYPE, FieldId.NATIONALITY, FieldId.DATE_OF_BIRTH, FieldId.PASSPORT_EXPIRY, FieldId.ISSUING_COUNTRY, FieldId.NOTES),
        requiredFields = listOf(FieldId.NAME, FieldId.FULL_NAME, FieldId.PASSPORT_NUMBER, FieldId.PASSPORT_EXPIRY),
        recommendedFields = listOf(FieldId.PASSPORT_TYPE, FieldId.NATIONALITY, FieldId.DATE_OF_BIRTH, FieldId.ISSUING_COUNTRY)
    ),
    NATIONAL_ID(
        displayName = "National ID",
        description = "National identification card details",
        icon = CategoryIconIdentifier.NATIONAL_ID,
        colorArgb = 0xFF4A148C,
        sortOrder = 20,
        groups = listOf(FieldGroup.GENERAL, FieldGroup.PERSONAL_INFO, FieldGroup.IDENTITY, FieldGroup.NOTES),
        fields = listOf(FieldId.NAME, FieldId.FULL_NAME, FieldId.NATIONAL_ID_NUMBER, FieldId.DATE_OF_BIRTH, FieldId.NATIONALITY, FieldId.NOTES),
        requiredFields = listOf(FieldId.NAME, FieldId.FULL_NAME, FieldId.NATIONAL_ID_NUMBER),
        recommendedFields = listOf(FieldId.DATE_OF_BIRTH, FieldId.NATIONALITY)
    ),
    DRIVER_LICENSE(
        displayName = "Driver License",
        description = "Driver license information and details",
        icon = CategoryIconIdentifier.DRIVER_LICENSE,
        colorArgb = 0xFFE65100,
        sortOrder = 21,
        groups = listOf(FieldGroup.GENERAL, FieldGroup.PERSONAL_INFO, FieldGroup.IDENTITY, FieldGroup.ADDRESS, FieldGroup.NOTES),
        fields = listOf(FieldId.NAME, FieldId.FULL_NAME, FieldId.DRIVER_LICENSE_NUMBER, FieldId.LICENSE_CLASS, FieldId.DATE_OF_BIRTH, FieldId.LICENSE_EXPIRY_DATE, FieldId.ADDRESS_LINE_1, FieldId.ADDRESS_LINE_2, FieldId.CITY, FieldId.POSTAL_CODE, FieldId.COUNTRY, FieldId.NOTES),
        requiredFields = listOf(FieldId.NAME, FieldId.FULL_NAME, FieldId.DRIVER_LICENSE_NUMBER),
        recommendedFields = listOf(FieldId.LICENSE_CLASS, FieldId.DATE_OF_BIRTH, FieldId.LICENSE_EXPIRY_DATE)
    ),
    RESIDENCE_PERMIT(
        displayName = "Residence Permit",
        description = "Residence permit and immigration document details",
        icon = CategoryIconIdentifier.RESIDENCE_PERMIT,
        colorArgb = 0xFF00695C,
        sortOrder = 22,
        groups = listOf(FieldGroup.GENERAL, FieldGroup.PERSONAL_INFO, FieldGroup.IDENTITY, FieldGroup.NOTES),
        fields = listOf(FieldId.NAME, FieldId.FULL_NAME, FieldId.RESIDENCE_PERMIT_NUMBER, FieldId.RESIDENCE_PERMIT_TYPE, FieldId.PASSPORT_NUMBER, FieldId.ISSUING_COUNTRY, FieldId.DATE_OF_BIRTH, FieldId.NATIONALITY, FieldId.RESIDENCE_PERMIT_EXPIRY, FieldId.NOTES),
        requiredFields = listOf(FieldId.NAME, FieldId.FULL_NAME, FieldId.RESIDENCE_PERMIT_NUMBER, FieldId.RESIDENCE_PERMIT_EXPIRY),
        recommendedFields = listOf(FieldId.RESIDENCE_PERMIT_TYPE, FieldId.PASSPORT_NUMBER, FieldId.ISSUING_COUNTRY, FieldId.NATIONALITY)
    ),
    HEALTH_INSURANCE(
        displayName = "Health Insurance",
        description = "Health insurance card and policy details",
        icon = CategoryIconIdentifier.HEALTH_INSURANCE,
        colorArgb = 0xFF1B5E20,
        sortOrder = 23,
        groups = listOf(FieldGroup.GENERAL, FieldGroup.PERSONAL_INFO, FieldGroup.IDENTITY, FieldGroup.CONTACT, FieldGroup.NOTES),
        fields = listOf(FieldId.NAME, FieldId.FULL_NAME, FieldId.HEALTH_INSURANCE_PROVIDER, FieldId.HEALTH_INSURANCE_ID, FieldId.HEALTH_INSURANCE_GROUP_NUMBER, FieldId.POLICY_NUMBER, FieldId.PHONE_NUMBER, FieldId.NOTES),
        requiredFields = listOf(FieldId.NAME, FieldId.FULL_NAME, FieldId.HEALTH_INSURANCE_PROVIDER, FieldId.POLICY_NUMBER),
        recommendedFields = listOf(FieldId.HEALTH_INSURANCE_ID, FieldId.HEALTH_INSURANCE_GROUP_NUMBER, FieldId.PHONE_NUMBER)
    ),
    SOCIAL_SECURITY(
        displayName = "Social Security",
        description = "Social security number and related information",
        icon = CategoryIconIdentifier.SOCIAL_SECURITY,
        colorArgb = 0xFF37474F,
        sortOrder = 24,
        groups = listOf(FieldGroup.GENERAL, FieldGroup.PERSONAL_INFO, FieldGroup.IDENTITY, FieldGroup.NOTES),
        fields = listOf(FieldId.NAME, FieldId.FULL_NAME, FieldId.SOCIAL_SECURITY_NUMBER, FieldId.NOTES),
        requiredFields = listOf(FieldId.NAME, FieldId.FULL_NAME, FieldId.SOCIAL_SECURITY_NUMBER)
    ),
    TAX_ID(
        displayName = "Tax ID",
        description = "Tax identification number information",
        icon = CategoryIconIdentifier.TAX_ID,
        colorArgb = 0xFF4E342E,
        sortOrder = 25,
        groups = listOf(FieldGroup.GENERAL, FieldGroup.PERSONAL_INFO, FieldGroup.IDENTITY, FieldGroup.NOTES),
        fields = listOf(FieldId.NAME, FieldId.FULL_NAME, FieldId.TAX_ID, FieldId.NOTES),
        requiredFields = listOf(FieldId.NAME, FieldId.FULL_NAME, FieldId.TAX_ID)
    ),
    STUDENT_ID(
        displayName = "Student ID",
        description = "Student identification card details",
        icon = CategoryIconIdentifier.STUDENT_ID,
        colorArgb = 0xFF0277BD,
        sortOrder = 26,
        groups = listOf(FieldGroup.GENERAL, FieldGroup.PERSONAL_INFO, FieldGroup.IDENTITY, FieldGroup.NOTES),
        fields = listOf(FieldId.NAME, FieldId.FULL_NAME, FieldId.STUDENT_ID_NUMBER, FieldId.INSTITUTION_NAME, FieldId.DATE_OF_BIRTH, FieldId.NOTES),
        requiredFields = listOf(FieldId.NAME, FieldId.FULL_NAME, FieldId.INSTITUTION_NAME),
        recommendedFields = listOf(FieldId.STUDENT_ID_NUMBER, FieldId.DATE_OF_BIRTH)
    ),
    OTHER(
        displayName = "Other",
        description = "General purpose vault item",
        icon = CategoryIconIdentifier.OTHER,
        colorArgb = 0xFF9E9E9E,
        sortOrder = 18,
        groups = listOf(FieldGroup.GENERAL, FieldGroup.ACCOUNT, FieldGroup.CREDENTIALS, FieldGroup.NOTES),
        fields = listOf(FieldId.NAME, FieldId.USERNAME, FieldId.EMAIL_ADDRESS, FieldId.PASSWORD, FieldId.URL, FieldId.NOTES),
        requiredFields = listOf(FieldId.NAME),
        recommendedFields = listOf(FieldId.PASSWORD)
    )
}
