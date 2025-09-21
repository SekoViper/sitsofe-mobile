package com.sitsofe.scanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import com.sitsofe.scanner.ui.ScanScreen
import com.sitsofe.scanner.feature.products.ProductsScreen
import com.sitsofe.scanner.core.auth.Auth
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TEMP: seed auth so API calls work during dev. (Replace with real login later)
        Auth.token = Auth.token ?: "YOUR_JWT_HERE"
        Auth.tenantId = Auth.tenantId ?: "36cf424d-1413-4ec3-92aa-1e9062ed4998"   // <- note trailing 8
        Auth.subsidiaryId = Auth.subsidiaryId ?: "6831d64c5612a14921872e77"
        Auth.role = Auth.role ?: "subsidiary_admin"

        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) {
                    AppNav()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppNav() {
    val nav = rememberNavController()
    val backStack = nav.currentBackStackEntryAsState()
    val route = backStack.value?.destination?.route ?: "products"

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val title = when (route) {
        "scan" -> "Scan"
        else -> "Products"
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (route != "products") {
                        TextButton(onClick = { nav.popBackStack() }) { Text("Back") }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = "products",
            modifier = Modifier.padding(padding)
        ) {
            composable("products") {
                ProductsScreen(
                    onScanClick = { nav.navigate("scan") },
                    onPick = { product ->
                        // Show a snackbar from a coroutine instead of LaunchedEffect
                        scope.launch { snackbar.showSnackbar("${product.name} selected") }
                        // TODO: add to cart or open details screen here
                    }
                )
            }
            composable("scan") {
                // Your existing scanner UI (hardware broadcast + CameraX fallback)
                ScanScreen()
            }
        }
    }
}
