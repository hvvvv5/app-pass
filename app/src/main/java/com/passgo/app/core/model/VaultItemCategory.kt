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
    SHOPPING(
        displayName = "Shopping",
        description = "Online shopping account and payment details",
        icon = CategoryIconIdentifier.SHOPPING,
        colorArgb = 0xFFFF6D00,
        sortOrder = 5,
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
        sortOrder = 6,
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
        sortOrder = 7,
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
        sortOrder = 8,
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
        sortOrder = 9,
        groups = listOf(FieldGroup.GENERAL, FieldGroup.NETWORK, FieldGroup.NOTES),
        fields = listOf(FieldId.NAME, FieldId.WIFI_SSID, FieldId.WIFI_PASSWORD, FieldId.NOTES),
        requiredFields = listOf(FieldId.NAME, FieldId.WIFI_SSID, FieldId.WIFI_PASSWORD)
    ),
    SOFTWARE_LICENSE(
        displayName = "Software License",
        description = "Software license key and registration details",
        icon = CategoryIconIdentifier.SOFTWARE_LICENSE,
        colorArgb = 0xFF00BCD4,
        sortOrder = 10,
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
        sortOrder = 11,
        groups = listOf(FieldGroup.GENERAL, FieldGroup.NOTES),
        fields = listOf(FieldId.NAME, FieldId.NOTES),
        requiredFields = listOf(FieldId.NAME)
    ),
    OTHER(
        displayName = "Other",
        description = "General purpose vault item",
        icon = CategoryIconIdentifier.OTHER,
        colorArgb = 0xFF9E9E9E,
        sortOrder = 12,
        groups = listOf(FieldGroup.GENERAL, FieldGroup.ACCOUNT, FieldGroup.CREDENTIALS, FieldGroup.NOTES),
        fields = listOf(FieldId.NAME, FieldId.USERNAME, FieldId.EMAIL_ADDRESS, FieldId.PASSWORD, FieldId.URL, FieldId.NOTES),
        requiredFields = listOf(FieldId.NAME),
        recommendedFields = listOf(FieldId.PASSWORD)
    )
}
