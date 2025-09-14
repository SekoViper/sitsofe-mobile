package com.sitsofe.scanner.barcode

interface ScannerProvider {
    fun start(onResult: (String) -> Unit)
    fun stop()
}

enum class ScannerMode { AUTO, BROADCAST, KEYBOARD }