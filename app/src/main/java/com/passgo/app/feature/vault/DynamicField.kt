package com.passgo.app.feature.vault

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.passgo.app.core.model.FieldDefinition
import com.passgo.app.core.model.FieldInputType

@Composable
fun DynamicField(
    definition: FieldDefinition,
    value: String,
    onValueChange: (String) -> Unit,
    error: String? = null,
    onGeneratePassword: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    when (definition.inputType) {
        FieldInputType.PASSWORD -> DynamicPasswordField(
            definition = definition,
            value = value,
            onValueChange = onValueChange,
            error = error,
            onGenerate = onGeneratePassword,
            modifier = modifier
        )
        FieldInputType.EMAIL -> DynamicEmailField(
            definition = definition,
            value = value,
            onValueChange = onValueChange,
            error = error,
            modifier = modifier
        )
        FieldInputType.URL -> DynamicUrlField(
            definition = definition,
            value = value,
            onValueChange = onValueChange,
            error = error,
            modifier = modifier
        )
        FieldInputType.PHONE -> DynamicPhoneField(
            definition = definition,
            value = value,
            onValueChange = onValueChange,
            error = error,
            modifier = modifier
        )
        FieldInputType.DATE -> DynamicDatePicker(
            definition = definition,
            value = value,
            onValueChange = onValueChange,
            error = error,
            modifier = modifier
        )
        FieldInputType.NUMBER -> DynamicTextField(
            definition = definition,
            value = value,
            onValueChange = onValueChange,
            error = error,
            keyboardType = KeyboardType.Number,
            modifier = modifier
        )
        FieldInputType.TEXT -> {
            if (definition.maxLength > 500) {
                DynamicNotesField(
                    definition = definition,
                    value = value,
                    onValueChange = onValueChange,
                    error = error,
                    modifier = modifier
                )
            } else {
                DynamicTextField(
                    definition = definition,
                    value = value,
                    onValueChange = onValueChange,
                    error = error,
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
fun DynamicTextField(
    definition: FieldDefinition,
    value: String,
    onValueChange: (String) -> Unit,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { onValueChange(definition.parse(it)) },
            label = { Text(definition.label) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = ImeAction.Next
            ),
            isError = error != null,
            supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun DynamicPasswordField(
    definition: FieldDefinition,
    value: String,
    onValueChange: (String) -> Unit,
    error: String? = null,
    onGenerate: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { onValueChange(definition.parse(it)) },
            label = { Text(definition.label) },
            singleLine = true,
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            isError = error != null,
            supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            trailingIcon = {
                Row {
                    if (onGenerate != null) {
                        IconButton(onClick = onGenerate) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "Generate")
                        }
                    }
                    IconButton(onClick = { visible = !visible }) {
                        Icon(
                            if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (visible) "Hide" else "Show"
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun DynamicUrlField(
    definition: FieldDefinition,
    value: String,
    onValueChange: (String) -> Unit,
    error: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { onValueChange(definition.parse(it)) },
            label = { Text(definition.label) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Next
            ),
            isError = error != null,
            supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun DynamicEmailField(
    definition: FieldDefinition,
    value: String,
    onValueChange: (String) -> Unit,
    error: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { onValueChange(definition.parse(it)) },
            label = { Text(definition.label) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            isError = error != null,
            supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun DynamicPhoneField(
    definition: FieldDefinition,
    value: String,
    onValueChange: (String) -> Unit,
    error: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { onValueChange(definition.parse(it)) },
            label = { Text(definition.label) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            isError = error != null,
            supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun DynamicDatePicker(
    definition: FieldDefinition,
    value: String,
    onValueChange: (String) -> Unit,
    error: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = if (value.length == 8) {
                "${value.substring(0, 4)}-${value.substring(4, 6)}-${value.substring(6, 8)}"
            } else value,
            onValueChange = { raw ->
                val cleaned = raw.filter { it.isDigit() }
                if (cleaned.length <= 8) {
                    onValueChange(cleaned)
                }
            },
            label = { Text(definition.label) },
            singleLine = true,
            placeholder = { Text("YYYY-MM-DD") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            isError = error != null,
            supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun DynamicNotesField(
    definition: FieldDefinition,
    value: String,
    onValueChange: (String) -> Unit,
    error: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { onValueChange(definition.parse(it)) },
            label = { Text(definition.label) },
            minLines = 3,
            maxLines = 8,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
            isError = error != null,
            supportingText = error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun DynamicFieldDisplay(
    definition: FieldDefinition,
    value: String,
    onCopy: (() -> Unit)? = null,
    onOpen: (() -> Unit)? = null,
    onToggleVisibility: (() -> Unit)? = null,
    isPasswordVisible: Boolean = false
) {
    val displayValue = when {
        definition.inputType == FieldInputType.PASSWORD && !isPasswordVisible -> "\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022"
        definition.inputType == FieldInputType.DATE && value.length == 8 -> {
            "${value.substring(0, 4)}-${value.substring(4, 6)}-${value.substring(6, 8)}"
        }
        else -> definition.format(value)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = definition.label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            if (onToggleVisibility != null) {
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (isPasswordVisible) "Hide" else "Show",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            if (onCopy != null) {
                IconButton(onClick = onCopy) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(18.dp))
                }
            }
            if (onOpen != null) {
                IconButton(onClick = onOpen) {
                    Icon(Icons.Default.Language, contentDescription = "Open", modifier = Modifier.size(18.dp))
                }
            }
        }
        if (value.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = displayValue,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
