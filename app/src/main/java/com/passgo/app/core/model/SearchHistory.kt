package com.passgo.app.core.model

data class SearchHistory(
    val id: String,
    val vaultId: String,
    val query: String,
    val createdAt: Long
)
