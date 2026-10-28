package com.example.model

import com.example.crypto.EncryptedPayload

enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO,
    VOICE_NOTE,
    DOCUMENT,
    SYSTEM
}

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ
}

enum class CallStatus {
    DIALING,
    RINGING,
    CONNECTED,
    ENDED
}

data class Contact(
    val id: String,
    val username: String,
    val displayName: String,
    val avatarColorHex: String,
    val identityKeyFingerprint: String,
    val isVerified: Boolean = false,
    val isOnline: Boolean = true,
    val lastSeenText: String = "Online",
    val aboutText: String = "E2EE Secured • Self-hosted"
)

data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val senderName: String,
    val timestamp: Long,
    val type: MessageType,
    val textContent: String,
    val encryptedPayload: EncryptedPayload,
    val sharedAesKeyHex: String,
    val mediaUri: String? = null,
    val mediaThumbnailColorHex: String = "#0A2540",
    val mediaDurationSec: Int = 0,
    val fileSizeStr: String = "",
    val fileChecksumSha256: String = "",
    val status: MessageStatus = MessageStatus.READ,
    val replyToId: String? = null,
    val replyToSender: String? = null,
    val replyToSnippet: String? = null,
    val reactions: Map<String, Int> = emptyMap(),
    val isDisappearing: Boolean = false,
    val expiresAt: Long? = null
)

data class Conversation(
    val id: String,
    val isGroup: Boolean,
    val name: String,
    val avatarColorHex: String,
    val participantIds: List<String>,
    val lastMessage: Message?,
    val unreadCount: Int = 0,
    val isSafetyVerified: Boolean = false,
    val isPinned: Boolean = false,
    val disappearingSeconds: Int = 0 // 0 = off, 30, 300, 3600, 86400
)

data class CallSession(
    val callId: String,
    val contact: Contact,
    val isVideo: Boolean,
    val status: CallStatus,
    val durationSeconds: Int = 0,
    val isMuted: Boolean = false,
    val isCameraOff: Boolean = false,
    val isSpeakerOn: Boolean = true,
    val sasVerificationCode: String = "Falcon • Diamond • Sapphire",
    val dtlsSrtpCipher: String = "SRTP_AEAD_AES_256_GCM (DTLS 1.3)",
    val iceConnectionState: String = "Direct P2P (Low Latency)"
)

data class ServerSettings(
    val wsUrl: String = "wss://chat.myserver.org/ws",
    val minioUrl: String = "https://s3.myserver.org:9000/chatme-vault",
    val turnUrl: String = "turn:turn.myserver.org:3478",
    val pushUrl: String = "https://ntfy.sh/chatme_e2ee_alerts",
    val isConnected: Boolean = true,
    val lastPingMs: Long = 18,
    val tlsVersion: String = "TLS 1.3 (RFC 8446)"
)

data class SecurityPreferences(
    val appLockEnabled: Boolean = false,
    val pinCode: String = "1234",
    val biometricEnabled: Boolean = true,
    val blockScreenshots: Boolean = false,
    val autoDeleteCacheHours: Int = 24
)
