package com.example.citymind.data.remote

import android.util.Log
import com.example.citymind.data.remote.dtos.ComplaintDto
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONObject

data class SocketStatusUpdatePayload(
    val complaintId: String,
    val status: String,
    val message: String? = null,
    val updatedAt: String? = null,
    val complaint: ComplaintDto? = null
)

object SocketManager {
    private const val TAG = "SOCKET"
    private var socket: Socket? = null
    private val gson = Gson()

    fun getSocketUrl(): String {
        val base = ApiConfig.BASE_URL
        return if (base.endsWith("/api/")) {
            base.substring(0, base.length - 5)
        } else if (base.endsWith("/api")) {
            base.substring(0, base.length - 4)
        } else {
            base
        }
    }

    @Synchronized
    fun connect() {
        if (socket == null || !socket!!.connected()) {
            try {
                val url = getSocketUrl()
                Log.d(TAG, "Connecting Socket.IO to $url...")
                val opts = IO.Options().apply {
                    reconnection = true
                    reconnectionAttempts = Int.MAX_VALUE
                    reconnectionDelay = 1000
                }
                socket = IO.socket(url, opts)

                socket?.on(Socket.EVENT_CONNECT) {
                    Log.d(TAG, "Connected to Socket.IO backend")
                }

                socket?.on(Socket.EVENT_DISCONNECT) {
                    Log.d(TAG, "Disconnected from Socket.IO backend")
                }

                socket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
                    Log.e(TAG, "Socket connection error: ${args.getOrNull(0)}")
                }

                socket?.connect()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize socket: ${e.message}", e)
            }
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket = null
        Log.d(TAG, "Disconnected Socket.IO")
    }

    fun isConnected(): Boolean {
        return socket?.connected() == true
    }

    fun joinComplaintRoom(complaintId: String) {
        connect()
        if (complaintId.isNotEmpty()) {
            Log.d(TAG, "Joining complaint room: $complaintId")
            socket?.emit("joinComplaint", complaintId)
        }
    }

    fun leaveComplaintRoom(complaintId: String) {
        if (complaintId.isNotEmpty()) {
            Log.d(TAG, "Leaving complaint room: $complaintId")
            socket?.emit("leaveComplaint", complaintId)
        }
    }

    fun observeStatusUpdates(): Flow<SocketStatusUpdatePayload> = callbackFlow {
        connect()
        val listener = io.socket.emitter.Emitter.Listener { args ->
            try {
                if (args.isNotEmpty()) {
                    val rawJson = args[0].toString()
                    Log.d(TAG, "Status update received: $rawJson")
                    val jsonObj = JSONObject(rawJson)
                    val complaintId = jsonObj.optString("complaintId", "")
                    val status = jsonObj.optString("status", "")
                    val message = jsonObj.optString("message", "")
                    val updatedAt = jsonObj.optString("updatedAt", "")

                    var complaintDto: ComplaintDto? = null
                    if (jsonObj.has("complaint")) {
                        try {
                            complaintDto = gson.fromJson(jsonObj.getJSONObject("complaint").toString(), ComplaintDto::class.java)
                        } catch (ex: Exception) {
                            // fallback
                        }
                    }

                    val payload = SocketStatusUpdatePayload(
                        complaintId = complaintId,
                        status = status,
                        message = message,
                        updatedAt = updatedAt,
                        complaint = complaintDto
                    )
                    trySend(payload)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing status update event: ${e.message}", e)
            }
        }

        socket?.on("complaint:statusUpdated", listener)

        awaitClose {
            socket?.off("complaint:statusUpdated", listener)
        }
    }
}
