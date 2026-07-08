package com.passgo.app.core.security

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FakeDataStore : DataStore<Preferences> {
    private val _data = MutableStateFlow<Preferences>(emptyPreferences())
    override val data: Flow<Preferences> = _data
    override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences {
        val current = _data.value
        val newValue = transform(current)
        _data.value = newValue
        return newValue
    }
}

class SecurityEventLoggerTest {

    private lateinit var fakeDataStore: FakeDataStore
    private lateinit var logger: SecurityEventLogger

    @BeforeEach
    fun setup() {
        fakeDataStore = FakeDataStore()
        logger = SecurityEventLogger(mockk<Context>())
        logger.setTestDataStore(fakeDataStore)
    }

    @Test
    fun `recentEvents returns empty list initially`() = runTest {
        val events = logger.recentEvents.first()
        assertTrue(events.isEmpty())
    }

    @Test
    fun `logEvent stores UNLOCK_SUCCESS`() = runTest {
        logger.logEvent(EventType.UNLOCK_SUCCESS)
        val events = logger.recentEvents.first()
        assertEquals(1, events.size)
        assertEquals(EventType.UNLOCK_SUCCESS, events[0].type)
        assertTrue(events[0].timestamp > 0)
    }

    @Test
    fun `logEvent stores UNLOCK_FAILED`() = runTest {
        logger.logEvent(EventType.UNLOCK_FAILED)
        val events = logger.recentEvents.first()
        assertEquals(1, events.size)
        assertEquals(EventType.UNLOCK_FAILED, events[0].type)
    }

    @Test
    fun `logEvent stores LOCKOUT_STARTED`() = runTest {
        logger.logEvent(EventType.LOCKOUT_STARTED)
        val events = logger.recentEvents.first()
        assertEquals(1, events.size)
        assertEquals(EventType.LOCKOUT_STARTED, events[0].type)
    }

    @Test
    fun `multiple events are stored in order`() = runTest {
        logger.logEvent(EventType.UNLOCK_SUCCESS)
        logger.logEvent(EventType.UNLOCK_FAILED)
        logger.logEvent(EventType.LOCKOUT_STARTED)

        val events = logger.recentEvents.first()
        assertEquals(3, events.size)
        assertEquals(EventType.UNLOCK_SUCCESS, events[0].type)
        assertEquals(EventType.UNLOCK_FAILED, events[1].type)
        assertEquals(EventType.LOCKOUT_STARTED, events[2].type)

        assertTrue(events[2].timestamp >= events[1].timestamp)
        assertTrue(events[1].timestamp >= events[0].timestamp)
    }

    @Test
    fun `events have valid timestamps`() = runTest {
        logger.logEvent(EventType.UNLOCK_SUCCESS)

        val events = logger.recentEvents.first()
        val now = System.currentTimeMillis()
        assertTrue(events[0].timestamp <= now)
        assertTrue(events[0].timestamp > now - 5000)
    }

    @Test
    fun `events persist across logEvent calls`() = runTest {
        repeat(10) {
            logger.logEvent(EventType.UNLOCK_SUCCESS)
        }

        val events = logger.recentEvents.first()
        assertEquals(10, events.size)
    }

    @Test
    fun `does not exceed max event limit`() = runTest {
        repeat(60) {
            logger.logEvent(EventType.UNLOCK_FAILED)
        }

        val events = logger.recentEvents.first()
        assertEquals(50, events.size)
    }

    @Test
    fun `keeps most recent events when over limit`() = runTest {
        repeat(55) { i ->
            if (i < 5) {
                logger.logEvent(EventType.UNLOCK_SUCCESS)
            } else {
                logger.logEvent(EventType.UNLOCK_FAILED)
            }
        }

        val events = logger.recentEvents.first()
        assertEquals(50, events.size)
        val unlockCount = events.count { it.type == EventType.UNLOCK_FAILED }
        assertEquals(50, unlockCount)
    }
}
