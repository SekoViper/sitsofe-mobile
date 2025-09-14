package com.sitsofe.scanner.barcode

import android.content.Context

class ScannerFactory(private val ctx: Context) {
    fun create(mode: ScannerMode): ScannerProvider = when(mode) {
        ScannerMode.BROADCAST -> VendorBroadcastScanner(ctx)
        ScannerMode.KEYBOARD  -> KeyboardWedgeScanner()
        ScannerMode.AUTO      -> VendorBroadcastScanner(ctx)
    }
}