package com.passgo.app.feature.unlock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passgo.app.core.logging.PassGoLogger
import com.passgo.app.core.security.EventType
import com.passgo.app.core.security.MasterPasswordStore
import com.passgo.app.core.security.PasswordHasher
import com.passgo.app.core.security.SecurityEventLogger
import com.passgo.app.data.session.SessionManager
import com.passgo.app.data.settings.FailedAttemptStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UnlockViewModel @Inject constructor(
    private val passwordHasher: PasswordHasher,
    private val passwordStore: MasterPasswordStore,
    private val sessionManager: SessionManager,
    private val failedAttemptStore: FailedAttemptStore,
    private val securityEventLogger: SecurityEventLogger,
    private val logger: PassGoLogger
) : ViewModel() {

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isUnlocked = MutableSharedFlow<Boolean>()
    val isUnlocked: SharedFlow<Boolean> = _isUnlocked.asSharedFlow()

    private val _lockoutSecondsRemaining = MutableStateFlow(0)
    val lockoutSecondsRemaining: StateFlow<Int> = _lockoutSecondsRemaining.asStateFlow()

    private var lockoutJob: Job? = null

    fun onPasswordChanged(value: String) {
        _password.value = value
        if (!sessionManager.isLockedOut()) {
            _error.value = null
        }
    }

    fun unlock() {
        if (sessionManager.isLockedOut()) {
            showLockoutState()
            startLockoutCountdown()
            return
        }

        val passwordChars = _password.value.toCharArray()
        if (passwordChars.isEmpty()) {
            _error.value = "Password cannot be empty"
            return
        }

        viewModelScope.launch {
            try {
                val storedHash = passwordStore.loadHash()
                val storedSalt = passwordStore.loadSalt()

                if (storedHash == null || storedSalt == null) {
                    _error.value = "Vault not initialized"
                    passwordHasher.clearPassword(passwordChars)
                    return@launch
                }

                val isValid = passwordHasher.verifyPassword(passwordChars, storedSalt, storedHash)
                if (isValid) {
                    sessionManager.notifySuccessfulUnlock()
                    sessionManager.unlock()
                    securityEventLogger.logEvent(EventType.UNLOCK_SUCCESS)
                    logger.info("UnlockViewModel", "Vault unlocked successfully")
                    _isUnlocked.emit(true)
                } else {
                    sessionManager.notifyFailedUnlockAttempt()
                    securityEventLogger.logEvent(EventType.UNLOCK_FAILED)
                    if (sessionManager.isLockedOut()) {
                        securityEventLogger.logEvent(EventType.LOCKOUT_STARTED)
                        showLockoutState()
                        startLockoutCountdown()
                    } else {
                        val remaining = SessionManager.MAX_FAILED_ATTEMPTS - failedAttemptStore.getFailedAttempts()
                        _error.value = "Invalid password ($remaining attempt${if (remaining != 1) "s" else ""} remaining)"
                    }
                    logger.info("UnlockViewModel", "Failed unlock attempt")
                    _isUnlocked.emit(false)
                }
            } catch (e: Exception) {
                logger.error("UnlockViewModel", "Failed to unlock vault: ${e.message}")
                _error.value = "Unlock failed"
                passwordHasher.clearPassword(passwordChars)
                _isUnlocked.emit(false)
            }
        }
    }

    private fun showLockoutState() {
        val remainingMs = sessionManager.remainingLockoutTime()
        val seconds = (remainingMs / 1000 + 1).toInt()
        _lockoutSecondsRemaining.value = seconds
        _error.value = "Too many attempts. Try again in ${seconds}s."
    }

    private fun startLockoutCountdown() {
        lockoutJob?.cancel()
        if (_lockoutSecondsRemaining.value <= 0) return
        lockoutJob = viewModelScope.launch {
            while (_lockoutSecondsRemaining.value > 0) {
                delay(1000)
                val remainingMs = sessionManager.remainingLockoutTime()
                val seconds = (remainingMs / 1000).toInt()
                _lockoutSecondsRemaining.value = seconds
                if (seconds > 0) {
                    _error.value = "Too many attempts. Try again in ${seconds}s."
                } else {
                    _error.value = null
                    break
                }
            }
        }
    }

    override fun onCleared() {
        lockoutJob?.cancel()
        super.onCleared()
        _password.value.toCharArray().fill('\u0000')
    }
}
