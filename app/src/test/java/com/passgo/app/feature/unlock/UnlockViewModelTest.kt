package com.passgo.app.feature.unlock

import com.passgo.app.core.logging.PassGoLogger
import com.passgo.app.core.security.EventType
import com.passgo.app.core.security.MasterPasswordStore
import com.passgo.app.core.security.PasswordHasher
import com.passgo.app.core.security.SecurityEventLogger
import com.passgo.app.data.session.SessionManager
import com.passgo.app.data.settings.FailedAttemptStore
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UnlockViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var passwordHasher: PasswordHasher
    private lateinit var passwordStore: MasterPasswordStore
    private lateinit var sessionManager: SessionManager
    private lateinit var failedAttemptStore: FailedAttemptStore
    private lateinit var securityEventLogger: SecurityEventLogger
    private lateinit var logger: PassGoLogger
    private lateinit var viewModel: UnlockViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        passwordHasher = mockk()
        passwordStore = mockk()
        sessionManager = mockk()
        failedAttemptStore = mockk()
        securityEventLogger = mockk()
        logger = mockk(relaxed = true)

        every { sessionManager.isLockedOut() } returns false
        every { sessionManager.remainingLockoutTime() } returns 0L
        coEvery { securityEventLogger.logEvent(any()) } just Runs
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): UnlockViewModel {
        return UnlockViewModel(
            passwordHasher = passwordHasher,
            passwordStore = passwordStore,
            sessionManager = sessionManager,
            failedAttemptStore = failedAttemptStore,
            securityEventLogger = securityEventLogger,
            logger = logger
        )
    }

    @Test
    fun `initial state has empty password no error and no lockout`() {
        viewModel = createViewModel()

        assertEquals("", viewModel.password.value)
        assertNull(viewModel.error.value)
        assertEquals(0, viewModel.lockoutSecondsRemaining.value)
    }

    @Test
    fun `onPasswordChanged updates password value`() {
        viewModel = createViewModel()
        viewModel.onPasswordChanged("testpassword")
        assertEquals("testpassword", viewModel.password.value)
    }

    @Test
    fun `onPasswordChanged clears error when not locked out`() {
        viewModel = createViewModel()
        viewModel.onPasswordChanged("newpassword")
        assertNull(viewModel.error.value)
    }

    @Test
    fun `onPasswordChanged preserves error when locked out`() = runTest(testDispatcher) {
        every { sessionManager.isLockedOut() } returns true
        every { sessionManager.remainingLockoutTime() } returnsMany listOf(30000L, 0L)

        viewModel = createViewModel()
        viewModel.unlock()

        assertTrue(viewModel.error.value?.startsWith("Too many attempts") == true)

        viewModel.onPasswordChanged("newpassword")
        assertTrue(viewModel.error.value?.startsWith("Too many attempts") == true)
    }

    @Test
    fun `unlock with empty password shows error`() {
        viewModel = createViewModel()
        viewModel.unlock()

        assertEquals("Password cannot be empty", viewModel.error.value)
    }

    @Test
    fun `successful unlock calls success handlers and emits true`() = runTest(testDispatcher) {
        every { passwordStore.loadHash() } returns "hash".toByteArray()
        every { passwordStore.loadSalt() } returns "salt".toByteArray()
        every { passwordHasher.verifyPassword(any<CharArray>(), any(), any()) } returns true
        every { sessionManager.notifySuccessfulUnlock() } just Runs
        every { sessionManager.unlock() } just Runs

        viewModel = createViewModel()
        viewModel.onPasswordChanged("correct")

        val results = mutableListOf<Boolean>()
        val job = launch { viewModel.isUnlocked.collect { results.add(it) } }

        viewModel.unlock()

        verify { sessionManager.notifySuccessfulUnlock() }
        verify { sessionManager.unlock() }
        coVerify { securityEventLogger.logEvent(EventType.UNLOCK_SUCCESS) }
        assertNull(viewModel.error.value)
        assertEquals(true, results.last())
        job.cancel()
    }

    @Test
    fun `failed unlock shows remaining attempts and emits false`() = runTest(testDispatcher) {
        every { passwordStore.loadHash() } returns "hash".toByteArray()
        every { passwordStore.loadSalt() } returns "salt".toByteArray()
        every { passwordHasher.verifyPassword(any<CharArray>(), any(), any()) } returns false
        every { sessionManager.notifyFailedUnlockAttempt() } just Runs
        every { failedAttemptStore.getFailedAttempts() } returns 2

        viewModel = createViewModel()
        viewModel.onPasswordChanged("wrong")

        val results = mutableListOf<Boolean>()
        val job = launch { viewModel.isUnlocked.collect { results.add(it) } }

        viewModel.unlock()

        verify { sessionManager.notifyFailedUnlockAttempt() }
        coVerify { securityEventLogger.logEvent(EventType.UNLOCK_FAILED) }
        assertEquals("Invalid password (3 attempts remaining)", viewModel.error.value)
        assertEquals(false, results.last())
        job.cancel()
    }

    @Test
    fun `fifth failed attempt triggers lockout`() = runTest(testDispatcher) {
        every { sessionManager.isLockedOut() } returnsMany listOf(false, false, true)
        every { passwordStore.loadHash() } returns "hash".toByteArray()
        every { passwordStore.loadSalt() } returns "salt".toByteArray()
        every { passwordHasher.verifyPassword(any<CharArray>(), any(), any()) } returns false
        every { sessionManager.notifyFailedUnlockAttempt() } just Runs
        every { sessionManager.remainingLockoutTime() } returnsMany listOf(30000L, 0L)

        viewModel = createViewModel()
        viewModel.onPasswordChanged("wrong")

        val results = mutableListOf<Boolean>()
        val job = launch { viewModel.isUnlocked.collect { results.add(it) } }

        viewModel.unlock()

        verify { sessionManager.notifyFailedUnlockAttempt() }
        coVerify { securityEventLogger.logEvent(EventType.UNLOCK_FAILED) }
        coVerify { securityEventLogger.logEvent(EventType.LOCKOUT_STARTED) }
        assertTrue(viewModel.error.value?.startsWith("Too many attempts") == true)
        assertTrue(viewModel.lockoutSecondsRemaining.value > 0)
        assertEquals(false, results.last())
        job.cancel()
    }

    @Test
    fun `unlock when already locked out shows lockout without verification`() = runTest(testDispatcher) {
        every { sessionManager.isLockedOut() } returns true
        every { sessionManager.remainingLockoutTime() } returnsMany listOf(30000L, 0L)

        viewModel = createViewModel()
        viewModel.onPasswordChanged("wrong")

        viewModel.unlock()

        verify(exactly = 0) { passwordHasher.verifyPassword(any<CharArray>(), any(), any()) }
        coVerify(inverse = true) { securityEventLogger.logEvent(EventType.UNLOCK_FAILED) }
        coVerify(inverse = true) { securityEventLogger.logEvent(EventType.LOCKOUT_STARTED) }
        assertTrue(viewModel.error.value?.startsWith("Too many attempts") == true)
        assertTrue(viewModel.lockoutSecondsRemaining.value > 0)
    }

    @Test
    fun `successful unlock does not log failure or lockout events`() = runTest(testDispatcher) {
        every { passwordStore.loadHash() } returns "hash".toByteArray()
        every { passwordStore.loadSalt() } returns "salt".toByteArray()
        every { passwordHasher.verifyPassword(any<CharArray>(), any(), any()) } returns true
        every { sessionManager.notifySuccessfulUnlock() } just Runs
        every { sessionManager.unlock() } just Runs

        viewModel = createViewModel()
        viewModel.onPasswordChanged("correct")

        viewModel.unlock()

        coVerify(inverse = true) { securityEventLogger.logEvent(EventType.UNLOCK_FAILED) }
        coVerify(inverse = true) { securityEventLogger.logEvent(EventType.LOCKOUT_STARTED) }
    }

    @Test
    fun `unlock with exception clears password and emits false`() = runTest(testDispatcher) {
        every { passwordStore.loadHash() } throws RuntimeException("DB error")
        every { passwordHasher.clearPassword(any<CharArray>()) } just Runs

        viewModel = createViewModel()
        viewModel.onPasswordChanged("mypassword")

        val results = mutableListOf<Boolean>()
        val job = launch { viewModel.isUnlocked.collect { results.add(it) } }

        viewModel.unlock()

        assertNotNull(viewModel.error.value)
        assertEquals(false, results.last())
        job.cancel()
    }
}
