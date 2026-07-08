package com.passgo.app.feature.settings

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.pm.PackageInfoCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passgo.app.core.logging.PassGoLogger
import com.passgo.app.data.settings.ThemeMode
import com.passgo.app.data.settings.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val logger: PassGoLogger,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = userPreferences.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    val autoLockSeconds: StateFlow<Int> = userPreferences.autoLockSeconds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 300)

    val appVersion: String = try {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        "${info.versionName} (${PackageInfoCompat.getLongVersionCode(info)})"
    } catch (_: PackageManager.NameNotFoundException) {
        "Unknown"
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            userPreferences.setThemeMode(mode)
        }
    }

    fun setAutoLockSeconds(seconds: Int) {
        viewModelScope.launch {
            userPreferences.setAutoLockSeconds(seconds)
        }
    }

    val clipboardClearEnabled: StateFlow<Boolean> = userPreferences.clipboardClearEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val clipboardClearDelayMs: StateFlow<Long> = userPreferences.clipboardClearDelayMs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 30000L)

    fun setClipboardClearEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setClipboardClearEnabled(enabled)
        }
    }

    fun setClipboardClearDelayMs(delayMs: Long) {
        viewModelScope.launch {
            userPreferences.setClipboardClearDelayMs(delayMs)
        }
    }
}
