package com.passgo.app.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.passgo.app.core.logging.PassGoLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "passgo_preferences")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logger: PassGoLogger
) {

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        val name = prefs[THEME_KEY] ?: ThemeMode.SYSTEM.name
        try {
            ThemeMode.valueOf(name)
        } catch (_: IllegalArgumentException) {
            ThemeMode.SYSTEM
        }
    }

    val autoLockSeconds: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[AUTO_LOCK_KEY] ?: DEFAULT_AUTO_LOCK_SECONDS
    }

    val languageCode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[LANGUAGE_KEY] ?: "en"
    }

    val securityTipsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[SECURITY_TIPS_KEY] ?: true
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[THEME_KEY] = mode.name
        }
    }

    suspend fun setAutoLockSeconds(seconds: Int) {
        context.dataStore.edit { prefs ->
            prefs[AUTO_LOCK_KEY] = seconds
        }
    }

    suspend fun setLanguageCode(code: String) {
        context.dataStore.edit { prefs ->
            prefs[LANGUAGE_KEY] = code
        }
    }

    suspend fun setSecurityTipsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[SECURITY_TIPS_KEY] = enabled
        }
    }

    val clipboardClearEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[CLIPBOARD_CLEAR_KEY] ?: true
    }

    val clipboardClearDelayMs: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[CLIPBOARD_DELAY_KEY] ?: DEFAULT_CLIPBOARD_DELAY_MS
    }

    suspend fun setClipboardClearEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[CLIPBOARD_CLEAR_KEY] = enabled
        }
    }

    suspend fun setClipboardClearDelayMs(delayMs: Long) {
        context.dataStore.edit { prefs ->
            prefs[CLIPBOARD_DELAY_KEY] = delayMs
        }
    }

    companion object {
        private val THEME_KEY = stringPreferencesKey("theme_mode")
        private val AUTO_LOCK_KEY = intPreferencesKey("auto_lock_seconds")
        private val LANGUAGE_KEY = stringPreferencesKey("language_code")
        private val SECURITY_TIPS_KEY = booleanPreferencesKey("security_tips_enabled")
        private val CLIPBOARD_CLEAR_KEY = booleanPreferencesKey("clipboard_clear_enabled")
        private val CLIPBOARD_DELAY_KEY = longPreferencesKey("clipboard_clear_delay_ms")
        private const val DEFAULT_AUTO_LOCK_SECONDS = 300
        private const val DEFAULT_CLIPBOARD_DELAY_MS = 30000L
    }
}
