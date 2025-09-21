package com.sitsofe.scanner.feature.products

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.sitsofe.scanner.core.db.ProductEntity
import com.sitsofe.scanner.barcode.ScannerFactory
import com.sitsofe.scanner.barcode.ScannerMode
import com.sitsofe.scanner.ui.CameraScanScreen

@Composable
fun ProductsScreen(
    onPick: (ProductEntity) -> Unit,
    outerPadding: PaddingValues = PaddingValues(0.dp)
) {
    val ctx = LocalContext.current
    val vm: ProductsViewModel = viewModel(factory = ProductsVMFactory(ctx))
    LaunchedEffect(Unit) { vm.initialSyncOnce() }

    val provider = remember { ScannerFactory(ctx).create(ScannerMode.AUTO) }
    DisposableEffect(provider) {
        provider.start { code -> vm.addByBarcode(code) }
        onDispose { provider.stop() }
    }

    var cameraOpen by remember { mutableStateOf(false) }

    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(Unit) { vm.events.collect { snackbar.showSnackbar(it) } }

    val lazyItems = vm.items.collectAsLazyPagingItems()
    val cart by vm.cart.collectAsState()

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { inside ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(outerPadding)
                .padding(inside)
                .padding(12.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = vm.query,
                    onValueChange = { vm.onQueryChange(it) },
                    label = { Text("Search for products…") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = { cameraOpen = !cameraOpen }) {
                    Text(if (cameraOpen) "Close" else "Scan")
                }
            }

            if (cameraOpen) {
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth().height(240.dp)) {
                    CameraScanScreen(
                        onResult = { code ->
                            vm.addByBarcode(code)
                            cameraOpen = false
                        },
                        onClose = { cameraOpen = false }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // NOTE: no paging-compose items() here. We use count + index.
                items(lazyItems.itemCount) { index ->
                    val p = lazyItems[index] ?: return@items

                    ListItem(
                        leadingContent = {
                            val q = cart[p.id] ?: 0
                            if (q > 0) AssistChip(onClick = {}, label = { Text("$q") })
                        },
                        headlineContent = { Text(p.name) },
                        supportingContent = { Text("${p.categoryName} · Stock: ${p.stock}") },
                        trailingContent = {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    "GHS ${p.price}",
                                    color = MaterialTheme.colorScheme.primary
                                )
                                FilledTonalButton(onClick = { vm.add(p) }) { Text("+") }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(p) }
                    )
                    Divider()
                }

                // Loading / error footers
                if (lazyItems.loadState.refresh is LoadState.Loading) {
                    item { CircularProgressIndicator(Modifier.padding(16.dp)) }
                }
                if (lazyItems.loadState.append is LoadState.Loading) {
                    item { CircularProgressIndicator(Modifier.padding(16.dp)) }
                }
                if (lazyItems.loadState.refresh is LoadState.Error) {
                    item { Text("Failed to load. Pull to refresh.", Modifier.padding(16.dp)) }
                }
            }
        }
    }
}
