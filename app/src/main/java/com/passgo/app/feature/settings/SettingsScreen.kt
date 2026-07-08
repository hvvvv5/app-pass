package com.passgo.app.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.passgo.app.data.settings.ThemeMode

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val autoLockSeconds by viewModel.autoLockSeconds.collectAsState()
    val clipboardClearEnabled by viewModel.clipboardClearEnabled.collectAsState()
    val clipboardClearDelayMs by viewModel.clipboardClearDelayMs.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Settings",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        SectionHeader("Appearance")

        ThemeSection(
            currentMode = themeMode,
            onModeSelected = viewModel::setThemeMode
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        SectionHeader("Security")

        AutoLockSection(
            currentSeconds = autoLockSeconds,
            onSecondsSelected = viewModel::setAutoLockSeconds
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        ClipboardSection(
            enabled = clipboardClearEnabled,
            delayMs = clipboardClearDelayMs,
            onEnabledChanged = viewModel::setClipboardClearEnabled,
            onDelayMsSelected = viewModel::setClipboardClearDelayMs
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        SectionHeader("About")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(
                modifier = Modifier.padding(start = 16.dp)
            ) {
                Text(
                    "App Version",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    viewModel.appVersion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun ThemeSection(
    currentMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit
) {
    Text(
        "Theme",
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    ThemeMode.entries.forEach { mode ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onModeSelected(mode) }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = currentMode == mode,
                onClick = { onModeSelected(mode) }
            )
            Text(
                text = when (mode) {
                    ThemeMode.SYSTEM -> "System Default"
                    ThemeMode.LIGHT -> "Light"
                    ThemeMode.DARK -> "Dark"
                },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun AutoLockSection(
    currentSeconds: Int,
    onSecondsSelected: (Int) -> Unit
) {
    Text(
        "Auto-Lock Timer",
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    val options = listOf(
        60 to "1 minute",
        300 to "5 minutes",
        600 to "10 minutes",
        1800 to "30 minutes"
    )

    options.forEach { (seconds, label) ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSecondsSelected(seconds) }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = currentSeconds == seconds,
                onClick = { onSecondsSelected(seconds) }
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun ClipboardSection(
    enabled: Boolean,
    delayMs: Long,
    onEnabledChanged: (Boolean) -> Unit,
    onDelayMsSelected: (Long) -> Unit
) {
    Text(
        "Clipboard",
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Auto-Clear Copied Passwords",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = enabled,
            onCheckedChange = onEnabledChanged
        )
    }

    if (enabled) {
        Text(
            "Clear after:",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )

        val options = listOf(
            15000L to "15 seconds",
            30000L to "30 seconds",
            60000L to "60 seconds"
        )

        options.forEach { (ms, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDelayMsSelected(ms) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = delayMs == ms,
                    onClick = { onDelayMsSelected(ms) }
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
