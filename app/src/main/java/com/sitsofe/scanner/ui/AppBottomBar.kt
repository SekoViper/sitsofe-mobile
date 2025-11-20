package com.sitsofe.scanner.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

data class BottomItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

/**
 * Bottom navigation with 5 tabs.
 * Any route starting with "shop" is considered selected on the Shop tab.
 */
@Composable
fun AppBottomBar(nav: NavHostController) {
    val items = listOf(
        BottomItem("home", "Dashboard", Icons.Outlined.Home),
        BottomItem("shop", "Shop", Icons.Outlined.ShoppingCart),
        BottomItem("inventory", "Inventory", Icons.Outlined.ListAlt),
        BottomItem("account", "Account", Icons.Outlined.AccountCircle),
        BottomItem("settings", "Settings", Icons.Outlined.Settings),
    )

    val backStackEntry by nav.currentBackStackEntryAsState()
    val current = backStackEntry?.destination

    NavigationBar {
        items.forEach { item ->
            val selected = isSelected(current, item.route)
            NavigationBarItem(
                selected = selected,
                onClick = {
                    val target = if (item.route == "shop") "shop" else item.route
                    nav.navigate(target) {
                        popUpTo(nav.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors()
            )
        }
    }
}

private fun isSelected(dest: NavDestination?, route: String): Boolean {
    if (dest == null) return false
    return dest.hierarchy.any { d ->
        val r = d.route ?: return@any false
        r == route || r.startsWith("$route/")
    }
}
