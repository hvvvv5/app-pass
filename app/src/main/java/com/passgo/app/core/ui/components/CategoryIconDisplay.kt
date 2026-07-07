package com.passgo.app.core.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.ContactMail
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.passgo.app.core.model.CategoryIconIdentifier
import com.passgo.app.core.model.VaultItemCategory

fun CategoryIconIdentifier.toImageVector(): ImageVector = when (this) {
    CategoryIconIdentifier.GOOGLE -> Icons.Default.Badge
    CategoryIconIdentifier.EMAIL -> Icons.Default.Mail
    CategoryIconIdentifier.SOCIAL_MEDIA -> Icons.Default.People
    CategoryIconIdentifier.BANKING -> Icons.Default.Key
    CategoryIconIdentifier.SHOPPING -> Icons.Default.ShoppingCart
    CategoryIconIdentifier.WORK -> Icons.Default.Work
    CategoryIconIdentifier.ENTERTAINMENT -> Icons.Default.Star
    CategoryIconIdentifier.GAMING -> Icons.Default.Games
    CategoryIconIdentifier.WIFI -> Icons.Default.Wifi
    CategoryIconIdentifier.SOFTWARE_LICENSE -> Icons.Default.Description
    CategoryIconIdentifier.SECURE_NOTE -> Icons.Default.Dns
    CategoryIconIdentifier.OTHER -> Icons.Outlined.Folder
}

fun VaultItemCategory.toColor(): Color = Color(this.colorArgb)

@Composable
fun CategoryIcon(
    category: VaultItemCategory,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = category.icon.toImageVector(),
        contentDescription = category.displayName,
        modifier = modifier,
        tint = category.toColor()
    )
}
