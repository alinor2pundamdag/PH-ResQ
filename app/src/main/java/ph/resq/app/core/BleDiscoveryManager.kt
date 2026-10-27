package ph.resq.app.core

import android.Manifest
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat

data class NearbyBlePeer(val label: String, val rssi: Int)

class BleDiscoveryManager(context: Context) {
    private val appContext = context.applicationContext
    private val handler = Handler(Looper.getMainLooper())
    private val bluetoothManager = appContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter get() = bluetoothManager.adapter
    private var scanner: BluetoothLeScanner? = null
    private var callback: ScanCallback? = null
    private var onChanged: ((List<NearbyBlePeer>) -> Unit)? = null
    private var onFinished: (() -> Unit)? = null
    private val peers = linkedMapOf<String, NearbyBlePeer>()
    private val stopTask = Runnable { stop(); onFinished?.invoke() }

    fun start(onDevicesChanged: (List<NearbyBlePeer>) -> Unit, onFinished: () -> Unit) {
        stop()
        val bluetoothAdapter = adapter ?: throw IllegalStateException("Bluetooth hardware unavailable")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requireGranted(Manifest.permission.BLUETOOTH_SCAN)
            requireGranted(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            requireGranted(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (!bluetoothAdapter.isEnabled) throw IllegalStateException("Bluetooth disabled")
        val activeScanner = bluetoothAdapter.bluetoothLeScanner ?: throw IllegalStateException("BLE scanner unavailable")
        peers.clear()
        onChanged = onDevicesChanged
        this.onFinished = onFinished
        scanner = activeScanner
        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val key = try { result.device.address } catch (_: SecurityException) { result.device.hashCode().toString() }
                val label = try {
                    result.scanRecord?.deviceName ?: result.device.name ?: "Nearby BLE device"
                } catch (_: SecurityException) {
                    "Nearby BLE device"
                }
                peers[key] = NearbyBlePeer(label.take(48), result.rssi)
                this@BleDiscoveryManager.onChanged?.invoke(peers.values.sortedByDescending { it.rssi })
            }

            override fun onScanFailed(errorCode: Int) {
                this@BleDiscoveryManager.stop()
                this@BleDiscoveryManager.onFinished?.invoke()
            }
        }
        callback = scanCallback
        try {
            activeScanner.startScan(null, ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build(), scanCallback)
        } catch (error: SecurityException) {
            stop()
            throw error
        }
        handler.postDelayed(stopTask, 15_000L)
    }

    fun stop() {
        handler.removeCallbacks(stopTask)
        val activeScanner = scanner
        val activeCallback = callback
        if (activeScanner != null && activeCallback != null) {
            try { activeScanner.stopScan(activeCallback) } catch (_: SecurityException) { }
        }
        scanner = null
        callback = null
    }

    private fun requireGranted(permission: String) {
        if (ContextCompat.checkSelfPermission(appContext, permission) != PackageManager.PERMISSION_GRANTED) {
            throw SecurityException("Missing permission: " + permission)
        }
    }
}
