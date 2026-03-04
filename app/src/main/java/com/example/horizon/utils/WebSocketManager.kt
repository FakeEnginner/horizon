package com.example.horizon.utils

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object WebSocketManager {
    private const val TAG = "WebSocketManager"

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .build()

    var username: String? = null
    var onMessageReceived: ((String) -> Unit)? = null
    var onConnectionStateChanged: ((Boolean) -> Unit)? = null

    @Volatile
    private var isConnected = false
    private var lastToken: String? = null

    @Synchronized
    fun connect(
        username: String,
        accessToken: String?,
        onConnected: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (username.isBlank()) {
            onError("Invalid username")
            return
        }

        if (accessToken.isNullOrBlank()) {
            onError("Missing access token")
            return
        }

        if (isConnected && this.username == username) {
            Log.d(TAG, "Already connected as $username")
            onConnected()
            return
        }

        if (isConnected && this.username != username) {
            Log.d(TAG, "Connected as different user. Reconnecting.")
            disconnect()
        } else if (webSocket != null) {
            disconnect()
        }

        this.username = username
        this.lastToken = accessToken

        val wsUrl = buildWebSocketUrl()
        Log.d(TAG, "Connecting to WebSocket as user: $username")
        Log.d(TAG, "WebSocket endpoint: $wsUrl")

        val request = Request.Builder()
            .url(wsUrl)
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        val requestBuilder = Request.Builder().url(wsUrl)
        if (!accessToken.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $accessToken")
        }

        webSocket = client.newWebSocket(requestBuilder.build(), object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "✅ WebSocket connection opened")
                isConnected = true
                onConnectionStateChanged?.invoke(true)

                val storeUserMessage = JSONObject().apply {
                    put("type", "store_user")
                    put("username", username)
                }.toString()

                webSocket.send(storeUserMessage)
                requestOnlineUsers()
                onConnected()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "📩 Received message: $text")
                onMessageReceived?.invoke(text)
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                Log.d(TAG, "Received binary message of ${bytes.size} bytes")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                isConnected = false
                onConnectionStateChanged?.invoke(false)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                isConnected = false
                onConnectionStateChanged?.invoke(false)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                val serverCode = response?.code
                val message = if (serverCode != null) {
                    "${t.message ?: "WebSocket failure"} (HTTP $serverCode)"
                } else {
                    t.message ?: "Unknown error"
                }

                Log.e(TAG, "❌ WebSocket failure: $message")
                isConnected = false
                this@WebSocketManager.webSocket = null
                onConnectionStateChanged?.invoke(false)
                onError(message)
            }
        })
    }

    private fun buildWebSocketUrl(): String {
        val base = Utility.apiUrls.trim().removeSuffix("/")
        return when {
            base.startsWith("https://") -> "wss://${base.removePrefix("https://")}"
            base.startsWith("http://") -> "ws://${base.removePrefix("http://")}"
            base.startsWith("wss://") || base.startsWith("ws://") -> base
            else -> "ws://$base"
        }
    }

    fun requestOnlineUsers() {
        if (!isConnected) {
            Log.w(TAG, "Cannot request users, not connected")
            return
        }

        val requestUsersMessage = JSONObject().apply {
            put("type", "request_online_users")
            put("from", username)
        }.toString()

        sendMessage(requestUsersMessage)
        Log.d(TAG, "Requested online users list")
    }

    fun sendMessage(message: String) {
        val socket = webSocket
        if (isConnected && socket != null) {
            socket.send(message)
        } else {
            Log.w(TAG, "Cannot send message, WebSocket not connected")
        }
    }

    fun testConnection() {
        if (!isConnected) return
        val pingMessage = JSONObject().apply {
            put("type", "ping")
            put("from", username)
            put("timestamp", System.currentTimeMillis())
        }.toString()
        sendMessage(pingMessage)
    }

    @Synchronized
    fun disconnect() {
        webSocket?.close(1000, "Client disconnect")
        webSocket = null
        isConnected = false
        onConnectionStateChanged?.invoke(false)
        Log.d(TAG, "WebSocket disconnected")
    }

    fun isConnected(): Boolean = isConnected
}
