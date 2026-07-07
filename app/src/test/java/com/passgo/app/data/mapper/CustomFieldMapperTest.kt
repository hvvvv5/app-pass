package com.passgo.app.data.mapper

import com.passgo.app.core.database.entity.CustomFieldEntity
import com.passgo.app.core.model.CustomField
import com.passgo.app.core.model.FieldId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CustomFieldMapperTest {

    @Test
    fun `entity to domain maps all fields`() {
        val entity = CustomFieldEntity(
            id = "f1",
            itemId = "item1",
            fieldId = FieldId.CREDIT_CARD_NUMBER.name,
            value = "4111111111111111",
            sortOrder = 0
        )
        val domain = entity.toDomain()
        assertEquals("f1", domain.id)
        assertEquals("item1", domain.itemId)
        assertEquals(FieldId.CREDIT_CARD_NUMBER, domain.fieldId)
        assertEquals("4111111111111111", domain.value)
        assertEquals(0, domain.sortOrder)
    }

    @Test
    fun `domain to entity maps all fields`() {
        val domain = CustomField(
            id = "f1",
            itemId = "item1",
            fieldId = FieldId.IBAN,
            value = "GB33BUKB20201555555555",
            sortOrder = 1
        )
        val entity = domain.toEntity()
        assertEquals("f1", entity.id)
        assertEquals("item1", entity.itemId)
        assertEquals(FieldId.IBAN.name, entity.fieldId)
        assertEquals("GB33BUKB20201555555555", entity.value)
        assertEquals(1, entity.sortOrder)
    }

    @Test
    fun `entity with unknown fieldId falls back to CUSTOM_TEXT`() {
        val entity = CustomFieldEntity(
            id = "f1",
            itemId = "item1",
            fieldId = "UNKNOWN_FIELD",
            value = "test",
            sortOrder = 0
        )
        val domain = entity.toDomain()
        assertEquals(FieldId.CUSTOM_TEXT, domain.fieldId)
    }
}
