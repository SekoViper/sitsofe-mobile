package com.sitsofe.scanner.feature.checkout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sitsofe.scanner.core.db.ProductEntity
import com.sitsofe.scanner.feature.products.ProductsViewModel

@Composable
fun CheckoutScreen(
    vm: CheckoutViewModel,
    productsVM: ProductsViewModel,
    onClose: () -> Unit,
    onCompleted: () -> Unit,
    outerPadding: PaddingValues = PaddingValues(0.dp)
) {
    val loading by vm.loading.collectAsState()
    val customers by vm.customers.collectAsState()
    val search by vm.search.collectAsState()
    val selected by vm.selected.collectAsState()
    val internal by vm.internal.collectAsState()
    val condition by vm.condition.collectAsState()
    val toast by vm.toast.collectAsState()

    // Load customers once
    LaunchedEffect(Unit) { vm.loadCustomers() }

    // Build cart lines from shared ProductsViewModel
    val cart by productsVM.cart.collectAsState()
    val ids = remember(cart) { cart.keys.toList() }
    var lines by remember { mutableStateOf<List<Pair<ProductEntity, Int>>>(emptyList()) }

    LaunchedEffect(ids) {
        val items = productsVM.productsByIds(ids)
        val map = items.associateBy { it.id }
        lines = ids.mapNotNull { id ->
            val p = map[id] ?: return@mapNotNull null
            val q = cart[id] ?: 0
            p to q
        }
    }

    // Filter customers by last 4
    val filtered = remember(customers, search) {
        if (search.isBlank()) customers
        else customers.filter { it.phone.takeLast(4).contains(search) }
    }

    val snack = remember { SnackbarHostState() }
    LaunchedEffect(toast) {
        toast?.let {
            snack.showSnackbar(it)
            vm.clearToast()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snack) }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(outerPadding)
                .padding(padding)
                .padding(16.dp)
        ) {

            Text(
                "Complete Sales",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(18.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = search,
                    onValueChange = { vm.onSearchChange(it.filter { ch -> ch.isDigit() }.take(4)) },
                    label = { Text("Search customer last 4 digits") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                )
                val green = Color(0xFF26C281)
                OutlinedButton(
                    onClick = { vm.toggleInternal() },
                    border = BorderStroke(1.dp, if (internal) green else MaterialTheme.colorScheme.outline),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (internal) green.copy(alpha = 0.15f) else Color.Transparent,
                        contentColor = if (internal) green else MaterialTheme.colorScheme.onSurface
                    ),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Internal Sales")
                }
            }

            Spacer(Modifier.height(16.dp))

            if (selected != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AssistChip(
                        onClick = { vm.clearSelection() },
                        label = { Text(selected!!.name) },
                        trailingIcon = {
                            IconButton(onClick = { vm.clearSelection() }) {
                                Text("✕", color = MaterialTheme.colorScheme.primary)
                            }
                        },
                        modifier = Modifier
                            .height(40.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            if (!internal && selected == null) {
                Text("All Customers", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                if (loading && customers.isEmpty()) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 220.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filtered, key = { it._id }) { c ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${c.name} - ${c.phone}", style = MaterialTheme.typography.bodyMedium)
                                Button(
                                    onClick = { vm.selectCustomer(c) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    shape = MaterialTheme.shapes.small,
                                    modifier = Modifier.height(36.dp)
                                ) { Text("Select") }
                            }
                            Divider()
                        }
                    }
                }
            }

            if (internal || selected != null) {
                Spacer(Modifier.height(16.dp))
                Text("Enter Conditions", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = condition,
                    onValueChange = vm::onConditionChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 80.dp)
                        .heightIn(min = 80.dp, max = 160.dp),
                    placeholder = { Text("Optional notes...") },
                    minLines = 3,
                    maxLines = 6,
                    shape = MaterialTheme.shapes.medium
                )
            }

            Spacer(Modifier.height(24.dp))

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onClose,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE74C3C),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = MaterialTheme.shapes.medium
                ) { Text("Close") }

                Button(
                    onClick = { vm.completeSale(lines) { onCompleted() } },
                    enabled = !loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2F80ED),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = MaterialTheme.shapes.medium
                ) { Text("Complete Sales") }
            }
        }
    }
}
