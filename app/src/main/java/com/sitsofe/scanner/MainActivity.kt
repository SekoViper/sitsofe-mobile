package com.sitsofe.scanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sitsofe.scanner.core.auth.Auth
import com.sitsofe.scanner.feature.auth.LoginScreen
import com.sitsofe.scanner.feature.auth.LoginViewModel
import com.sitsofe.scanner.feature.cart.CartScreen
import com.sitsofe.scanner.feature.checkout.CheckoutScreen
import com.sitsofe.scanner.feature.checkout.CheckoutViewModel
import com.sitsofe.scanner.feature.dashboard.DashboardScreen
import com.sitsofe.scanner.feature.dashboard.DashboardViewModel
import com.sitsofe.scanner.feature.products.ProductsScreen
import com.sitsofe.scanner.feature.products.ProductsVMFactory
import com.sitsofe.scanner.feature.products.ProductsViewModel
import com.sitsofe.scanner.ui.AppBottomBar
import com.sitsofe.scanner.ui.AppTopBar

class MainActivity : ComponentActivity() {

    private val productsVM: ProductsViewModel by viewModels { ProductsVMFactory(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Draw edge-to-edge (we’ll add insets in Compose with statusBarsPadding / navigationBarsPadding)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MaterialTheme {
                // Auth gate — reflect persisted session
                var loggedIn by remember { mutableStateOf(Auth.isLoggedIn) }
                val loginVM = remember { LoginViewModel(applicationContext) }

                if (!loggedIn) {
                    LoginScreen(
                        vm = loginVM,
                        onLoggedIn = { loggedIn = true }
                    )
                    return@MaterialTheme
                }

                val nav = rememberNavController()
                val backEntry by nav.currentBackStackEntryAsState()
                val route = backEntry?.destination?.route ?: "shop"
                val canGoBack = nav.previousBackStackEntry != null
                val cartCount by productsVM.cartCount.collectAsState(initial = 0)

                val (title, showCart) = when {
                    route.startsWith("shop/checkout") -> "Complete Sales" to false
                    route.startsWith("shop/cart") -> "Cart ($cartCount)" to false
                    route.startsWith("shop") -> "Products" to true
                    route == "home" -> "Home" to false
                    route == "inventory" -> "Inventory" to false
                    route == "account" -> "Account" to false
                    route == "settings" -> "Settings" to false
                    else -> "Sitsofe" to false
                }

                Scaffold(
                    // IMPORTANT: we manage insets ourselves; don’t let Scaffold add system paddings
                    contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
                    topBar = {
                        AppTopBar(
                            showBack = canGoBack,
                            title = title,
                            cartCount = cartCount,
                            showCart = showCart,
                            onBack = { nav.popBackStack() },
                            onCart = { nav.navigate("shop/cart") }
                        )
                    },
                    bottomBar = { AppBottomBar(nav) }
                ) { padding ->
                    NavHost(navController = nav, startDestination = "shop") {

                        composable("home") {
                            val vm = remember { DashboardViewModel(applicationContext) }
                            DashboardScreen(vm = vm, outerPadding = padding)
                        }
                        composable("shop") {
                            ProductsScreen(
                                vm = productsVM,
                                onPick = { /* optional details */ },
                                outerPadding = padding
                            )
                        }
                        composable("inventory") { SimpleCenterText(padding, "Inventory") }
                        composable("account") { SimpleCenterText(padding, "Account") }
                        composable("settings") { SimpleCenterText(padding, "Settings") }

                        composable("shop/cart") {
                            CartScreen(
                                vm = productsVM,
                                onBack = { nav.popBackStack() },
                                onProceed = { nav.navigate("shop/checkout") },
                                outerPadding = padding
                            )
                        }
                        composable("shop/checkout") {
                            val vm = remember { CheckoutViewModel(this@MainActivity, productsVM) }
                            CheckoutScreen(
                                vm = vm,
                                productsVM = productsVM,
                                onClose = { nav.popBackStack() },
                                onCompleted = {
                                    productsVM.clearCart()
                                    nav.popBackStack("shop", inclusive = false)
                                },
                                outerPadding = padding
                            )
                        }
                    }
                }
            }
        }
    }
}

// Simple placeholder screens for non-Shop tabs
@Composable
private fun SimpleCenterText(paddingValues: PaddingValues, text: String) {
    Box(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = MaterialTheme.typography.titleLarge)
    }
}
