package com.example.horizon.utils

import okhttp3.*
import okio.ByteString

class WebSocketClient {

    private val client = OkHttpClient()
    private lateinit var ws: WebSocket

    fun connect(username: String) {
        val request = Request.Builder()
            .url("ws://192.168.1.13:3000") // replace with your IP
            .build()

        ws = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                println("✅ WebSocket Connected")

                // Send store_user message
                val msg = """
                    {
                        "type": "store_user",
                        "username": "$username"
                    }
                """.trimIndent()
                ws.send(msg)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                println("📩 From server: $text")
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                super.onMessage(webSocket, bytes)
                println("📩 From server (bytes): $bytes")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosing(webSocket, code, reason)
                webSocket.close(1000, null)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                println("❌ WebSocket Error: ${t.message}")
            }
        })
    }

    fun sendMessage(to: String, message: String) {
        val msg = """
            {
                "type": "send_to_user",
                "to": "$to",
                "message": "$message"
            }
        """.trimIndent()
        ws.send(msg)
    }

    fun broadcast(message: String) {
        val msg = """
            {
                "type": "broadcast",
                "message": "$message"
            }
        """.trimIndent()
        ws.send(msg)
    }
}
