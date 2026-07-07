package com.passgo.app.core.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CustomFieldTest {

    @Test
    fun `definition returns matching FieldDefinition`() {
        val field = CustomField(
            id = "f1",
            itemId = "item1",
            fieldId = FieldId.CREDIT_CARD_NUMBER,
            value = "4111111111111111",
            sortOrder = 0
        )
        assertTrue(field.definition is FieldDefinition.CreditCardNumber)
    }

    @Test
    fun `formattedValue applies definition format`() {
        val field = CustomField(
            id = "f1",
            itemId = "item1",
            fieldId = FieldId.CREDIT_CARD_NUMBER,
            value = "4111111111111111",
            sortOrder = 0
        )
        assertEquals("4111 1111 1111 1111", field.formattedValue)
    }

    @Test
    fun `formattedValue returns raw value for simple fields`() {
        val field = CustomField(
            id = "f1",
            itemId = "item1",
            fieldId = FieldId.WIFI_SSID,
            value = "HomeNetwork",
            sortOrder = 0
        )
        assertEquals("HomeNetwork", field.formattedValue)
    }
}
