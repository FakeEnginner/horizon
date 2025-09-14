package com.example.horizon.ui.fragment.peerconnect

import android.content.Context
import android.util.Log
import org.webrtc.*

class WebRTCClient(
    private val context: Context,
    private val localView: SurfaceViewRenderer,
    private val remoteView: SurfaceViewRenderer,
    private val eglBase: EglBase // Now passed from Fragment
) {

    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var videoCapturer: VideoCapturer? = null
    private var localVideoTrack: VideoTrack? = null
    private var localAudioTrack: AudioTrack? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null
    private var videoSource: VideoSource? = null
    private var audioSource: AudioSource? = null

    // Callback when local tracks are ready
    var onLocalTracksReady: (() -> Unit)? = null
    // Callback when remote stream is received - ADDED
    var onRemoteStreamReceived: (() -> Unit)? = null

    companion object {
        private const val TAG = "WebRTCClient"
    }

    init {
        Log.d(TAG, "Initializing WebRTCClient")
        initializePeerConnectionFactory()
    }

    private fun initializePeerConnectionFactory() {
        Log.d(TAG, "Initializing PeerConnectionFactory")

        try {
            // Initialize WebRTC
            val options = PeerConnectionFactory.InitializationOptions.builder(context)
                .setFieldTrials("WebRTC-H264HighProfile/Enabled/")
                .setEnableInternalTracer(true)
                .createInitializationOptions()
            PeerConnectionFactory.initialize(options)
            Log.d(TAG, "PeerConnectionFactory initialized")

            // Create PeerConnectionFactory
            val encoderFactory = DefaultVideoEncoderFactory(
                eglBase.eglBaseContext,
                true, // Enable Intel VP8 encoder
                true  // Enable H264 high profile
            )
            val decoderFactory = DefaultVideoDecoderFactory(eglBase.eglBaseContext)

            peerConnectionFactory = PeerConnectionFactory.builder()
                .setOptions(PeerConnectionFactory.Options().apply {
                    disableEncryption = false
                    disableNetworkMonitor = false
                })
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .createPeerConnectionFactory()

            Log.d(TAG, "PeerConnectionFactory created successfully")

            // Create local media stream
            createLocalMediaStream()

        } catch (e: Exception) {
            Log.e(TAG, "Error initializing PeerConnectionFactory: ${e.message}", e)
        }
    }

    private fun createLocalMediaStream() {
        Log.d(TAG, "Creating local media stream")

        try {
            // Create audio track first (usually more reliable)
            createAudioTrack()

            // Then create video track
            createVideoTrack()

            // Notify that local tracks are ready
            Log.d(TAG, "Local tracks created, invoking callback")
            onLocalTracksReady?.invoke()

        } catch (e: Exception) {
            Log.e(TAG, "Error creating local media stream: ${e.message}", e)
            // Still invoke callback even if video fails, audio might work
            onLocalTracksReady?.invoke()
        }
    }

    private fun createAudioTrack() {
        try {
            Log.d(TAG, "Creating audio track")

            val audioConstraints = MediaConstraints().apply {
                // Add audio constraints
                mandatory.add(MediaConstraints.KeyValuePair("googEchoCancellation", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("googAutoGainControl", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("googHighpassFilter", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("googNoiseSuppression", "true"))
            }

            audioSource = peerConnectionFactory?.createAudioSource(audioConstraints)
            localAudioTrack = peerConnectionFactory?.createAudioTrack("audioTrack", audioSource)

            if (localAudioTrack != null) {
                Log.d(TAG, "Audio track created successfully")
            } else {
                Log.e(TAG, "Failed to create audio track")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating audio track: ${e.message}", e)
        }
    }

    private fun createVideoTrack() {
        try {
            Log.d(TAG, "Creating video track")

            // Create camera capturer
            videoCapturer = createCameraCapturer()
            if (videoCapturer == null) {
                Log.e(TAG, "No camera found on device")
                return
            }

            Log.d(TAG, "Camera capturer created")

            // Create video source
            videoSource = peerConnectionFactory?.createVideoSource(videoCapturer!!.isScreencast)
            if (videoSource == null) {
                Log.e(TAG, "Failed to create video source")
                return
            }

            Log.d(TAG, "Video source created")

            // Create SurfaceTextureHelper
            surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.eglBaseContext)
            if (surfaceTextureHelper == null) {
                Log.e(TAG, "Failed to create SurfaceTextureHelper")
                return
            }

            Log.d(TAG, "SurfaceTextureHelper created")

            // Initialize video capturer
            videoCapturer?.initialize(surfaceTextureHelper, context, videoSource?.capturerObserver)
            Log.d(TAG, "Video capturer initialized")

            // Create video track
            localVideoTrack = peerConnectionFactory?.createVideoTrack("videoTrack", videoSource)
            if (localVideoTrack == null) {
                Log.e(TAG, "Failed to create video track")
                return
            }

            Log.d(TAG, "Video track created")

            // Add video track to local view
            localVideoTrack?.setEnabled(true)
            localVideoTrack?.addSink(localView)
            Log.d(TAG, "Video track added to local view")

            // Start capturing
            try {
                videoCapturer?.startCapture(1280, 720, 30)
                Log.d(TAG, "Camera capture started successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start camera capture: ${e.message}", e)
                // Try lower resolution
                try {
                    videoCapturer?.startCapture(640, 480, 30)
                    Log.d(TAG, "Camera capture started with lower resolution")
                } catch (e2: Exception) {
                    Log.e(TAG, "Failed to start camera capture even with lower resolution: ${e2.message}", e2)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error creating video track: ${e.message}", e)
        }
    }

    fun createPeerConnection(
        iceServers: List<PeerConnection.IceServer>,
        onIceCandidate: (IceCandidate) -> Unit
    ) {
        Log.d(TAG, "Creating peer connection")

        try {
            val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
                sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
                continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
                keyType = PeerConnection.KeyType.ECDSA
                bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
                rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
                tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED
                candidateNetworkPolicy = PeerConnection.CandidateNetworkPolicy.ALL
                audioJitterBufferMaxPackets = 50
                audioJitterBufferFastAccelerate = false
                iceConnectionReceivingTimeout = -1
                iceBackupCandidatePairPingInterval = -1
            }

            peerConnection = peerConnectionFactory?.createPeerConnection(rtcConfig,
                object : PeerConnection.Observer {
                    override fun onIceCandidate(candidate: IceCandidate) {
                        Log.d(TAG, "New ICE candidate: ${candidate.sdpMid} ${candidate.sdpMLineIndex}")
                        onIceCandidate(candidate)
                    }

                    override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate?>?) {
                        Log.d(TAG, "ICE candidates removed")
                    }

                    override fun onAddStream(stream: MediaStream?) {
                        Log.d(TAG, "Stream added (legacy)")
                        // Handle legacy stream addition
                        stream?.videoTracks?.firstOrNull()?.let { videoTrack ->
                            videoTrack.setEnabled(true)
                            videoTrack.addSink(remoteView)
                            onRemoteStreamReceived?.invoke()
                        }
                    }

                    override fun onDataChannel(dc: DataChannel?) {
                        Log.d(TAG, "Data channel created")
                    }

                    override fun onIceConnectionReceivingChange(receiving: Boolean) {
                        Log.d(TAG, "ICE connection receiving change: $receiving")
                    }

                    override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) {
                        Log.d(TAG, "ICE connection state changed: $state")
                        when (state) {
                            PeerConnection.IceConnectionState.CONNECTED -> {
                                Log.d(TAG, "ICE CONNECTED - Call should be working now!")
                            }
                            PeerConnection.IceConnectionState.FAILED -> {
                                Log.e(TAG, "ICE CONNECTION FAILED")
                            }
                            PeerConnection.IceConnectionState.DISCONNECTED -> {
                                Log.w(TAG, "ICE DISCONNECTED")
                            }
                            else -> {}
                        }
                    }

                    override fun onIceGatheringChange(state: PeerConnection.IceGatheringState) {
                        Log.d(TAG, "ICE gathering state changed: $state")
                    }

                    override fun onSignalingChange(state: PeerConnection.SignalingState) {
                        Log.d(TAG, "Signaling state changed: $state")
                    }

                    override fun onRemoveStream(stream: MediaStream?) {
                        Log.d(TAG, "Stream removed")
                    }

                    override fun onRenegotiationNeeded() {
                        Log.d(TAG, "Renegotiation needed")
                    }

                    override fun onAddTrack(receiver: RtpReceiver?, streams: Array<out MediaStream>?) {
                        val track = receiver?.track()
                        when (track) {
                            is VideoTrack -> {
                                Log.d(TAG, "Remote video track received")
                                track.setEnabled(true)
                                track.addSink(remoteView)
                                Log.d(TAG, "Remote video track added to remote view")
                                onRemoteStreamReceived?.invoke()
                            }
                            is AudioTrack -> {
                                Log.d(TAG, "Remote audio track received")
                                track.setEnabled(true)
                            }
                        }
                    }
                })

            if (peerConnection == null) {
                Log.e(TAG, "Failed to create peer connection")
                return
            }

            Log.d(TAG, "Peer connection created successfully")

            // Add local tracks with proper stream IDs
            val streamIds = listOf("stream")

            localAudioTrack?.let {
                val sender = peerConnection?.addTrack(it, streamIds)
                if (sender != null) {
                    Log.d(TAG, "Local audio track added to peer connection")
                } else {
                    Log.e(TAG, "Failed to add local audio track")
                }
            }

            localVideoTrack?.let {
                val sender = peerConnection?.addTrack(it, streamIds)
                if (sender != null) {
                    Log.d(TAG, "Local video track added to peer connection")
                } else {
                    Log.e(TAG, "Failed to add local video track")
                }
            }

            Log.d(TAG, "Peer connection setup complete")

        } catch (e: Exception) {
            Log.e(TAG, "Error creating peer connection: ${e.message}", e)
        }
    }

    fun createOffer(onOfferCreated: (SessionDescription) -> Unit) {
        Log.d(TAG, "Creating offer")

        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        }

        peerConnection?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription) {
                Log.d(TAG, "Offer created successfully")
                peerConnection?.setLocalDescription(object : SdpObserver {
                    override fun onSetSuccess() {
                        Log.d(TAG, "Local SDP set successfully for offer")
                        onOfferCreated(sdp)
                    }
                    override fun onSetFailure(error: String?) {
                        Log.e(TAG, "Failed to set local SDP: $error")
                    }
                    override fun onCreateSuccess(p0: SessionDescription?) {}
                    override fun onCreateFailure(p0: String?) {}
                }, sdp)
            }
            override fun onCreateFailure(error: String?) {
                Log.e(TAG, "Failed to create offer: $error")
            }
            override fun onSetSuccess() {}
            override fun onSetFailure(error: String?) {}
        }, constraints)
    }

    fun createAnswer(onAnswerCreated: (SessionDescription) -> Unit) {
        Log.d(TAG, "Creating answer")

        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        }

        peerConnection?.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription) {
                Log.d(TAG, "Answer created successfully")
                peerConnection?.setLocalDescription(object : SdpObserver {
                    override fun onSetSuccess() {
                        Log.d(TAG, "Local SDP set successfully for answer")
                        onAnswerCreated(sdp)
                    }
                    override fun onSetFailure(error: String?) {
                        Log.e(TAG, "Failed to set local SDP: $error")
                    }
                    override fun onCreateSuccess(p0: SessionDescription?) {}
                    override fun onCreateFailure(p0: String?) {}
                }, sdp)
            }
            override fun onCreateFailure(error: String?) {
                Log.e(TAG, "Failed to create answer: $error")
            }
            override fun onSetSuccess() {}
            override fun onSetFailure(error: String?) {}
        }, constraints)
    }

    fun setRemoteDescription(sdp: SessionDescription, onSet: (() -> Unit)? = null) {
        Log.d(TAG, "Setting remote description: ${sdp.type}")
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onSetSuccess() {
                Log.d(TAG, "Remote SDP set successfully")
                onSet?.invoke()
            }
            override fun onSetFailure(error: String?) {
                Log.e(TAG, "Failed to set remote SDP: $error")
            }
            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onCreateFailure(p0: String?) {}
        }, sdp)
    }

    fun addIceCandidate(candidate: IceCandidate) {
        peerConnection?.addIceCandidate(candidate)
        Log.d(TAG, "Added remote ICE candidate")
    }

    // ADDED: Toggle audio method
    fun toggleAudio(enabled: Boolean) {
        localAudioTrack?.setEnabled(enabled)
        Log.d(TAG, "Audio ${if (enabled) "enabled" else "disabled"}")
    }

    // ADDED: Toggle video method
    fun toggleVideo(enabled: Boolean) {
        localVideoTrack?.setEnabled(enabled)
        Log.d(TAG, "Video ${if (enabled) "enabled" else "disabled"}")
    }

    // ADDED: Switch camera method
    fun switchCamera() {
        try {
            (videoCapturer as? CameraVideoCapturer)?.switchCamera(object : CameraVideoCapturer.CameraSwitchHandler {
                override fun onCameraSwitchDone(isFrontCamera: Boolean) {
                    Log.d(TAG, "Camera switched successfully. Front camera: $isFrontCamera")
                }

                override fun onCameraSwitchError(errorDescription: String?) {
                    Log.e(TAG, "Camera switch failed: $errorDescription")
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error switching camera: ${e.message}", e)
        }
    }

    private fun createCameraCapturer(): VideoCapturer? {
        Log.d(TAG, "Creating camera capturer")

        return try {
            // Try Camera2 first (for newer devices)
            val camera2Enumerator = Camera2Enumerator(context)
            val deviceNames = camera2Enumerator.deviceNames

            Log.d(TAG, "Found ${deviceNames.size} cameras")

            // Try front camera first
            for (deviceName in deviceNames) {
                if (camera2Enumerator.isFrontFacing(deviceName)) {
                    Log.d(TAG, "Creating front camera capturer: $deviceName")
                    val capturer = camera2Enumerator.createCapturer(deviceName, null)
                    if (capturer != null) {
                        return capturer
                    }
                }
            }

            // Try back camera if front not available
            for (deviceName in deviceNames) {
                if (camera2Enumerator.isBackFacing(deviceName)) {
                    Log.d(TAG, "Creating back camera capturer: $deviceName")
                    val capturer = camera2Enumerator.createCapturer(deviceName, null)
                    if (capturer != null) {
                        return capturer
                    }
                }
            }

            // If Camera2 fails, try Camera1
            Log.w(TAG, "Camera2 failed, trying Camera1")
            val camera1Enumerator = Camera1Enumerator(true)
            val camera1DeviceNames = camera1Enumerator.deviceNames

            for (deviceName in camera1DeviceNames) {
                if (camera1Enumerator.isFrontFacing(deviceName)) {
                    Log.d(TAG, "Creating Camera1 front capturer: $deviceName")
                    val capturer = camera1Enumerator.createCapturer(deviceName, null)
                    if (capturer != null) {
                        return capturer
                    }
                }
            }

            for (deviceName in camera1DeviceNames) {
                if (camera1Enumerator.isBackFacing(deviceName)) {
                    Log.d(TAG, "Creating Camera1 back capturer: $deviceName")
                    val capturer = camera1Enumerator.createCapturer(deviceName, null)
                    if (capturer != null) {
                        return capturer
                    }
                }
            }

            Log.e(TAG, "No camera capturer could be created")
            null

        } catch (e: Exception) {
            Log.e(TAG, "Error creating camera capturer: ${e.message}", e)
            null
        }
    }

    fun close() {
        Log.d(TAG, "Closing WebRTCClient...")

        try {
            // Stop video capture
            videoCapturer?.stopCapture()
            videoCapturer?.dispose()
            videoCapturer = null

            // Remove video sink and dispose
            localVideoTrack?.removeSink(localView)
            localVideoTrack?.dispose()
            localVideoTrack = null

            // Dispose audio track
            localAudioTrack?.dispose()
            localAudioTrack = null

            // Dispose sources
            videoSource?.dispose()
            videoSource = null
            audioSource?.dispose()
            audioSource = null

            // Dispose surface texture helper
            surfaceTextureHelper?.dispose()
            surfaceTextureHelper = null

            // Close peer connection
            peerConnection?.close()
            peerConnection?.dispose()
            peerConnection = null

            // Dispose factory
            peerConnectionFactory?.dispose()
            peerConnectionFactory = null

            Log.d(TAG, "WebRTCClient closed successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error closing WebRTCClient: ${e.message}", e)
        }
    }
}