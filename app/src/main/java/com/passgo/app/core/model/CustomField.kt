package com.passgo.app.core.model

data class CustomField(
    val id: String,
    val itemId: String,
    val fieldId: FieldId,
    val value: String,
    val sortOrder: Int
) {
    val definition: FieldDefinition get() = FieldDefinition.fromId(fieldId)

    val formattedValue: String get() = definition.format(value)
}
