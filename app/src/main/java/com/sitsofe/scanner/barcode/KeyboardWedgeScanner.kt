package com.sitsofe.scanner.barcode

class KeyboardWedgeScanner: ScannerProvider {
    private var cb: ((String) -> Unit)? = null
    override fun start(onResult: (String) -> Unit) { cb = onResult }
    override fun stop() { cb = null }
    fun onSubmit(text: String) { cb?.invoke(text) }
}