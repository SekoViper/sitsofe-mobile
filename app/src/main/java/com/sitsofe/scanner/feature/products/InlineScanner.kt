package com.sitsofe.scanner.feature.products

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@Composable
fun InlineScanner(
    onDetected: (String) -> Unit,
    onClose: () -> Unit
) {
    val ctx = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
        )
    }
    val ask = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { ok -> granted = ok }

    LaunchedEffect(Unit) { if (!granted) ask.launch(Manifest.permission.CAMERA) }

    if (!granted) {
        Surface(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Camera permission is required to scan.")
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { ask.launch(Manifest.permission.CAMERA) }) { Text("Grant") }
                    OutlinedButton(onClick = onClose) { Text("Close") }
                }
            }
        }
        return
    }

    val executor = remember { Executors.newSingleThreadExecutor() }
    DisposableEffect(Unit) { onDispose { executor.shutdown() } }

    val scanner = remember {
        val opts = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_UPC_A, Barcode.FORMAT_CODE_128, Barcode.FORMAT_QR_CODE
            ).build()
        BarcodeScanning.getClient(opts)
    }
    DisposableEffect(scanner) { onDispose { scanner.close() } }

    var delivered by remember { mutableStateOf(false) }

    Surface(tonalElevation = 6.dp) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            factory = { context ->
                val view = PreviewView(context).apply {
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }

                val future = ProcessCameraProvider.getInstance(context)
                future.addListener({
                    val provider = future.get()

                    val preview = Preview.Builder()
                        .setTargetResolution(Size(1280, 720))
                        .build()
                        .also { it.setSurfaceProvider(view.surfaceProvider) }

                    val analysis = ImageAnalysis.Builder()
                        .setTargetResolution(Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    analysis.setAnalyzer(executor) { proxy ->
                        val media = proxy.image
                        if (media != null && !delivered) {
                            val input = InputImage.fromMediaImage(
                                media, proxy.imageInfo.rotationDegrees
                            )
                            scanner.process(input)
                                .addOnSuccessListener { list ->
                                    val v = list.firstOrNull()?.rawValue
                                    if (v != null && !delivered) {
                                        delivered = true
                                        onDetected(v)
                                        delivered = false
                                    }
                                }
                                .addOnCompleteListener { proxy.close() }
                        } else proxy.close()
                    }

                    provider.unbindAll()
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview, analysis
                    )
                }, ContextCompat.getMainExecutor(context))

                view
            }
        )
    }
}
