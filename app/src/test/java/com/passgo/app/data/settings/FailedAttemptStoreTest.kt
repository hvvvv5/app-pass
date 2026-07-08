package com.passgo.app.data.settings

import android.content.Context
import android.content.SharedPreferences
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FailedAttemptStoreTest {

    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor
    private lateinit var store: FailedAttemptStore

    private val KEY_ATTEMPTS = "failed_attempts"
    private val KEY_LOCKED_UNTIL = "locked_until_ms"

    private var attemptsValue = 0
    private var lockedUntilValue = 0L

    @BeforeEach
    fun setup() {
        attemptsValue = 0
        lockedUntilValue = 0L

        mockEditor = mockk(relaxed = true)
        mockPrefs = mockk()

        every { mockPrefs.edit() } returns mockEditor
        every { mockPrefs.getInt(KEY_ATTEMPTS, 0) } answers { attemptsValue }
        every { mockPrefs.getLong(KEY_LOCKED_UNTIL, 0L) } answers { lockedUntilValue }

        every { mockEditor.putInt(KEY_ATTEMPTS, any()) } answers {
            attemptsValue = secondArg()
            mockEditor
        }
        every { mockEditor.putLong(KEY_LOCKED_UNTIL, any()) } answers {
            lockedUntilValue = secondArg()
            mockEditor
        }

        store = FailedAttemptStore(mockk<Context>())
        store.setTestPrefs(mockPrefs)
    }

    @Test
    fun `getFailedAttempts returns zero initially`() {
        assertEquals(0, store.getFailedAttempts())
    }

    @Test
    fun `recordFailedAttempt increments counter`() {
        store.recordFailedAttempt(0L)
        assertEquals(1, store.getFailedAttempts())

        store.recordFailedAttempt(0L)
        assertEquals(2, store.getFailedAttempts())
    }

    @Test
    fun `recordFailedAttempt stores lockout timestamp`() {
        val lockUntil = System.currentTimeMillis() + 30000L
        store.recordFailedAttempt(lockUntil)
        assertEquals(lockUntil, lockedUntilValue)
    }

    @Test
    fun `resetAttempts clears counter and lockout timestamp`() {
        store.recordFailedAttempt(System.currentTimeMillis() + 30000L)
        assertEquals(1, store.getFailedAttempts())

        store.resetAttempts()
        assertEquals(0, store.getFailedAttempts())
        assertEquals(0L, lockedUntilValue)
    }

    @Test
    fun `isLockedOut returns false when lockout timestamp is zero`() {
        assertFalse(store.isLockedOut())
    }

    @Test
    fun `isLockedOut returns true when within lockout period`() {
        lockedUntilValue = Long.MAX_VALUE
        store.setTestPrefs(mockPrefs)
        assertTrue(store.isLockedOut())
    }

    @Test
    fun `isLockedOut returns false and resets when lockout expired`() {
        lockedUntilValue = 1L
        store.setTestPrefs(mockPrefs)

        assertFalse(store.isLockedOut())
        verify { mockEditor.putInt(KEY_ATTEMPTS, 0) }
        verify { mockEditor.putLong(KEY_LOCKED_UNTIL, 0L) }
        assertEquals(0, attemptsValue)
        assertEquals(0L, lockedUntilValue)
    }

    @Test
    fun `getRemainingLockoutMs returns zero when not locked out`() {
        assertEquals(0L, store.getRemainingLockoutMs())
    }

    @Test
    fun `getRemainingLockoutMs returns positive when within lockout`() {
        val future = System.currentTimeMillis() + 30000L
        lockedUntilValue = future
        store.setTestPrefs(mockPrefs)

        val remaining = store.getRemainingLockoutMs()
        assertTrue(remaining > 0)
        assertTrue(remaining <= 30000L)
    }

    @Test
    fun `getRemainingLockoutMs returns zero and resets when expired`() {
        lockedUntilValue = 1L
        store.setTestPrefs(mockPrefs)

        assertEquals(0L, store.getRemainingLockoutMs())
        verify { mockEditor.putInt(KEY_ATTEMPTS, 0) }
        verify { mockEditor.putLong(KEY_LOCKED_UNTIL, 0L) }
    }

    @Test
    fun `multiple recordFailedAttempt increments correctly`() {
        repeat(5) { store.recordFailedAttempt(0L) }
        assertEquals(5, store.getFailedAttempts())
    }

    @Test
    fun `lockout state persists across calls`() {
        lockedUntilValue = Long.MAX_VALUE
        store.setTestPrefs(mockPrefs)

        for (i in 1..5) {
            assertTrue(store.isLockedOut())
        }
    }
}
