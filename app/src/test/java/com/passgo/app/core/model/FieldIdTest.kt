package com.passgo.app.core.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FieldIdTest {

    @Test
    fun `enum names are stable and never renamed`() {
        assertEquals("CREDIT_CARD_NUMBER", FieldId.CREDIT_CARD_NUMBER.name)
        assertEquals("CREDIT_CARD_CVV", FieldId.CREDIT_CARD_CVV.name)
        assertEquals("CREDIT_CARD_EXPIRY", FieldId.CREDIT_CARD_EXPIRY.name)
        assertEquals("CREDIT_CARD_HOLDER_NAME", FieldId.CREDIT_CARD_HOLDER_NAME.name)
        assertEquals("BANK_ACCOUNT_NUMBER", FieldId.BANK_ACCOUNT_NUMBER.name)
        assertEquals("BANK_ROUTING_NUMBER", FieldId.BANK_ROUTING_NUMBER.name)
        assertEquals("IBAN", FieldId.IBAN.name)
        assertEquals("PASSPORT_NUMBER", FieldId.PASSPORT_NUMBER.name)
        assertEquals("NATIONAL_ID_NUMBER", FieldId.NATIONAL_ID_NUMBER.name)
        assertEquals("DRIVER_LICENSE_NUMBER", FieldId.DRIVER_LICENSE_NUMBER.name)
        assertEquals("FULL_NAME", FieldId.FULL_NAME.name)
        assertEquals("DATE_OF_BIRTH", FieldId.DATE_OF_BIRTH.name)
        assertEquals("API_KEY", FieldId.API_KEY.name)
        assertEquals("API_SECRET", FieldId.API_SECRET.name)
        assertEquals("WIFI_SSID", FieldId.WIFI_SSID.name)
        assertEquals("WIFI_PASSWORD", FieldId.WIFI_PASSWORD.name)
        assertEquals("LICENSE_KEY", FieldId.LICENSE_KEY.name)
        assertEquals("SERVER_HOSTNAME", FieldId.SERVER_HOSTNAME.name)
        assertEquals("SERVER_PORT", FieldId.SERVER_PORT.name)
        assertEquals("CUSTOM_LABEL", FieldId.CUSTOM_LABEL.name)
        assertEquals("CUSTOM_TEXT", FieldId.CUSTOM_TEXT.name)
        assertEquals("CUSTOM_NUMBER", FieldId.CUSTOM_NUMBER.name)
        assertEquals("CUSTOM_URL", FieldId.CUSTOM_URL.name)
        assertEquals("CUSTOM_DATE", FieldId.CUSTOM_DATE.name)
        assertEquals("CUSTOM_EMAIL", FieldId.CUSTOM_EMAIL.name)
        assertEquals("BANK_NAME", FieldId.BANK_NAME.name)
        assertEquals("ACCOUNT_HOLDER", FieldId.ACCOUNT_HOLDER.name)
        assertEquals("BRANCH", FieldId.BRANCH.name)
        assertEquals("CURRENCY", FieldId.CURRENCY.name)
        assertEquals("PAYPAL_EMAIL", FieldId.PAYPAL_EMAIL.name)
        assertEquals("MERCHANT_ID", FieldId.MERCHANT_ID.name)
        assertEquals("CUSTOMER_ID", FieldId.CUSTOMER_ID.name)
        assertEquals("BENEFICIARY", FieldId.BENEFICIARY.name)
        assertEquals("REFERENCE", FieldId.REFERENCE.name)
        assertEquals("PASSPORT_TYPE", FieldId.PASSPORT_TYPE.name)
        assertEquals("ISSUING_COUNTRY", FieldId.ISSUING_COUNTRY.name)
        assertEquals("NATIONALITY", FieldId.NATIONALITY.name)
        assertEquals("RESIDENCE_PERMIT_NUMBER", FieldId.RESIDENCE_PERMIT_NUMBER.name)
        assertEquals("RESIDENCE_PERMIT_TYPE", FieldId.RESIDENCE_PERMIT_TYPE.name)
        assertEquals("RESIDENCE_PERMIT_EXPIRY", FieldId.RESIDENCE_PERMIT_EXPIRY.name)
        assertEquals("LICENSE_CLASS", FieldId.LICENSE_CLASS.name)
        assertEquals("HEALTH_INSURANCE_ID", FieldId.HEALTH_INSURANCE_ID.name)
        assertEquals("HEALTH_INSURANCE_PROVIDER", FieldId.HEALTH_INSURANCE_PROVIDER.name)
        assertEquals("HEALTH_INSURANCE_GROUP_NUMBER", FieldId.HEALTH_INSURANCE_GROUP_NUMBER.name)
        assertEquals("POLICY_NUMBER", FieldId.POLICY_NUMBER.name)
        assertEquals("STUDENT_ID_NUMBER", FieldId.STUDENT_ID_NUMBER.name)
        assertEquals("INSTITUTION_NAME", FieldId.INSTITUTION_NAME.name)
    }

    @Test
    fun `all FieldId values map to a FieldDefinition`() {
        for (fieldId in FieldId.entries) {
            val definition = FieldDefinition.fromId(fieldId)
            assertEquals(fieldId, definition.fieldId)
        }
    }

    @Test
    fun `valueOf roundtrip preserves identity`() {
        for (fieldId in FieldId.entries) {
            val roundtrip = FieldId.valueOf(fieldId.name)
            assertEquals(fieldId, roundtrip)
        }
    }
}
