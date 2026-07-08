package com.passgo.app.core.security

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.annotation.VisibleForTesting
import com.passgo.app.data.settings.UserPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Singleton
class ClipboardGuard @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferences: UserPreferences
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    @VisibleForTesting
    internal var clipboardClearEnabled: Boolean = true

    @VisibleForTesting
    internal var clipboardClearDelayMs: Long = 30000L

    @VisibleForTesting
    internal var lastCopyToken: String? = null
        private set

    private var lastCopyJob: Job? = null

    init {
        scope.launch {
            userPreferences.clipboardClearEnabled.collect { enabled ->
                clipboardClearEnabled = enabled
            }
        }
        scope.launch {
            userPreferences.clipboardClearDelayMs.collect { delayMs ->
                clipboardClearDelayMs = delayMs
            }
        }
    }

    fun copySensitiveText(text: String) {
        lastCopyJob?.cancel()
        val token = UUID.randomUUID().toString()
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(token, text))
        lastCopyToken = token
        if (!clipboardClearEnabled) return
        lastCopyJob = scope.launch {
            delay(clipboardClearDelayMs)
            val current = clipboard.primaryClip
            if (current?.description?.label == lastCopyToken) {
                clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
            }
        }
    }

    fun cancelPendingClear() {
        lastCopyJob?.cancel()
        lastCopyToken = null
    }
}
