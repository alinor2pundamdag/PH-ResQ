package com.phresq.vanguard.data.mesh

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.p2p.WifiP2pManager
import android.util.Log

@SuppressLint("MissingPermission")
class WifiDirectMeshManager(
    private val context: Context,
    private val onPeerConnected: (String) -> Unit
) {
    private val manager: WifiP2pManager? = context.getSystemService(Context.WIFI_P2P_SERVICE) as? WifiP2pManager
    private var channel: WifiP2pManager.Channel? = null

    init {
        channel = manager?.initialize(context, context.mainLooper) {
            Log.i("WifiDirectMesh", "Wi-Fi P2P channel disconnected.")
        }
    }

    fun discoverPeers() {
        manager?.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.i("WifiDirectMesh", "Peer discovery initiated successfully.")
            }
            override fun onFailure(reason: Int) {
                Log.e("WifiDirectMesh", "Peer discovery failed: $reason")
            }
        })
    }

    fun createGroup() {
        manager?.createGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Log.i("WifiDirectMesh", "Wi-Fi Direct Group created successfully.")
            }
            override fun onFailure(reason: Int) {
                Log.e("WifiDirectMesh", "Failed to create group: $reason")
            }
        })
    }

    fun stopDiscovery() {
        manager?.stopPeerDiscovery(channel, null)
    }
}
