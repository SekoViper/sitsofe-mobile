package com.sitsofe.scanner.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sitsofe.scanner.feature.cart.CartActionIcon

/**
 * Stable custom top bar that handles status bar inset.
 */
@Composable
fun AppTopBar(
    showBack: Boolean,
    title: String,
    cartCount: Int,
    showCart: Boolean,
    onBack: () -> Unit,
    onCart: () -> Unit
) {
    Surface(shadowElevation = 4.dp) {
        Column(
            modifier = Modifier
                // Make space for the transparent status bar so no “black strip” appears above
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showBack) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                } else {
                    Spacer(Modifier.width(8.dp))
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 6.dp)
                )

                if (showCart) {
                    CartActionIcon(count = cartCount, onClick = onCart)
                } else {
                    Spacer(Modifier.width(8.dp))
                }
            }
        }
    }
}
