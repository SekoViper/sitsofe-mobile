package com.sitsofe.scanner.feature.cart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sitsofe.scanner.core.db.ProductEntity
import com.sitsofe.scanner.feature.products.ProductsViewModel

@Composable
fun CartScreen(
    vm: ProductsViewModel,
    onBack: () -> Unit,
    onProceed: () -> Unit
) {
    val cart by vm.cart.collectAsState()
    val ids = remember(cart) { cart.keys.toList() }

    var lines by remember { mutableStateOf<List<Pair<ProductEntity, Int>>>(emptyList()) }
    var total by remember { mutableStateOf(0.0) }

    LaunchedEffect(ids) {
        val items = vm.productsByIds(ids)
        val map = items.associateBy { it.id }
        val computed = ids.mapNotNull { id ->
            val p = map[id] ?: return@mapNotNull null
            val q = cart[id] ?: 0
            p to q
        }
        lines = computed
        total = computed.sumOf { it.first.price * it.second }
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Items: ${cart.values.sum()}", style = MaterialTheme.typography.titleMedium)
            if (cart.isNotEmpty()) {
                TextButton(onClick = { vm.clearCart() }) { Text("Clear Cart") }
            }
        }

        if (lines.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Your cart is empty")
            }
            return
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(lines, key = { it.first.id }) { (p, qty) ->
                ElevatedCard {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(p.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Stock: ${p.stock.toInt()}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { vm.remove(p) }) { Text("–") }
                            Text(qty.toString(), style = MaterialTheme.typography.titleMedium)
                            FilledTonalButton(onClick = { vm.add(p) }) { Text("+") }
                        }
                    }
                    Divider()
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Unit", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "GHS ${p.price}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Surface(tonalElevation = 2.dp) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total", style = MaterialTheme.typography.labelMedium)
                    Text("GHS ${"%.2f".format(total)}", style = MaterialTheme.typography.titleLarge)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onBack) { Text("Close") }
                    Button(onClick = onProceed) { Text("Proceed") }
                }
            }
        }
    }
}
