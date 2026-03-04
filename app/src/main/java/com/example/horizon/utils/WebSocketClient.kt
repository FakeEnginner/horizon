package com.example.horizon.utils

/**
 * Legacy wrapper kept for compatibility. Prefer [WebSocketManager].
 */
@Deprecated("Use WebSocketManager instead")
class WebSocketClient {

    fun connect(username: String, accessToken: String, onError: (String) -> Unit = {}) {
        WebSocketManager.connect(
            username = username,
            accessToken = accessToken,
            onConnected = {},
            onError = onError
        )
    }

    fun sendMessage(to: String, message: String) {
        val from = WebSocketManager.username ?: return
        WebSocketManager.sendMessage(
            """
            {
                "type": "send_to_user",
                "to": "$to",
                "from": "$from",
                "message": "$message"
            }
            """.trimIndent()
        )
    }

    fun broadcast(message: String) {
        val from = WebSocketManager.username ?: return
        WebSocketManager.sendMessage(
            """
            {
                "type": "broadcast",
                "from": "$from",
                "message": "$message"
            }
            """.trimIndent()
        )
    }
}
