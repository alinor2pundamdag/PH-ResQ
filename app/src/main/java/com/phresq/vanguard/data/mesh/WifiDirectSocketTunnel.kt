package com.phresq.vanguard.data.mesh

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.ByteBuffer

class WifiDirectSocketTunnel(private val port: Int = 8988) {

    companion object {
        private const val TAG = "WifiDirectSocketTunnel"
    }

    suspend fun startServer(onPayloadReceived: (ByteArray) -> Unit) = withContext(Dispatchers.IO) {
        try {
            val serverSocket = ServerSocket(port)
            while (true) {
                val clientSocket = serverSocket.accept()
                handleClient(clientSocket, onPayloadReceived)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Server error: ${e.localizedMessage}")
        }
    }

    private fun handleClient(socket: Socket, onPayloadReceived: (ByteArray) -> Unit) {
        try {
            val inputStream: InputStream = socket.getInputStream()
            val sizeBuffer = ByteArray(4)
            inputStream.read(sizeBuffer)
            val size = ByteBuffer.wrap(sizeBuffer).int

            val payload = ByteArray(size)
            var totalRead = 0
            while (totalRead < size) {
                val read = inputStream.read(payload, totalRead, size - totalRead)
                if (read == -1) break
                totalRead += read
            }
            socket.close()
            onPayloadReceived(payload)
        } catch (e: Exception) {
            Log.e(TAG, "Client handling error: ${e.localizedMessage}")
        }
    }

    suspend fun sendPayload(address: String, payload: ByteArray): Boolean = withContext(Dispatchers.IO) {
        try {
            val socket = Socket()
            socket.connect(InetSocketAddress(address, port), 5000)
            val outputStream: OutputStream = socket.getOutputStream()
            
            val sizeHeader = ByteBuffer.allocate(4).putInt(payload.size).array()
            outputStream.write(sizeHeader)
            outputStream.write(payload)
            outputStream.flush()
            socket.close()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Send failed: ${e.localizedMessage}")
            false
        }
    }
}
