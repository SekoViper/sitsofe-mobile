package com.sitsofe.scanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sitsofe.scanner.feature.cart.CartScreen
import com.sitsofe.scanner.feature.products.ProductsScreen
import com.sitsofe.scanner.feature.products.ProductsVMFactory
import com.sitsofe.scanner.feature.products.ProductsViewModel
import com.sitsofe.scanner.ui.AppTopBar

class MainActivity : ComponentActivity() {

    private val productsVM: ProductsViewModel by viewModels { ProductsVMFactory(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                val nav = rememberNavController()
                val backStackEntry by nav.currentBackStackEntryAsState()
                val route = backStackEntry?.destination?.route ?: "products"
                val canGoBack = nav.previousBackStackEntry != null
                val cartCount by productsVM.cartCount.collectAsState(initial = 0)

                val title = when (route) {
                    "cart" -> "Cart ($cartCount)"
                    else -> "Products"
                }

                Scaffold(
                    topBar = {
                        AppTopBar(
                            showBack = canGoBack,
                            title = title,
                            cartCount = cartCount,
                            onBack = { nav.popBackStack() },
                            onCart = { if (route != "cart") nav.navigate("cart") }
                        )
                    }
                ) { padding ->
                    NavHost(navController = nav, startDestination = "products") {
                        composable("products") {
                            ProductsScreen(
                                vm = productsVM,
                                onPick = { /* open details if needed */ },
                                outerPadding = padding
                            )
                        }
                        composable("cart") {
                            CartScreen(
                                vm = productsVM,
                                onBack = { nav.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
