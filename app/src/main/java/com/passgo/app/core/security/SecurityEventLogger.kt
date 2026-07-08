package com.passgo.app.core.security

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.eventDataStore by preferencesDataStore(name = "passgo_security_events")

enum class EventType {
    UNLOCK_SUCCESS,
    UNLOCK_FAILED,
    LOCKOUT_STARTED
}

data class SecurityEvent(
    val type: EventType,
    val timestamp: Long
)

@Singleton
class SecurityEventLogger @Inject constructor(
    @ApplicationContext private val context: Context
) {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal var dataStore: DataStore<Preferences>? = null
        private set

    private fun getDataStore(): DataStore<Preferences> {
        if (dataStore == null) {
            dataStore = context.eventDataStore
        }
        return dataStore!!
    }

    @VisibleForTesting
    internal fun setTestDataStore(testDataStore: DataStore<Preferences>) {
        dataStore = testDataStore
    }

    suspend fun logEvent(type: EventType) {
        getDataStore().edit { prefs ->
            val existing = prefs[EVENTS_KEY] ?: ""
            val entry = "${type.name},${System.currentTimeMillis()}"
            val updated = if (existing.isEmpty()) entry else "$existing;$entry"
            val trimmed = updated.split(";").takeLast(MAX_EVENTS).joinToString(";")
            prefs[EVENTS_KEY] = trimmed
        }
    }

    val recentEvents: Flow<List<SecurityEvent>>
        get() = getDataStore().data.map { prefs ->
            val raw = prefs[EVENTS_KEY] ?: ""
            if (raw.isEmpty()) emptyList()
            else raw.split(";").mapNotNull { part ->
                val parts = part.split(",", limit = 2)
                if (parts.size == 2) {
                    try {
                        SecurityEvent(EventType.valueOf(parts[0]), parts[1].toLong())
                    } catch (_: Exception) {
                        null
                    }
                } else null
            }
        }

    companion object {
        private val EVENTS_KEY = stringPreferencesKey("security_events")
        private const val MAX_EVENTS = 50
    }
}
