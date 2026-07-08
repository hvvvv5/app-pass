package com.passgo.app.core.security

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import com.passgo.app.data.settings.UserPreferences
import io.mockk.clearStaticMockk
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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
class ClipboardGuardTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var mockContext: Context
    private lateinit var mockClipboardManager: ClipboardManager
    private lateinit var clipboardGuard: ClipboardGuard

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockClipboardManager = mockk()
        mockContext = mockk()
        val mockUserPreferences = mockk<UserPreferences>()

        mockkStatic(ClipData::class)
        every { ClipData.newPlainText(any(), any()) } answers {
            val label = firstArg<String>()
            val cd = mockk<ClipData>()
            val desc = mockk<ClipDescription>()
            every { desc.label } returns label
            every { cd.description } returns desc
            cd
        }

        every { mockClipboardManager.primaryClip } returns null
        every { mockContext.getSystemService(Context.CLIPBOARD_SERVICE) } returns mockClipboardManager
        every { mockUserPreferences.clipboardClearEnabled } returns MutableStateFlow(true)
        every { mockUserPreferences.clipboardClearDelayMs } returns MutableStateFlow(15000L)

        clipboardGuard = ClipboardGuard(mockContext, mockUserPreferences)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @AfterEach
    fun tearDown() {
        clearStaticMockk(ClipData::class)
        Dispatchers.resetMain()
    }

    @Test
    fun `init collects clipboard preferences from userPreferences`() = runTest(testDispatcher) {
        assertTrue(clipboardGuard.clipboardClearEnabled)
        assertEquals(15000L, clipboardGuard.clipboardClearDelayMs)
    }

    @Test
    fun `copySensitiveText calls setPrimaryClip and stores token`() = runTest(testDispatcher) {
        every { mockClipboardManager.setPrimaryClip(any()) } returns Unit

        clipboardGuard.copySensitiveText("test-password")

        verify(exactly = 1) { mockClipboardManager.setPrimaryClip(any()) }
        assertNotNull(clipboardGuard.lastCopyToken)
    }

    @Test
    fun `copySensitiveText stores a token`() = runTest(testDispatcher) {
        every { mockClipboardManager.setPrimaryClip(any()) } returns Unit

        clipboardGuard.copySensitiveText("secret")

        assertNotNull(clipboardGuard.lastCopyToken)
    }

    @Test
    fun `copySensitiveText generates new token on each call`() = runTest(testDispatcher) {
        every { mockClipboardManager.setPrimaryClip(any()) } returns Unit

        clipboardGuard.copySensitiveText("first")
        val firstToken = clipboardGuard.lastCopyToken
        clipboardGuard.copySensitiveText("second")
        val secondToken = clipboardGuard.lastCopyToken

        assertNotNull(firstToken)
        assertNotNull(secondToken)
        assertTrue(firstToken != secondToken)
    }

    @Test
    fun `clears clipboard after configured delay when token matches`() = runTest(testDispatcher) {
        every { mockClipboardManager.setPrimaryClip(any()) } returns Unit
        every { mockClipboardManager.primaryClip } answers {
            ClipData.newPlainText(clipboardGuard.lastCopyToken, "password")
        }

        clipboardGuard.copySensitiveText("password")

        advanceTimeBy(14999)
        verify(exactly = 1) { mockClipboardManager.setPrimaryClip(any()) }

        advanceTimeBy(2)
        verify(exactly = 2) { mockClipboardManager.setPrimaryClip(any()) }
    }

    @Test
    fun `does not clear clipboard when token does not match`() = runTest(testDispatcher) {
        every { mockClipboardManager.setPrimaryClip(any()) } returns Unit
        every { mockClipboardManager.primaryClip } returns ClipData.newPlainText("other-app-token", "other")

        clipboardGuard.copySensitiveText("password")

        advanceTimeBy(15001)
        verify(exactly = 1) { mockClipboardManager.setPrimaryClip(any()) }
    }

    @Test
    fun `does not clear clipboard when primaryClip is null`() = runTest(testDispatcher) {
        every { mockClipboardManager.setPrimaryClip(any()) } returns Unit
        every { mockClipboardManager.primaryClip } returns null

        clipboardGuard.copySensitiveText("password")

        advanceTimeBy(15001)
        verify(exactly = 1) { mockClipboardManager.setPrimaryClip(any()) }
    }

    @Test
    fun `re-copy cancels previous timer and only one clear occurs`() = runTest(testDispatcher) {
        every { mockClipboardManager.setPrimaryClip(any()) } returns Unit
        every { mockClipboardManager.primaryClip } answers {
            ClipData.newPlainText(clipboardGuard.lastCopyToken, "dummy")
        }

        clipboardGuard.copySensitiveText("first")
        advanceTimeBy(5000)
        clipboardGuard.copySensitiveText("second")

        advanceTimeBy(15001)
        verify(exactly = 3) { mockClipboardManager.setPrimaryClip(any()) }
    }

    @Test
    fun `does not clear clipboard when disabled`() = runTest(testDispatcher) {
        every { mockClipboardManager.setPrimaryClip(any()) } returns Unit

        clipboardGuard.clipboardClearEnabled = false

        clipboardGuard.copySensitiveText("password")

        advanceTimeBy(15001)
        verify(exactly = 1) { mockClipboardManager.setPrimaryClip(any()) }
    }

    @Test
    fun `cancelPendingClear cancels timer and clears token`() = runTest(testDispatcher) {
        every { mockClipboardManager.setPrimaryClip(any()) } returns Unit
        every { mockClipboardManager.primaryClip } returns ClipData.newPlainText("token", "password")

        clipboardGuard.copySensitiveText("password")
        assertNotNull(clipboardGuard.lastCopyToken)

        clipboardGuard.cancelPendingClear()
        assertNull(clipboardGuard.lastCopyToken)

        advanceTimeBy(15001)
        verify(exactly = 1) { mockClipboardManager.setPrimaryClip(any()) }
    }

    @Test
    fun `uses configured delay from preferences`() = runTest(testDispatcher) {
        every { mockClipboardManager.setPrimaryClip(any()) } returns Unit
        every { mockClipboardManager.primaryClip } answers {
            ClipData.newPlainText(clipboardGuard.lastCopyToken, "pwd")
        }

        clipboardGuard.clipboardClearDelayMs = 60000L

        clipboardGuard.copySensitiveText("pwd")

        advanceTimeBy(59999)
        verify(exactly = 1) { mockClipboardManager.setPrimaryClip(any()) }

        advanceTimeBy(2)
        verify(exactly = 2) { mockClipboardManager.setPrimaryClip(any()) }
    }
}
