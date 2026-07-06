package com.passgo.app.feature.autofill.model

data class AutofillCredential(
    val id: String,
    val name: String,
    val username: String,
    val email: String,
    val password: String,
    val url: String = "",
    val favorite: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
)
