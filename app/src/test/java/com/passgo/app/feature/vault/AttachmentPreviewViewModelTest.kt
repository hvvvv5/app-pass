package com.passgo.app.feature.vault

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.passgo.app.core.error.AppException
import com.passgo.app.core.error.AppResult
import com.passgo.app.core.logging.PassGoLogger
import com.passgo.app.core.model.Attachment
import com.passgo.app.data.repository.AttachmentRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AttachmentPreviewViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var attachmentRepository: AttachmentRepository
    private lateinit var passGoLogger: PassGoLogger
    private lateinit var context: Context
    private lateinit var viewModel: AttachmentPreviewViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        attachmentRepository = mockk()
        passGoLogger = mockk(relaxed = true)
        context = mockk(relaxed = true)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() {
        coEvery { attachmentRepository.getAttachmentById(any()) } returns flowOf(
            Attachment(id = "a1", itemId = "i1", name = "test")
        )

        viewModel = AttachmentPreviewViewModel(attachmentRepository, passGoLogger, context)

        assertTrue(viewModel.state.value is PreviewUiState.Loading)
    }

    @Test
    fun `image attachment transitions to ImageContent`() = runTest(testDispatcher) {
        val attachment = Attachment(id = "a1", itemId = "i1", name = "photo.png", mimeType = "image/png")
        val previewUri = mockk<Uri>()
        coEvery { attachmentRepository.getAttachmentById("a1") } returns flowOf(attachment)
        coEvery { attachmentRepository.getAttachmentPreviewUri(attachment) } returns AppResult.Success(previewUri)

        viewModel = AttachmentPreviewViewModel(attachmentRepository, passGoLogger, context)

        val job = launch { viewModel.state.collect { } }
        viewModel.loadAttachment("a1")
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is PreviewUiState.ImageContent, "Expected ImageContent but got ${state::class.simpleName}")
        assertEquals("photo.png", (state as PreviewUiState.ImageContent).attachment.name)
        assertEquals(previewUri, state.uri)
        job.cancel()
    }

    @Test
    fun `text attachment transitions to TextContent`() = runTest(testDispatcher) {
        val attachment = Attachment(id = "a2", itemId = "i1", name = "notes.txt", mimeType = "text/plain")
        val textBytes = "Hello World".toByteArray()
        coEvery { attachmentRepository.getAttachmentById("a2") } returns flowOf(attachment)
        coEvery { attachmentRepository.getAttachmentFile(attachment) } returns AppResult.Success(textBytes)

        viewModel = AttachmentPreviewViewModel(attachmentRepository, passGoLogger, context)

        viewModel.loadAttachment("a2")
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is PreviewUiState.TextContent, "Expected TextContent but got ${state::class.simpleName}")
        assertEquals("Hello World", (state as PreviewUiState.TextContent).content)
        assertEquals("notes.txt", state.attachment.name)
    }

    @Test
    fun `pdf attachment transitions to ExternalContent`() = runTest(testDispatcher) {
        val attachment = Attachment(id = "a3", itemId = "i1", name = "doc.pdf", mimeType = "application/pdf")
        val previewUri = mockk<Uri>()
        coEvery { attachmentRepository.getAttachmentById("a3") } returns flowOf(attachment)
        coEvery { attachmentRepository.getAttachmentPreviewUri(attachment) } returns AppResult.Success(previewUri)

        viewModel = AttachmentPreviewViewModel(attachmentRepository, passGoLogger, context)

        viewModel.loadAttachment("a3")
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is PreviewUiState.ExternalContent, "Expected ExternalContent but got ${state::class.simpleName}")
        assertEquals("application/pdf", (state as PreviewUiState.ExternalContent).mimeType)
        assertEquals(previewUri, state.uri)
    }

    @Test
    fun `non existent attachment shows error`() = runTest(testDispatcher) {
        coEvery { attachmentRepository.getAttachmentById("missing") } returns flowOf(null)

        viewModel = AttachmentPreviewViewModel(attachmentRepository, passGoLogger, context)

        viewModel.loadAttachment("missing")
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is PreviewUiState.Error, "Expected Error but got ${state::class.simpleName}")
        assertEquals("Attachment not found", (state as PreviewUiState.Error).message)
    }

    @Test
    fun `decryption error shows error`() = runTest(testDispatcher) {
        val attachment = Attachment(id = "a1", itemId = "i1", name = "photo.png", mimeType = "image/png")
        coEvery { attachmentRepository.getAttachmentById("a1") } returns flowOf(attachment)
        coEvery { attachmentRepository.getAttachmentPreviewUri(attachment) } returns
            AppResult.Error(AppException.UnknownException("Decrypt failed"))

        viewModel = AttachmentPreviewViewModel(attachmentRepository, passGoLogger, context)

        viewModel.loadAttachment("a1")
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is PreviewUiState.Error, "Expected Error but got ${state::class.simpleName}")
    }

    @Test
    fun `reload cancels previous load and shows new attachment`() = runTest(testDispatcher) {
        val suspend = MutableStateFlow(true)
        val imageAttachment = Attachment(id = "img", itemId = "i1", name = "photo.png", mimeType = "image/png")
        val textAttachment = Attachment(id = "txt", itemId = "i1", name = "notes.txt", mimeType = "text/plain")
        val textBytes = "Hello".toByteArray()
        val previewUri = mockk<Uri>()

        coEvery { attachmentRepository.getAttachmentById("img") } returns suspend.flatMapLatest {
            if (it) flowOf(imageAttachment) else flowOf(null)
        }
        coEvery { attachmentRepository.getAttachmentById("txt") } returns flowOf(textAttachment)
        coEvery { attachmentRepository.getAttachmentPreviewUri(imageAttachment) } returns AppResult.Success(previewUri)
        coEvery { attachmentRepository.getAttachmentFile(textAttachment) } returns AppResult.Success(textBytes)

        viewModel = AttachmentPreviewViewModel(attachmentRepository, passGoLogger, context)

        viewModel.loadAttachment("img")
        advanceUntilIdle()

        viewModel.loadAttachment("txt")
        advanceUntilIdle()

        suspend.value = false
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state is PreviewUiState.TextContent, "Expected TextContent but got ${state::class.simpleName}")
        assertEquals("notes.txt", (state as PreviewUiState.TextContent).attachment.name)
    }

    @Test
    fun `launchExternalViewer with ExternalContent starts activity`() = runTest(testDispatcher) {
        val attachment = Attachment(id = "a3", itemId = "i1", name = "doc.pdf", mimeType = "application/pdf")
        val previewUri = mockk<Uri>()
        coEvery { attachmentRepository.getAttachmentById("a3") } returns flowOf(attachment)
        coEvery { attachmentRepository.getAttachmentPreviewUri(attachment) } returns AppResult.Success(previewUri)

        viewModel = AttachmentPreviewViewModel(attachmentRepository, passGoLogger, context)

        viewModel.loadAttachment("a3")
        advanceUntilIdle()

        val launchContext = mockk<Context>(relaxed = true)
        val mockIntent = mockk<Intent>(relaxed = true)
        viewModel.launchExternalViewer(launchContext) { _, _ -> mockIntent }

        verify { launchContext.startActivity(mockIntent) }
    }

    @Test
    fun `launchExternalViewer with non External state does nothing`() = runTest(testDispatcher) {
        coEvery { attachmentRepository.getAttachmentById(any()) } returns flowOf(
            Attachment(id = "a1", itemId = "i1", name = "test")
        )

        viewModel = AttachmentPreviewViewModel(attachmentRepository, passGoLogger, context)

        val launchContext = mockk<Context>(relaxed = true)
        viewModel.launchExternalViewer(launchContext)

        verify(exactly = 0) { launchContext.startActivity(any<Intent>()) }
    }

    @Test
    fun `onCleared cleans up cache file`() = runTest(testDispatcher) {
        val tmpDir = File(System.getProperty("java.io.tmpdir"), "preview_test")
        val previewDir = File(tmpDir, "attachment_preview")
        previewDir.mkdirs()
        val cacheFile = File(previewDir, "a1")
        cacheFile.createNewFile()
        assertTrue(cacheFile.exists())

        every { context.cacheDir } returns tmpDir

        val attachment = Attachment(id = "a1", itemId = "i1", name = "doc.pdf", mimeType = "application/pdf")
        coEvery { attachmentRepository.getAttachmentById("a1") } returns flowOf(attachment)
        coEvery { attachmentRepository.getAttachmentPreviewUri(attachment) } returns AppResult.Success(mockk())

        viewModel = AttachmentPreviewViewModel(attachmentRepository, passGoLogger, context)

        viewModel.loadAttachment("a1")
        advanceUntilIdle()

        val method = ViewModel::class.java.getDeclaredMethod("onCleared")
        method.isAccessible = true
        method.invoke(viewModel)

        assertFalse(cacheFile.exists(), "Cache file should be deleted after onCleared")

        tmpDir.deleteRecursively()
    }
}
