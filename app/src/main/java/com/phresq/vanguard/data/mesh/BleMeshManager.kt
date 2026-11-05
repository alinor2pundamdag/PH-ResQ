package com.phresq.vanguard.data.mesh

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.nio.ByteBuffer
import java.security.MessageDigest
import kotlin.math.pow

data class PeerNode(
    val nodeIdHash: String,
    val rssi: Int,
    val estimatedDistanceMeters: Double,
    val emergencyStatus: Byte,
    val batteryLevel: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@SuppressLint("MissingPermission")
class BleMeshManager(private val context: Context) {

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private val bleAdvertiser: BluetoothLeAdvertiser? = bluetoothAdapter?.bluetoothLeAdvertiser
    private val bleScanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner

    private val _discoveredPeers = MutableStateFlow<Map<String, PeerNode>>(emptyMap())
    val discoveredPeers: StateFlow<Map<String, PeerNode>> = _discoveredPeers

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isScanning = false
    private var isAdvertising = false

    companion object {
        private const val TAG = "BleMeshManager"
        private val PH_RESQ_UUID = ParcelUuid.fromString("00005052-0000-1000-8000-00805F9B34FB") // "PR" magic header
        private const val TX_POWER_1M = -59 // Calibrated RSSI at 1 meter
        private const val PATH_LOSS_EXPONENT = 2.4 // Indoor / urban obstruction factor
    }

    fun startMeshNode(deviceId: String, emergencyStatus: Byte, batteryLevel: Int) {
        startAdvertising(deviceId, emergencyStatus, batteryLevel)
        startThrottledScanning()
    }

    private fun startAdvertising(deviceId: String, emergencyStatus: Byte, batteryLevel: Int) {
        if (bleAdvertiser == null || isAdvertising) return

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(true)
            .build()

        val hash = MessageDigest.getInstance("SHA-256").digest(deviceId.toByteArray())
        val shortHash = hash.copyOfRange(0, 4)

        val payload = ByteBuffer.allocate(10)
            .put(shortHash)
            .put(emergencyStatus)
            .put(batteryLevel.toByte())
            .putInt((System.currentTimeMillis() / 1000).toInt())
            .array()

        val data = AdvertiseData.Builder()
            .addServiceUuid(PH_RESQ_UUID)
            .addManufacturerData(0x02E0, payload)
            .setIncludeDeviceName(false)
            .build()

        bleAdvertiser.startAdvertising(settings, data, advertiseCallback)
        isAdvertising = true
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            Log.i(TAG, "BLE Mesh Advertising Started Successfully.")
        }
        override fun onStartFailure(errorCode: Int) {
            Log.e(TAG, "BLE Mesh Advertising Failed: $errorCode")
        }
    }

    private fun startThrottledScanning() {
        if (bleScanner == null || isScanning) return
        isScanning = true

        scope.launch {
            while (isActive && isScanning) {
                val settings = ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build()
                
                val filter = ScanFilter.Builder()
                    .setServiceUuid(PH_RESQ_UUID)
                    .build()

                try {
                    bleScanner.startScan(listOf(filter), settings, scanCallback)
                    delay(15_000)
                    bleScanner.stopScan(scanCallback)
                } catch (e: Exception) {
                    Log.e(TAG, "Scan error: ${e.localizedMessage}")
                }

                delay(45_000)
            }
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val mfgData = result.scanRecord?.getManufacturerSpecificData(0x02E0) ?: return
            if (mfgData.size >= 10) {
                val hashBytes = mfgData.copyOfRange(0, 4)
                val status = mfgData[4]
                val battery = mfgData[5].toInt() and 0xFF
                val hashHex = hashBytes.joinToString("") { "%02x".format(it) }

                val distance = calculateDistance(result.rssi, TX_POWER_1M)
                val node = PeerNode(hashHex, result.rssi, distance, status, battery)

                val currentMap = _discoveredPeers.value.toMutableMap()
                currentMap[hashHex] = node
                _discoveredPeers.value = currentMap
            }
        }
    }

    private fun calculateDistance(rssi: Int, txPower: Int): Double {
        if (rssi == 0) return -1.0
        val ratio = (txPower - rssi) / (10.0 * PATH_LOSS_EXPONENT)
        return 10.0.pow(ratio)
    }

    fun stopMesh() {
        isScanning = false
        try {
            bleAdvertiser?.stopAdvertising(advertiseCallback)
            bleScanner?.stopScan(scanCallback)
        } catch (_: Exception) {}
        scope.cancel()
    }
}
