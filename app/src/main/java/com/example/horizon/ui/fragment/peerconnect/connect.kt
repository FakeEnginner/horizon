package com.example.horizon.ui.fragment.peerconnect

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.horizon.R
import com.example.horizon.databinding.FragmentConnectBinding
import com.example.horizon.utils.WebSocketManager
import org.json.JSONObject
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import org.webrtc.PeerConnection
import org.webrtc.EglBase

class Connect : Fragment() {

    private var _binding: FragmentConnectBinding? = null
    private val binding get() = _binding!!

    private var selectedUser: String? = null
    private val onlineUsers = mutableListOf<String>()
    private lateinit var userAdapter: UserAdapter

    private var webRTCClient: WebRTCClient? = null
    private val rootEglBase: EglBase = EglBase.create()

    // Track call state
    private var isCallInProgress = false
    private var currentCallTarget: String? = null
    private var isWebRTCInitialized = false
    private var isMuted = false
    private var isVideoEnabled = true

    // Buffer for ICE candidates received before peer connection is ready
    private val pendingIceCandidates = mutableListOf<IceCandidate>()

    companion object {
        private const val TAG = "Connect"
        private const val REQUEST_CODE_PERMISSIONS = 1001
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConnectBinding.inflate(inflater, container, false)
        val view = binding.root

        Log.d(TAG, "onCreateView: Initializing views")

        initializeViews()
        setupUserList()
        setupCallControls()
        setupWebSocketListeners()

        // CRITICAL FIX: Actually connect to WebSocket
        initializeWebSocketConnection()

        updateConnectionStatus()

        return view
    }

    private fun initializeViews() {
        // Initialize the SurfaceViewRenderers with the shared EGL context
        binding.localView.init(rootEglBase.eglBaseContext, null)
        binding.localView.setMirror(true)
        binding.localView.setEnableHardwareScaler(true)

        binding.remoteView.init(rootEglBase.eglBaseContext, null)
        binding.remoteView.setMirror(false)
        binding.remoteView.setEnableHardwareScaler(true)

        // Initially hide call status and show placeholder
        binding.callStatusContainer.visibility = View.GONE
        binding.noVideoPlaceholder.visibility = View.VISIBLE
    }

    private fun setupUserList() {
        userAdapter = UserAdapter(onlineUsers) { user ->
            selectedUser = user
            Log.d(TAG, "Selected user: $selectedUser")

            // Enable call button when user is selected
            binding.callButton.isEnabled = true
        }

        binding.userRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = userAdapter
        }

        // Initially disable call button
        binding.callButton.isEnabled = false
        updateUserCount()
    }

    private fun setupCallControls() {
        // Main call button
        binding.callButton.setOnClickListener {
            if (isCallInProgress) {
                // End call
                showCallEndConfirmation()
            } else {
                // Start call
                if (selectedUser == null) {
                    Toast.makeText(requireContext(), "Please select a user first", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (allPermissionsGranted()) {
                    selectedUser?.let { target ->
                        Log.d(TAG, "Initiating call to: $target")
                        startCall(target)
                    }
                } else {
                    requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
                }
            }
        }

        // Mute audio button
        binding.muteAudioButton.setOnClickListener {
            toggleMute()
        }

        // Toggle video button
        binding.toggleVideoButton.setOnClickListener {
            toggleVideo()
        }

        // Add refresh button functionality if it exists in your layout
        try {
            // This will only work if you have a refresh button in your layout
            binding.refreshUsersButton.setOnClickListener {
                refreshOnlineUsers()
            }
        } catch (e: Exception) {
            // Refresh button doesn't exist, that's fine
        }
    }

    // CRITICAL FIX: Added WebSocket connection initialization
    private fun initializeWebSocketConnection() {
        val currentUsername = getCurrentUsername()
        val accessToken = getAccessToken()

        if (currentUsername.isNullOrBlank()) {
            Log.e(TAG, "❌ Cannot connect WebSocket: missing username")
            Toast.makeText(requireContext(), "Please login again", Toast.LENGTH_LONG).show()
            return
        }

        if (accessToken.isNullOrBlank()) {
            Log.e(TAG, "❌ Cannot connect WebSocket: missing access token")
            Toast.makeText(requireContext(), "Session expired. Please login again", Toast.LENGTH_LONG).show()
            return
        }

        Log.d(TAG, "🔌 Connecting WebSocket for user: $currentUsername")

        WebSocketManager.connect(
            username = currentUsername,
            accessToken = accessToken,
            onConnected = {
                Log.d(TAG, "✅ WebSocket connected successfully")
                activity?.runOnUiThread {
                    updateConnectionStatus()
                    view?.postDelayed({
                        WebSocketManager.requestOnlineUsers()
                    }, 400)
                }
            },
            onError = { error ->
                Log.e(TAG, "❌ WebSocket connection failed: $error")
                activity?.runOnUiThread {
                    updateConnectionStatus()
                    Toast.makeText(requireContext(), "Connection failed: $error", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    private fun getCurrentUsername(): String? {
        return try {
            val preference = com.example.horizon.utils.MySharedPrefrence()
            val userJsonString = preference.getUserDetail(requireContext())
            if (userJsonString.isNullOrBlank()) return null
            val user = org.json.JSONObject(userJsonString)
            user.optString("username", null)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse username from preferences: ${e.message}")
            null
        }
    }

    private fun getAccessToken(): String? {
        return try {
            val preference = com.example.horizon.utils.MySharedPrefrence()
            preference.getAccessToken(requireContext())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load access token: ${e.message}")
            null
        }
    }

    private fun startCall(targetUser: String) {
        currentCallTarget = targetUser
        showLoadingOverlay("Connecting...")
        updateCallButton(isInCall = true)
        initializeWebRTC(targetUser, isCaller = true)
        sendCallRequest(targetUser)
    }

    // ENHANCED: Better WebSocket listeners with connection state management
    private fun setupWebSocketListeners() {
        // Connection state change listener
        WebSocketManager.onConnectionStateChanged = { connected ->
            Log.d(TAG, "🔄 Connection state changed: $connected")
            activity?.runOnUiThread {
                updateConnectionStatus()

                if (connected) {
                    // Request user list when connected
                    view?.postDelayed({
                        Log.d(TAG, "📋 Requesting online users after connection")
                        WebSocketManager.requestOnlineUsers()
                    }, 500)
                }
            }
        }

        // Message received listener
        WebSocketManager.onMessageReceived = { message ->
            Log.d(TAG, "📩 Received WebSocket message: $message")
            try {
                val json = JSONObject(message)
                val type = json.getString("type")
                Log.d(TAG, "📩 Message type: $type")

                when (type) {
                    "online_users" -> {
                        Log.d(TAG, "📋 Processing online users message")
                        handleOnlineUsers(json)
                    }
                    "call_request" -> handleCallRequest(json)
                    "call_accepted" -> handleCallAccepted(json)
                    "call_rejected" -> handleCallRejected(json)
                    "call_end" -> handleCallEnd(json)
                    "sdp_offer" -> handleSdpOffer(json)
                    "sdp_answer" -> handleSdpAnswer(json)
                    "ice_candidate" -> handleIceCandidate(json)
                    "call_error" -> handleCallError(json)
                    "pong" -> Log.d(TAG, "📍 Received pong response")
                    else -> Log.d(TAG, "❓ Unknown message type: $type")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error processing WebSocket message: ${e.message}", e)
            }
        }
    }

    // ENHANCED: Better online users handling with detailed logging
    private fun handleOnlineUsers(json: JSONObject) {
        try {
            val usersArray = json.getJSONArray("users")
            val newUsers = mutableListOf<String>()

            Log.d(TAG, "📋 Raw users from server: ${usersArray.length()} users")
            Log.d(TAG, "📋 Current username: ${WebSocketManager.username}")

            for (i in 0 until usersArray.length()) {
                val user = usersArray.getString(i)
                Log.d(TAG, "📋 Processing user: '$user'")

                // Filter out current user and empty strings
                if (user != WebSocketManager.username && user.isNotBlank()) {
                    newUsers.add(user)
                    Log.d(TAG, "📋   ✓ Added user: $user")
                } else {
                    Log.d(TAG, "📋   ✗ Filtered out: $user (current user or blank)")
                }
            }

            Log.d(TAG, "📋 Final filtered user list: $newUsers (${newUsers.size} users)")

            activity?.runOnUiThread {
                // Clear and update the list
                val oldSize = onlineUsers.size
                onlineUsers.clear()
                onlineUsers.addAll(newUsers)

                // Notify adapter of changes
                userAdapter.notifyDataSetChanged()
                updateUserCount()

                Log.d(TAG, "📋 UI updated: $oldSize -> ${newUsers.size} users")

                // Check if previously selected user is still online
                if (selectedUser != null && !newUsers.contains(selectedUser)) {
                    Log.d(TAG, "📋 Previously selected user '$selectedUser' is no longer online")
                    selectedUser = null
                    binding.callButton.isEnabled = false
                }

                // Show toast for user feedback
                if (newUsers.isEmpty()) {
                    Toast.makeText(requireContext(), "No other users online", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "${newUsers.size} users online", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error handling online users: ${e.message}", e)
            activity?.runOnUiThread {
                Toast.makeText(requireContext(), "Error updating user list", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ENHANCED: Manual refresh with better feedback
    private fun refreshOnlineUsers() {
        if (WebSocketManager.isConnected()) {
            Log.d(TAG, "🔄 Manually refreshing online users")
            Toast.makeText(requireContext(), "Refreshing users...", Toast.LENGTH_SHORT).show()
            WebSocketManager.requestOnlineUsers()

            // Also test connection
            WebSocketManager.testConnection()
        } else {
            Log.w(TAG, "❌ Cannot refresh users - WebSocket not connected")
            Toast.makeText(requireContext(), "Not connected to server", Toast.LENGTH_SHORT).show()

            // Try to reconnect
            Toast.makeText(requireContext(), "Attempting to reconnect...", Toast.LENGTH_SHORT).show()
            initializeWebSocketConnection()
        }
    }

    private fun handleCallRequest(json: JSONObject) {
        val fromUser = json.getString("from")
        Log.d(TAG, "Incoming call from: $fromUser")
        activity?.runOnUiThread {
            showIncomingCallDialog(fromUser)
        }
    }

    private fun handleCallAccepted(json: JSONObject) {
        val fromUser = json.getString("from")
        Log.d(TAG, "Call accepted by: $fromUser")
        activity?.runOnUiThread {
            hideLoadingOverlay()
            showCallStatus("Connected")
            createAndSendOffer(fromUser)
        }
    }

    private fun handleCallRejected(json: JSONObject) {
        val fromUser = json.getString("from")
        Log.d(TAG, "Call rejected by: $fromUser")
        activity?.runOnUiThread {
            hideLoadingOverlay()
            Toast.makeText(requireContext(), "Call rejected by $fromUser", Toast.LENGTH_SHORT).show()
            endCall()
        }
    }

    private fun handleCallEnd(json: JSONObject) {
        val fromUser = json.getString("from")
        Log.d(TAG, "Call ended by: $fromUser")
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), "Call ended", Toast.LENGTH_SHORT).show()
            endCall()
        }
    }

    private fun handleSdpOffer(json: JSONObject) {
        val sdp = json.getString("sdp")
        val fromUser = json.getString("from")
        Log.d(TAG, "Received SDP offer from: $fromUser")

        if (webRTCClient == null || !isWebRTCInitialized) {
            Log.d(TAG, "WebRTC not initialized, initializing now for answerer")
            currentCallTarget = fromUser
            initializeWebRTC(fromUser, isCaller = false)

            waitForWebRTCInitialization {
                handleRemoteOffer(sdp)
            }
        } else {
            handleRemoteOffer(sdp)
        }
    }

    private fun handleSdpAnswer(json: JSONObject) {
        val sdp = json.getString("sdp")
        Log.d(TAG, "Received SDP answer")
        handleRemoteAnswer(sdp)
    }

    private fun handleIceCandidate(json: JSONObject) {
        val candidate = json.getString("candidate")
        val sdpMid = json.getString("sdpMid")
        val sdpMLineIndex = json.getInt("sdpMLineIndex")
        Log.d(TAG, "Received ICE candidate")

        val iceCandidate = IceCandidate(sdpMid, sdpMLineIndex, candidate)

        if (webRTCClient != null && isWebRTCInitialized) {
            webRTCClient?.addIceCandidate(iceCandidate)
        } else {
            Log.w(TAG, "WebRTC not ready, buffering ICE candidate")
            pendingIceCandidates.add(iceCandidate)
        }
    }

    private fun handleCallError(json: JSONObject) {
        val message = json.getString("message")
        Log.e(TAG, "Call error: $message")
        activity?.runOnUiThread {
            hideLoadingOverlay()
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            endCall()
        }
    }

    private fun initializeWebRTC(targetUser: String, isCaller: Boolean) {
        Log.d(TAG, "Initializing WebRTC for ${if (isCaller) "caller" else "answerer"}")

        isCallInProgress = true
        currentCallTarget = targetUser
        isWebRTCInitialized = false
        pendingIceCandidates.clear()

        webRTCClient = WebRTCClient(
            requireContext(),
            binding.localView,
            binding.remoteView,
            rootEglBase
        )

        webRTCClient?.onLocalTracksReady = {
            Log.d(TAG, "Local tracks ready, creating peer connection")

            val iceServers = listOf(
                PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
                PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer(),
                PeerConnection.IceServer.builder("turn:numb.viagenie.ca")
                    .setUsername("webrtc@live.com")
                    .setPassword("muazkh")
                    .createIceServer()
            )

            webRTCClient?.createPeerConnection(iceServers) { candidate ->
                Log.d(TAG, "Sending ICE candidate to: $targetUser")
                sendIceCandidate(targetUser, candidate)
            }

            isWebRTCInitialized = true
            processPendingIceCandidates()

            activity?.runOnUiThread {
                binding.noVideoPlaceholder.visibility = View.GONE
                binding.localVideoPlaceholder.visibility = View.GONE
            }
        }

        // Set up remote stream callback
        webRTCClient?.onRemoteStreamReceived = {
            activity?.runOnUiThread {
                binding.noVideoPlaceholder.visibility = View.GONE
                showCallStatus("Connected")
            }
        }
    }

    private fun waitForWebRTCInitialization(callback: () -> Unit) {
        if (isWebRTCInitialized) {
            callback()
        } else {
            var attempts = 0
            val maxAttempts = 50

            val checkInitialization = object : Runnable {
                override fun run() {
                    attempts++
                    if (isWebRTCInitialized) {
                        callback()
                    } else if (attempts < maxAttempts) {
                        view?.postDelayed(this, 100)
                    } else {
                        Log.e(TAG, "WebRTC initialization timeout")
                        endCall()
                    }
                }
            }
            view?.postDelayed(checkInitialization, 100)
        }
    }

    private fun processPendingIceCandidates() {
        Log.d(TAG, "Processing ${pendingIceCandidates.size} buffered ICE candidates")
        pendingIceCandidates.forEach { candidate ->
            webRTCClient?.addIceCandidate(candidate)
        }
        pendingIceCandidates.clear()
    }

    private fun createAndSendOffer(targetUser: String) {
        if (!isWebRTCInitialized) {
            Log.w(TAG, "Cannot create offer, WebRTC not initialized")
            return
        }

        Log.d(TAG, "Creating offer for: $targetUser")
        webRTCClient?.createOffer { sdp ->
            Log.d(TAG, "Offer created, sending to: $targetUser")
            sendSdpOffer(targetUser, sdp.description)
        }
    }

    private fun toggleMute() {
        isMuted = !isMuted
        webRTCClient?.toggleAudio(!isMuted)

        // Update UI
        val iconRes = if (isMuted) R.drawable.ic_mic_off else R.drawable.ic_mic
        val colorRes = if (isMuted) R.color.error_red else R.color.primary_green

        binding.muteAudioButton.setImageResource(iconRes)
        binding.muteAudioButton.backgroundTintList = ContextCompat.getColorStateList(requireContext(), colorRes)

        Toast.makeText(requireContext(), if (isMuted) "Muted" else "Unmuted", Toast.LENGTH_SHORT).show()
    }

    private fun toggleVideo() {
        isVideoEnabled = !isVideoEnabled
        webRTCClient?.toggleVideo(isVideoEnabled)

        // Update UI
        val iconRes = if (isVideoEnabled) R.drawable.ic_videocam else R.drawable.ic_videocam_off
        val colorRes = if (isVideoEnabled) R.color.primary_green else R.color.error_red

        binding.toggleVideoButton.setImageResource(iconRes)
        binding.toggleVideoButton.backgroundTintList = ContextCompat.getColorStateList(requireContext(), colorRes)

        // Show/hide local video placeholder
        binding.localVideoPlaceholder.visibility = if (isVideoEnabled) View.GONE else View.VISIBLE
    }

    private fun showCallEndConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("End Call")
            .setMessage("Are you sure you want to end this call?")
            .setPositiveButton("End Call") { _, _ ->
                sendCallEndMessage()
                endCall()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showLoadingOverlay(message: String) {
        binding.loadingOverlay.visibility = View.VISIBLE
        binding.loadingText.text = message
    }

    private fun hideLoadingOverlay() {
        binding.loadingOverlay.visibility = View.GONE
    }

    private fun showCallStatus(status: String) {
        binding.callStatusContainer.visibility = View.VISIBLE
        binding.callStatusText.text = status
    }

    private fun hideCallStatus() {
        binding.callStatusContainer.visibility = View.GONE
    }

    private fun updateCallButton(isInCall: Boolean) {
        if (isInCall) {
            binding.callButton.setImageResource(R.drawable.ic_call_end)
            binding.callButton.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.error_red)
        } else {
            binding.callButton.setImageResource(R.drawable.ic_call)
            binding.callButton.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.primary_green)
            binding.callButton.isEnabled = selectedUser != null
        }
    }

    private fun updateUserCount() {
        binding.userCount.text = onlineUsers.size.toString()
    }

    // ENHANCED: Better connection status with more detailed info
    private fun updateConnectionStatus() {
        val isConnected = WebSocketManager.isConnected()
        val status = if (isConnected) "Online" else "Offline"
        val colorRes = if (isConnected) R.color.primary_green else R.color.error_red

        binding.connectionStatus.text = status
        binding.connectionStatus.setTextColor(ContextCompat.getColor(requireContext(), colorRes))

        Log.d(TAG, "🔄 Connection status updated: $status")
    }

    // WebSocket message sending methods
    private fun sendCallRequest(target: String) {
        Log.d(TAG, "Sending call request to: $target")
        val json = JSONObject()
        json.put("type", "call_request")
        json.put("from", WebSocketManager.username)
        json.put("to", target)
        WebSocketManager.sendMessage(json.toString())
    }

    private fun sendCallResponse(target: String, accepted: Boolean) {
        Log.d(TAG, "Sending call response to: $target, accepted: $accepted")
        val json = JSONObject()
        json.put("type", if (accepted) "call_accepted" else "call_rejected")
        json.put("from", WebSocketManager.username)
        json.put("to", target)
        WebSocketManager.sendMessage(json.toString())
    }

    private fun sendCallEndMessage() {
        currentCallTarget?.let { target ->
            Log.d(TAG, "Sending call end to: $target")
            val json = JSONObject()
            json.put("type", "call_end")
            json.put("from", WebSocketManager.username)
            json.put("to", target)
            WebSocketManager.sendMessage(json.toString())
        }
    }

    private fun sendSdpOffer(target: String, sdp: String) {
        val json = JSONObject()
        json.put("type", "sdp_offer")
        json.put("to", target)
        json.put("from", WebSocketManager.username)
        json.put("sdp", sdp)
        WebSocketManager.sendMessage(json.toString())
    }

    private fun sendSdpAnswer(target: String, sdp: String) {
        val json = JSONObject()
        json.put("type", "sdp_answer")
        json.put("to", target)
        json.put("from", WebSocketManager.username)
        json.put("sdp", sdp)
        WebSocketManager.sendMessage(json.toString())
    }

    private fun sendIceCandidate(target: String, candidate: IceCandidate) {
        val json = JSONObject()
        json.put("type", "ice_candidate")
        json.put("to", target)
        json.put("from", WebSocketManager.username)
        json.put("candidate", candidate.sdp)
        json.put("sdpMid", candidate.sdpMid)
        json.put("sdpMLineIndex", candidate.sdpMLineIndex)
        WebSocketManager.sendMessage(json.toString())
    }

    private fun showIncomingCallDialog(fromUser: String) {
        if (isCallInProgress) {
            Log.w(TAG, "Already in a call, rejecting incoming call from: $fromUser")
            sendCallResponse(fromUser, false)
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Incoming Call")
            .setMessage("$fromUser is calling you.")
            .setPositiveButton("Accept") { _, _ ->
                Log.d(TAG, "Accepting call from: $fromUser")
                if (allPermissionsGranted()) {
                    sendCallResponse(fromUser, true)
                    currentCallTarget = fromUser
                    updateCallButton(isInCall = true)
                    showLoadingOverlay("Connecting...")
                    initializeWebRTC(fromUser, isCaller = false)
                } else {
                    requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
                }
            }
            .setNegativeButton("Reject") { dialog, _ ->
                Log.d(TAG, "Rejecting call from: $fromUser")
                sendCallResponse(fromUser, false)
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun handleRemoteOffer(sdp: String) {
        if (!isWebRTCInitialized) {
            Log.w(TAG, "Cannot handle remote offer, WebRTC not initialized")
            return
        }

        Log.d(TAG, "Handling remote offer")
        val session = SessionDescription(SessionDescription.Type.OFFER, sdp)
        webRTCClient?.setRemoteDescription(session) {
            Log.d(TAG, "Remote offer set, creating answer")
            webRTCClient?.createAnswer { answer ->
                Log.d(TAG, "Answer created, sending to: $currentCallTarget")
                currentCallTarget?.let { target ->
                    sendSdpAnswer(target, answer.description)
                }
            }
        }
    }

    private fun handleRemoteAnswer(sdp: String) {
        if (!isWebRTCInitialized) {
            Log.w(TAG, "Cannot handle remote answer, WebRTC not initialized")
            return
        }

        Log.d(TAG, "Handling remote answer")
        val session = SessionDescription(SessionDescription.Type.ANSWER, sdp)
        webRTCClient?.setRemoteDescription(session) {
            Log.d(TAG, "Remote answer set successfully")
        }
    }

    private fun endCall() {
        Log.d(TAG, "Ending call")
        isCallInProgress = false
        isWebRTCInitialized = false
        currentCallTarget = null
        pendingIceCandidates.clear()

        webRTCClient?.close()
        webRTCClient = null

        // Reset UI state
        activity?.runOnUiThread {
            hideLoadingOverlay()
            hideCallStatus()
            updateCallButton(isInCall = false)

            // Reset audio/video buttons
            isMuted = false
            isVideoEnabled = true
            binding.muteAudioButton.setImageResource(R.drawable.ic_mic)
            binding.muteAudioButton.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.primary_green)
            binding.toggleVideoButton.setImageResource(R.drawable.ic_videocam)
            binding.toggleVideoButton.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.primary_green)

            // Show placeholders
            binding.noVideoPlaceholder.visibility = View.VISIBLE
            binding.localVideoPlaceholder.visibility = View.GONE
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS && allPermissionsGranted()) {
            selectedUser?.let {
                startCall(it)
            }
        } else {
            Toast.makeText(requireContext(), "Permissions required for video calling", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh users when fragment becomes visible
        Log.d(TAG, "🔄 Fragment resumed, refreshing connection status")
        updateConnectionStatus()

        // If connected, refresh user list
        if (WebSocketManager.isConnected()) {
            view?.postDelayed({
                refreshOnlineUsers()
            }, 500)
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "⏸️ Fragment paused")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: Cleaning up")

        if (isCallInProgress) {
            sendCallEndMessage()
        }

        endCall()

        try {
            binding.localView.release()
            binding.remoteView.release()
            rootEglBase.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing video resources: ${e.message}")
        }

        // Disconnect socket for this screen and clear listeners
        WebSocketManager.disconnect()
        WebSocketManager.onMessageReceived = null
        WebSocketManager.onConnectionStateChanged = null

        _binding = null
        Log.d(TAG, "✅ Fragment cleanup complete")
    }
}