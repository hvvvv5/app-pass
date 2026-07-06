package com.passgo.app.feature.autofill.model

enum class SessionState {
    CREATED,
    PARSING,
    PARSED,
    RESPONDING,
    RESPONDED,
    CANCELLED,
    FINISHED
}
