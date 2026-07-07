package com.passgo.app.core.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.passgo.app.core.model.VaultItemCategory
import com.passgo.app.data.session.SessionManager
import com.passgo.app.feature.home.HomeScreen
import com.passgo.app.feature.premium.PremiumScreen
import com.passgo.app.feature.settings.SettingsScreen
import com.passgo.app.feature.setup.SetupScreen
import com.passgo.app.feature.unlock.UnlockScreen
import com.passgo.app.feature.vault.DynamicFormScreen
import com.passgo.app.feature.vault.DynamicItemDetailScreen
import com.passgo.app.feature.vault.VaultScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Setup : Screen("setup", "", Icons.Default.Lock)
    data object Unlock : Screen("unlock", "", Icons.Default.Lock)
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object Vault : Screen("vault", "Vault", Icons.Default.Lock)
    data object Premium : Screen("premium", "Premium", Icons.Default.Star)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

private val bottomNavItems = listOf(Screen.Home, Screen.Vault, Screen.Premium, Screen.Settings)

@Composable
fun PassGoNavHost(sessionManager: SessionManager) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val sessionState by sessionManager.sessionState.collectAsState()

    val startDestination = when (sessionState) {
        com.passgo.app.data.session.SessionManager.SessionState.SETUP_REQUIRED -> Screen.Setup.route
        com.passgo.app.data.session.SessionManager.SessionState.LOCKED -> Screen.Unlock.route
        com.passgo.app.data.session.SessionManager.SessionState.UNLOCKED -> Screen.Home.route
    }

    val bottomBarRoutes = bottomNavItems.map { it.route }
    val showBottomBar = currentDestination?.route in bottomBarRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(padding)
            ) {
                composable(Screen.Setup.route) {
                    SetupScreen(onComplete = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Setup.route) { inclusive = true }
                        }
                    })
                }
                composable(Screen.Unlock.route) {
                    UnlockScreen(onUnlocked = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Unlock.route) { inclusive = true }
                        }
                    })
                }
                composable(Screen.Home.route) {
                    HomeScreen(
                        onAddItem = {
                            navController.navigate("vault/add")
                        }
                    )
                }
                composable(Screen.Vault.route) {
                    VaultScreen(
                        onAddItem = { navController.navigate("vault/add") },
                        onItemClick = { itemId -> navController.navigate("vault/detail/$itemId") }
                    )
                }
                composable(
                    route = "vault/add?category={category}",
                    arguments = listOf(
                        navArgument("category") {
                            type = NavType.StringType
                            defaultValue = null
                            nullable = true
                        }
                    )
                ) { backStackEntry ->
                    val categoryName = backStackEntry.arguments?.getString("category")
                    val category = categoryName?.let { name ->
                        VaultItemCategory.entries.find { it.name == name }
                    }
                    DynamicFormScreen(
                        itemId = null,
                        categoryArg = category,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(
                    route = "vault/detail/{itemId}",
                    arguments = listOf(navArgument("itemId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
                    DynamicItemDetailScreen(
                        itemId = itemId,
                        onNavigateBack = { navController.popBackStack() },
                        onEdit = { id -> navController.navigate("vault/edit/$id") }
                    )
                }
                composable(
                    route = "vault/edit/{itemId}",
                    arguments = listOf(navArgument("itemId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
                    DynamicFormScreen(
                        itemId = itemId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.Premium.route) { PremiumScreen() }
                composable(Screen.Settings.route) { SettingsScreen() }
            }
        }
    }
}
