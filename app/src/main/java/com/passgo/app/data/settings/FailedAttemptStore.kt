package com.passgo.app.data.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FailedAttemptStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal var prefs: SharedPreferences? = null
        private set

    private fun getPrefs(): SharedPreferences {
        if (prefs == null) {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            prefs = EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
        return prefs!!
    }

    @VisibleForTesting
    internal fun setTestPrefs(testPrefs: SharedPreferences) {
        prefs = testPrefs
    }

    fun recordFailedAttempt(lockedUntilMs: Long) {
        getPrefs().edit()
            .putInt(KEY_ATTEMPTS, getFailedAttempts() + 1)
            .putLong(KEY_LOCKED_UNTIL, lockedUntilMs)
            .apply()
    }

    fun resetAttempts() {
        getPrefs().edit()
            .putInt(KEY_ATTEMPTS, 0)
            .putLong(KEY_LOCKED_UNTIL, 0)
            .apply()
    }

    fun getFailedAttempts(): Int = getPrefs().getInt(KEY_ATTEMPTS, 0)

    fun isLockedOut(): Boolean {
        val lockedUntil = getPrefs().getLong(KEY_LOCKED_UNTIL, 0)
        if (lockedUntil == 0L) return false
        if (System.currentTimeMillis() >= lockedUntil) {
            resetAttempts()
            return false
        }
        return true
    }

    fun getRemainingLockoutMs(): Long {
        val lockedUntil = getPrefs().getLong(KEY_LOCKED_UNTIL, 0)
        if (lockedUntil == 0L) return 0L
        val remaining = lockedUntil - System.currentTimeMillis()
        if (remaining <= 0) {
            resetAttempts()
            return 0L
        }
        return remaining
    }

    companion object {
        private const val PREFS_NAME = "passgo_failed_attempts"
        private const val KEY_ATTEMPTS = "failed_attempts"
        private const val KEY_LOCKED_UNTIL = "locked_until_ms"
    }
}
