package com.example.horizon.utils

import android.util.Log
import okhttp3.*
import okio.ByteString
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object WebSocketManager {
    private const val TAG = "WebSocketManager"
    private const val SERVER_URL = "ws://192.168.1.13:3000"

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .build()

    var username: String? = null
    var onMessageReceived: ((String) -> Unit)? = null
    var onConnectionStateChanged: ((Boolean) -> Unit)? = null
    private var isConnected = false

    fun connect(username: String, onConnected: () -> Unit, onError: (String) -> Unit) {
        if (isConnected) {
            Log.w(TAG, "Already connected")
            onConnected()
            return
        }

        this.username = username
        Log.d(TAG, "Connecting to WebSocket as user: $username")

        val request = Request.Builder()
            .url(SERVER_URL)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "✅ WebSocket connection opened")
                isConnected = true
                onConnectionStateChanged?.invoke(true)

                // Send registration message - using both formats for compatibility
                val registerMessage = JSONObject().apply {
                    put("type", "register")
                    put("username", username)
                }.toString()

                val storeUserMessage = JSONObject().apply {
                    put("type", "store_user")
                    put("username", username)
                }.toString()

                // Send both messages to ensure compatibility
                webSocket.send(registerMessage)
                webSocket.send(storeUserMessage)

                Log.d(TAG, "Sent registration messages for user: $username")
                onConnected()

                // Request online users list
                requestOnlineUsers()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "📩 Received message: $text")
                try {
                    // Parse and potentially transform the message
                    val transformedMessage = transformMessage(text)
                    onMessageReceived?.invoke(transformedMessage)
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing message: ${e.message}")
                    // Still forward the original message
                    onMessageReceived?.invoke(text)
                }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                Log.d(TAG, "📩 Received bytes: ${bytes.hex()}")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closing: $code / $reason")
                isConnected = false
                onConnectionStateChanged?.invoke(false)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closed: $code / $reason")
                isConnected = false
                onConnectionStateChanged?.invoke(false)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "❌ WebSocket failure: ${t.message}")
                isConnected = false
                onConnectionStateChanged?.invoke(false)
                onError(t.message ?: "Unknown error")
            }
        })
    }

    private fun transformMessage(originalMessage: String): String {
        return try {
            val json = JSONObject(originalMessage)

            // Handle different message types and transform if needed
            when {
                // If server sends user list in a different format, transform it
                json.has("users") || json.has("online_users") -> {
                    val users = when {
                        json.has("users") -> json.get("users")
                        json.has("online_users") -> json.get("online_users")
                        else -> JSONArray()
                    }

                    // Ensure consistent format
                    JSONObject().apply {
                        put("type", "online_users")
                        put("users", users)
                    }.toString()
                }

                // Pass through other message types as-is
                else -> originalMessage
            }
        } catch (e: Exception) {
            // If parsing fails, return original message
            originalMessage
        }
    }

    fun requestOnlineUsers() {
        if (!isConnected) {
            Log.w(TAG, "Cannot request users, not connected")
            return
        }

        // Try multiple request formats to ensure compatibility
        val getUsersMessage = JSONObject().apply {
            put("type", "get_users")
        }.toString()

        val requestUsersMessage = JSONObject().apply {
            put("type", "request_online_users")
        }.toString()

        sendMessage(getUsersMessage)
        sendMessage(requestUsersMessage)

        Log.d(TAG, "Requested online users list")
    }

    fun sendMessage(message: String) {
        if (isConnected && webSocket != null) {
            webSocket?.send(message)
            Log.d(TAG, "📤 Sent message: $message")
        } else {
            Log.w(TAG, "Cannot send message, WebSocket not connected")
        }
    }

    fun sendMessageToUser(to: String, message: String) {
        val msg = JSONObject().apply {
            put("type", "send_to_user")
            put("to", to)
            put("message", message)
            put("from", username)
        }.toString()
        sendMessage(msg)
    }

    fun broadcast(message: String) {
        val msg = JSONObject().apply {
            put("type", "broadcast")
            put("message", message)
            put("from", username)
        }.toString()
        sendMessage(msg)
    }

    fun disconnect() {
        webSocket?.close(1000, "Client disconnect")
        isConnected = false
        onConnectionStateChanged?.invoke(false)
        Log.d(TAG, "WebSocket disconnected")
    }

    fun isConnected(): Boolean = isConnected

    // Test methods
    fun simulateOnlineUsers(users: List<String>) {
        val message = JSONObject().apply {
            put("type", "online_users")
            put("users", JSONArray(users))
        }.toString()

        Log.d(TAG, "🧪 Simulating online users: $message")
        onMessageReceived?.invoke(message)
    }

    fun testConnection() {
        if (isConnected) {
            val testMessage = JSONObject().apply {
                put("type", "ping")
                put("timestamp", System.currentTimeMillis())
            }.toString()
            sendMessage(testMessage)
        }
    }
}