package com.passgo.app.data.session

import com.passgo.app.core.logging.PassGoLogger
import com.passgo.app.core.security.KeyStoreManager
import com.passgo.app.core.security.MasterKeyManager
import com.passgo.app.core.security.MasterPasswordStore
import com.passgo.app.data.settings.FailedAttemptStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val logger: PassGoLogger,
    private val passwordStore: MasterPasswordStore,
    private val failedAttemptStore: FailedAttemptStore,
    private val masterKeyManager: MasterKeyManager,
    private val keyStoreManager: KeyStoreManager
) {

    private val _sessionState = MutableStateFlow(
        if (passwordStore.isMasterPasswordSet()) SessionState.LOCKED
        else SessionState.SETUP_REQUIRED
    )
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    private var unlockedSince: Long = 0L
    private var autoLockTimeout: Long = DEFAULT_AUTO_LOCK_MS
    private var autofillSessionUnlock: Boolean = false
    private var autofillAuthAttempted: Boolean = false

    fun isUnlocked(): Boolean = _sessionState.value == SessionState.UNLOCKED

    fun notifyFailedUnlockAttempt() {
        val attempts = failedAttemptStore.getFailedAttempts() + 1
        val lockedUntil = if (attempts >= MAX_FAILED_ATTEMPTS) {
            System.currentTimeMillis() + LOCKOUT_DURATION_MS
        } else 0L
        failedAttemptStore.recordFailedAttempt(lockedUntil)
    }

    fun notifySuccessfulUnlock() {
        failedAttemptStore.resetAttempts()
    }

    fun isLockedOut(): Boolean = failedAttemptStore.isLockedOut()

    fun remainingLockoutTime(): Long = failedAttemptStore.getRemainingLockoutMs()

    fun unlock() {
        unlockedSince = System.currentTimeMillis()
        _sessionState.value = SessionState.UNLOCKED
        autofillSessionUnlock = false
        logger.info("SessionManager", "Session unlocked")
    }

    fun tempUnlockForAutofill() {
        unlockedSince = System.currentTimeMillis()
        _sessionState.value = SessionState.UNLOCKED
        autofillSessionUnlock = true
        autofillAuthAttempted = true
        logger.info("SessionManager", "Temporary autofill unlock")
    }

    fun markAutofillAuthAttempted() {
        autofillAuthAttempted = true
    }

    fun hasAutofillAuthBeenAttempted(): Boolean = autofillAuthAttempted

    fun lock() {
        _sessionState.value = SessionState.LOCKED
        unlockedSince = 0L
        autofillSessionUnlock = false
        masterKeyManager.clearOnLock()
        keyStoreManager.clearCache()
    }

    fun lockIfAutofillOnly() {
        if (autofillSessionUnlock) {
            lock()
        }
    }

    fun setAutoLockTimeout(seconds: Int) {
        autoLockTimeout = seconds * 1000L
    }

    fun checkAndLockIfExpired(): Boolean {
        if (_sessionState.value != SessionState.UNLOCKED) return false
        val elapsed = System.currentTimeMillis() - unlockedSince
        if (elapsed >= autoLockTimeout) {
            lock()
            return true
        }
        return false
    }

    enum class SessionState {
        LOCKED,
        UNLOCKED,
        SETUP_REQUIRED
    }

    companion object {
        private const val DEFAULT_AUTO_LOCK_MS = 300_000L
        const val MAX_FAILED_ATTEMPTS = 5
        const val LOCKOUT_DURATION_MS = 30_000L
    }
}
