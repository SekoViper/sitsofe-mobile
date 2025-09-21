package com.sitsofe.scanner.ui

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.sitsofe.scanner.barcode.*
import timber.log.Timber

@Composable
fun ScanScreen() {
    val ctx = LocalContext.current
    val vib = remember { ContextCompat.getSystemService(ctx, Vibrator::class.java) }

    var lastScan by remember { mutableStateOf("--") }
    var mode by remember { mutableStateOf(ScannerMode.AUTO) }
    var showCamera by remember { mutableStateOf(false) }

    val provider = remember(mode) { ScannerFactory(ctx).create(mode) }

    // Show camera scanner when requested
    if (showCamera) {
        CameraScanScreen(
            onResult = { code ->
                lastScan = code
                showCamera = false
            },
            onClose = { showCamera = false }
        )
        return
    }

    DisposableEffect(provider) {
        provider.start { value ->
            lastScan = value
            runCatching {
                if (vib != null && vib.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vib.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION") vib.vibrate(50)
                    }
                }
            }
            Timber.d("Scan->$value")
        }
        onDispose { provider.stop() }
    }

    Column(Modifier.padding(16.dp)) {
        Text("Sitsofe Scanner v1.3", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        Spacer(Modifier.height(12.dp))
        Button(onClick = { showCamera = true }) { Text("Scan") }

        Spacer(Modifier.height(16.dp))
        Text("Last scan:")
        Text(lastScan, style = MaterialTheme.typography.headlineSmall)
    }
}

