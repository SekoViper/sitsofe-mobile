package com.sitsofe.scanner.feature.products

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.sitsofe.scanner.core.db.ProductEntity
import com.sitsofe.scanner.core.db.pretty

@Composable
fun ProductsScreen(
    onScanClick: () -> Unit,
    onPick: (ProductEntity) -> Unit
) {
    val vm: ProductsViewModel = viewModel(factory = ProductsVMFactory(LocalContext.current))
    LaunchedEffect(Unit) { vm.initialSyncOnce() }

    var q by remember { mutableStateOf("") }
    val lazyItems = vm.items.collectAsLazyPagingItems()

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = q,
                onValueChange = { value -> q = value; vm.onQueryChange(value) },
                label = { Text("Search products by name or barcode") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Button(onClick = onScanClick) { Text("Scan") }
        }

        Spacer(Modifier.height(8.dp))

        // List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(lazyItems.itemCount) { index ->
                val p = lazyItems[index]
                if (p != null) {
                    ListItem(
                        headlineContent = { Text(p.name) },
                        supportingContent = { Text(p.pretty()) },
                        trailingContent = { Text(p.barcode ?: "") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(p) }
                    )
                    Divider()
                }
            }
        }

        // Lightweight loading/error UI outside the list to avoid 'item { }' DSL
        when {
            lazyItems.loadState.refresh is LoadState.Loading ||
                    lazyItems.loadState.append  is LoadState.Loading -> {
                Spacer(Modifier.height(8.dp))
                CircularProgressIndicator()
            }
            lazyItems.loadState.refresh is LoadState.Error -> {
                Spacer(Modifier.height(8.dp))
                Text("Failed to load. Pull to refresh or retry.")
            }
        }
    }
}
