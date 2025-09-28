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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sitsofe.scanner.feature.cart.CartScreen
import com.sitsofe.scanner.feature.checkout.CheckoutScreen
import com.sitsofe.scanner.feature.checkout.CheckoutViewModel
import com.sitsofe.scanner.feature.products.ProductsScreen
import com.sitsofe.scanner.feature.products.ProductsVMFactory
import com.sitsofe.scanner.feature.products.ProductsViewModel
import com.sitsofe.scanner.ui.AppBottomBar
import com.sitsofe.scanner.ui.AppTopBar

class MainActivity : ComponentActivity() {

    private val productsVM: ProductsViewModel by viewModels { ProductsVMFactory(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
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

                        // Tabs
                        composable("home") { SimpleCenterText(padding, "Home now") }
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

                        // Nested Shop routes
                        composable("shop/cart") {
                            CartScreen(
                                vm = productsVM,
                                onBack = { nav.popBackStack() },
                                onProceed = { nav.navigate("shop/checkout") }
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
            .padding(paddingValues) // <- import added
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = MaterialTheme.typography.titleLarge)
    }
}
