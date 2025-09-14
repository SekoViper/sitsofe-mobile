package com.sitsofe.scanner.barcode

import android.content.*
import android.os.Build
import timber.log.Timber

class VendorBroadcastScanner(
    private val ctx: Context,
    private val action: String = "android.scanner.scan",
    private val extraKey: String = "result"
) : ScannerProvider {

    private var cb: ((String) -> Unit)? = null
    private var lastValue: String? = null
    private var lastTs: Long = 0L

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context?, intent: Intent?) {
            val v = intent?.getStringExtra(extraKey) ?: return
            val now = System.currentTimeMillis()
            if (v == lastValue && now - lastTs < 800) { Timber.d("Skip duplicate: $v"); return }
            lastValue = v; lastTs = now
            Timber.d("Scan(broadcast) -> $v")
            cb?.invoke(v)
        }
    }

    override fun start(onResult: (String) -> Unit) {
        cb = onResult
        val filter = IntentFilter(action)
        try {
            if (Build.VERSION.SDK_INT >= 33) {
                ctx.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                @Suppress("DEPRECATION")
                ctx.registerReceiver(receiver, filter)
            }
        } catch (t: Throwable) {
            Timber.e(t, "Failed to register scan receiver")
        }
    }

    override fun stop() {
        cb = null
        runCatching { ctx.unregisterReceiver(receiver) }
            .onFailure { Timber.w(it, "unregisterReceiver failed") }
    }
}

class ScanBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // optional toast/log if you want; leaving empty keeps it noise-free
    }
}
