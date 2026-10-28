package com.example.data

import com.example.crypto.CryptoEngine
import com.example.crypto.EncryptedPayload
import com.example.crypto.PreKeyBundle
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.crypto.SecretKey

class ChatRepository {

    // Current User Profile
    val myUserId = "user_self"
    val myUsername = "@alice_privacy"
    val myDisplayName = "Alice Sharma"
    val myIdentityKeyPair = CryptoEngine.generateIdentityKeyPair()
    val myIdentityFingerprint = CryptoEngine.sha256Hex(myIdentityKeyPair.public.encoded)
        .chunked(2).take(16).joinToString(":")

    val myPreKeyBundle = PreKeyBundle(
        identityKeyHex = myIdentityFingerprint,
        signedPreKeyId = 1,
        signedPreKeyHex = "04A1B2C3D4E5F60718293A4B5C6D7E8F90",
        signedPreKeySignature = "3045022100A7F9B2C4E6D8F1...",
        oneTimePreKeyHex = "04B2C3D4E5F6A1020304050607080910"
    )

    private val sharedSecretKey: SecretKey = CryptoEngine.generateAesKey()
    val sharedAesHex = CryptoEngine.secretKeyToHex(sharedSecretKey)

    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts.asStateFlow()

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _messages = MutableStateFlow<Map<String, List<Message>>>(emptyMap())
    val messages: StateFlow<Map<String, List<Message>>> = _messages.asStateFlow()

    private val _serverSettings = MutableStateFlow(ServerSettings())
    val serverSettings: StateFlow<ServerSettings> = _serverSettings.asStateFlow()

    private val _securityPreferences = MutableStateFlow(SecurityPreferences())
    val securityPreferences: StateFlow<SecurityPreferences> = _securityPreferences.asStateFlow()

    private val _activeCall = MutableStateFlow<CallSession?>(null)
    val activeCall: StateFlow<CallSession?> = _activeCall.asStateFlow()

    private val _typingStatus = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val typingStatus: StateFlow<Map<String, Boolean>> = _typingStatus.asStateFlow()

    init {
        seedInitialData()
    }

    private fun seedInitialData() {
        val c1 = Contact(
            id = "c_aarav",
            username = "@aarav_sec",
            displayName = "Aarav Mehta",
            avatarColorHex = "#00B4D8",
            identityKeyFingerprint = "7F:3A:99:B2:1C:E4:66:88:9A:F0:34:B8:21:77:AC:E1",
            isVerified = true,
            isOnline = true,
            lastSeenText = "Online",
            aboutText = "Cryptographic Security • Signal Protocol"
        )

        val c2 = Contact(
            id = "c_priya",
            username = "@priya_infra",
            displayName = "Priya Patel",
            avatarColorHex = "#7209B7",
            identityKeyFingerprint = "C2:88:41:A5:10:9B:3E:F7:62:AA:7C:19:D3:55:01:E8",
            isVerified = true,
            isOnline = false,
            lastSeenText = "Last seen 12m ago",
            aboutText = "Self-hosted Coturn & MinIO Maintainer"
        )

        val c3 = Contact(
            id = "c_vikram",
            username = "@vikram_net",
            displayName = "Vikram Malhotra",
            avatarColorHex = "#F72585",
            identityKeyFingerprint = "44:91:0A:7E:88:2F:11:3D:B9:00:C8:54:E6:3A:91:20",
            isVerified = false,
            isOnline = true,
            lastSeenText = "Online",
            aboutText = "Privacy advocate • Zero cloud dependencies"
        )

        val c4 = Contact(
            id = "c_zoya",
            username = "@zoya_sys",
            displayName = "Zoya Khan",
            avatarColorHex = "#4361EE",
            identityKeyFingerprint = "9B:14:52:8E:31:07:FF:4C:6A:B8:29:10:73:C6:E4:55",
            isVerified = false,
            isOnline = false,
            lastSeenText = "Last seen 2h ago",
            aboutText = "Postgres + Redis real-time sync"
        )

        _contacts.value = listOf(c1, c2, c3, c4)

        // Seed Messages for Aarav
        val m1 = createPreSeededMessage(
            id = "m_1",
            convId = "conv_aarav",
            senderId = c1.id,
            senderName = c1.displayName,
            timeOffsetMs = -1000 * 60 * 45,
            text = "Namaste Alice! Maine Coturn TURN server deploy kar diya hai public IP pe. WebRTC calls P2P direct test karte hain?",
            type = MessageType.TEXT
        )

        val m2 = createPreSeededMessage(
            id = "m_2",
            convId = "conv_aarav",
            senderId = myUserId,
            senderName = myDisplayName,
            timeOffsetMs = -1000 * 60 * 30,
            text = "Haan Aarav, bilkul! DTLS-SRTP 256-bit media encryption enable hai. MinIO pe file sharing ka AES-256 test bhi run kiya.",
            type = MessageType.TEXT
        )

        val m3 = createPreSeededMessage(
            id = "m_3",
            convId = "conv_aarav",
            senderId = c1.id,
            senderName = c1.displayName,
            timeOffsetMs = -1000 * 60 * 15,
            text = "Architecture diagram and server verification logs attached. Encrypted with one-time AES-256 key:",
            type = MessageType.IMAGE,
            fileSize = "1.4 MB",
            checksum = "E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855",
            mediaThumbnailColor = "#0D324D"
        )

        val m4 = createPreSeededMessage(
            id = "m_4",
            convId = "conv_aarav",
            senderId = c1.id,
            senderName = c1.displayName,
            timeOffsetMs = -1000 * 60 * 5,
            text = "Voice note: Architecture walkthrough & Double Ratchet sync status (0:24)",
            type = MessageType.VOICE_NOTE,
            mediaDurationSec = 24,
            fileSize = "384 KB",
            checksum = "8A4C2E9F610B7D149B0F33A25781E9C304859AA71C2E0B55421D8A9F438B2110"
        )

        // Seed Messages for Priya
        val mp1 = createPreSeededMessage(
            id = "mp_1",
            convId = "conv_priya",
            senderId = c2.id,
            senderName = c2.displayName,
            timeOffsetMs = -1000 * 60 * 120,
            text = "MinIO bucket policy set to private. Zero-trust token auth active. Docker compose file ready hai.",
            type = MessageType.TEXT
        )

        val mp2 = createPreSeededMessage(
            id = "mp_2",
            convId = "conv_priya",
            senderId = c2.id,
            senderName = c2.displayName,
            timeOffsetMs = -1000 * 60 * 80,
            text = "docker-compose-production.yml (Encrypted config)",
            type = MessageType.DOCUMENT,
            fileSize = "14.8 KB",
            checksum = "94A1C8E56B0F2A3841C57D289B10E41A28469C315E0F42A17B8C90E2145A39B0"
        )

        // Seed Group: Core Dev Security Group
        val mg1 = createPreSeededMessage(
            id = "mg_1",
            convId = "conv_group_core",
            senderId = c3.id,
            senderName = c3.displayName,
            timeOffsetMs = -1000 * 60 * 180,
            text = "Welcome to ChatMe Core Security Council. All group messages use Signal Sender Keys protocol.",
            type = MessageType.TEXT
        )
        val mg2 = createPreSeededMessage(
            id = "mg_2",
            convId = "conv_group_core",
            senderId = c4.id,
            senderName = c4.displayName,
            timeOffsetMs = -1000 * 60 * 50,
            text = "PostgreSQL replicas and Redis pub/sub latency is under 12ms. WebSocket gateway handles 10k concurrent connections.",
            type = MessageType.TEXT
        )

        _messages.value = mapOf(
            "conv_aarav" to listOf(m1, m2, m3, m4),
            "conv_priya" to listOf(mp1, mp2),
            "conv_vikram" to emptyList(),
            "conv_zoya" to emptyList(),
            "conv_group_core" to listOf(mg1, mg2)
        )

        val conv1 = Conversation(
            id = "conv_aarav",
            isGroup = false,
            name = c1.displayName,
            avatarColorHex = c1.avatarColorHex,
            participantIds = listOf(myUserId, c1.id),
            lastMessage = m4,
            unreadCount = 1,
            isSafetyVerified = true,
            isPinned = true,
            disappearingSeconds = 0
        )

        val conv2 = Conversation(
            id = "conv_priya",
            isGroup = false,
            name = c2.displayName,
            avatarColorHex = c2.avatarColorHex,
            participantIds = listOf(myUserId, c2.id),
            lastMessage = mp2,
            unreadCount = 0,
            isSafetyVerified = true,
            isPinned = false,
            disappearingSeconds = 3600
        )

        val conv3 = Conversation(
            id = "conv_group_core",
            isGroup = true,
            name = "ChatMe Core Architecture",
            avatarColorHex = "#00E5A3",
            participantIds = listOf(myUserId, c1.id, c2.id, c3.id, c4.id),
            lastMessage = mg2,
            unreadCount = 2,
            isSafetyVerified = true,
            isPinned = true,
            disappearingSeconds = 0
        )

        val conv4 = Conversation(
            id = "conv_vikram",
            isGroup = false,
            name = c3.displayName,
            avatarColorHex = c3.avatarColorHex,
            participantIds = listOf(myUserId, c3.id),
            lastMessage = null,
            unreadCount = 0,
            isSafetyVerified = false,
            isPinned = false,
            disappearingSeconds = 0
        )

        _conversations.value = listOf(conv1, conv3, conv2, conv4)
    }

    private fun createPreSeededMessage(
        id: String,
        convId: String,
        senderId: String,
        senderName: String,
        timeOffsetMs: Long,
        text: String,
        type: MessageType,
        fileSize: String = "",
        checksum: String = "",
        mediaDurationSec: Int = 0,
        mediaThumbnailColor: String = "#0A2540"
    ): Message {
        val payload = CryptoEngine.encryptText(text, sharedSecretKey)
        return Message(
            id = id,
            conversationId = convId,
            senderId = senderId,
            senderName = senderName,
            timestamp = System.currentTimeMillis() + timeOffsetMs,
            type = type,
            textContent = text,
            encryptedPayload = payload,
            sharedAesKeyHex = sharedAesHex,
            fileSizeStr = fileSize,
            fileChecksumSha256 = checksum,
            mediaDurationSec = mediaDurationSec,
            mediaThumbnailColorHex = mediaThumbnailColor,
            status = MessageStatus.READ
        )
    }

    fun sendMessage(
        conversationId: String,
        text: String,
        type: MessageType = MessageType.TEXT,
        fileSizeStr: String = "",
        mediaDurationSec: Int = 0,
        mediaThumbnailColor: String = "#082E47",
        replyTo: Message? = null
    ): Message {
        val key = CryptoEngine.generateAesKey()
        val keyHex = CryptoEngine.secretKeyToHex(key)
        val payload = CryptoEngine.encryptText(text, key)
        val checksum = CryptoEngine.sha256Hex(text.toByteArray(Charsets.UTF_8))

        val currentConv = _conversations.value.find { it.id == conversationId }
        val disappearingSeconds = currentConv?.disappearingSeconds ?: 0

        val msg = Message(
            id = "msg_${System.currentTimeMillis()}_${(100..999).random()}",
            conversationId = conversationId,
            senderId = myUserId,
            senderName = myDisplayName,
            timestamp = System.currentTimeMillis(),
            type = type,
            textContent = text,
            encryptedPayload = payload,
            sharedAesKeyHex = keyHex,
            fileSizeStr = fileSizeStr,
            fileChecksumSha256 = checksum,
            mediaDurationSec = mediaDurationSec,
            mediaThumbnailColorHex = mediaThumbnailColor,
            status = MessageStatus.SENT,
            replyToId = replyTo?.id,
            replyToSender = replyTo?.senderName,
            replyToSnippet = replyTo?.textContent?.take(40),
            isDisappearing = disappearingSeconds > 0,
            expiresAt = if (disappearingSeconds > 0) System.currentTimeMillis() + disappearingSeconds * 1000L else null
        )

        // Add to messages
        val currentList = _messages.value[conversationId] ?: emptyList()
        _messages.update { it + (conversationId to (currentList + msg)) }

        // Update conversation last message
        _conversations.update { list ->
            list.map { conv ->
                if (conv.id == conversationId) {
                    conv.copy(lastMessage = msg)
                } else conv
            }
        }

        return msg
    }

    fun addReaction(conversationId: String, messageId: String, emoji: String) {
        val convMessages = _messages.value[conversationId] ?: return
        val updated = convMessages.map { m ->
            if (m.id == messageId) {
                val currentCount = m.reactions[emoji] ?: 0
                val newReactions = m.reactions.toMutableMap()
                newReactions[emoji] = currentCount + 1
                m.copy(reactions = newReactions)
            } else m
        }
        _messages.update { it + (conversationId to updated) }
    }

    fun deleteMessage(conversationId: String, messageId: String) {
        val convMessages = _messages.value[conversationId] ?: return
        val updated = convMessages.filterNot { it.id == messageId }
        _messages.update { it + (conversationId to updated) }
        _conversations.update { list ->
            list.map { conv ->
                if (conv.id == conversationId && conv.lastMessage?.id == messageId) {
                    conv.copy(lastMessage = updated.lastOrNull())
                } else conv
            }
        }
    }

    fun toggleContactSafetyVerification(contactId: String) {
        _contacts.update { list ->
            list.map { if (it.id == contactId) it.copy(isVerified = !it.isVerified) else it }
        }
        _conversations.update { list ->
            list.map { conv ->
                if (conv.participantIds.contains(contactId)) {
                    val contact = _contacts.value.find { it.id == contactId }
                    conv.copy(isSafetyVerified = contact?.isVerified ?: false)
                } else conv
            }
        }
    }

    fun setDisappearingTimer(conversationId: String, seconds: Int) {
        _conversations.update { list ->
            list.map { if (it.id == conversationId) it.copy(disappearingSeconds = seconds) else it }
        }
    }

    fun startCall(contact: Contact, isVideo: Boolean) {
        _activeCall.value = CallSession(
            callId = "call_${System.currentTimeMillis()}",
            contact = contact,
            isVideo = isVideo,
            status = CallStatus.CONNECTED,
            durationSeconds = 0,
            sasVerificationCode = "Falcon • Diamond • Sapphire"
        )
    }

    fun endCall() {
        _activeCall.value = null
    }

    fun toggleMuteCall() {
        _activeCall.update { it?.copy(isMuted = !it.isMuted) }
    }

    fun toggleCameraCall() {
        _activeCall.update { it?.copy(isCameraOff = !it.isCameraOff) }
    }

    fun toggleSpeakerCall() {
        _activeCall.update { it?.copy(isSpeakerOn = !it.isSpeakerOn) }
    }

    fun updateServerSettings(newSettings: ServerSettings) {
        _serverSettings.value = newSettings
    }

    fun updateSecurityPreferences(newPrefs: SecurityPreferences) {
        _securityPreferences.value = newPrefs
    }

    fun createNewChat(contact: Contact): String {
        val existing = _conversations.value.find { !it.isGroup && it.participantIds.contains(contact.id) }
        if (existing != null) return existing.id

        val newConvId = "conv_${contact.id}"
        val newConv = Conversation(
            id = newConvId,
            isGroup = false,
            name = contact.displayName,
            avatarColorHex = contact.avatarColorHex,
            participantIds = listOf(myUserId, contact.id),
            lastMessage = null,
            unreadCount = 0,
            isSafetyVerified = contact.isVerified
        )
        _conversations.update { listOf(newConv) + it }
        return newConvId
    }

    fun createGroupChat(name: String, selectedContactIds: List<String>): String {
        val newConvId = "group_${System.currentTimeMillis()}"
        val newConv = Conversation(
            id = newConvId,
            isGroup = true,
            name = name,
            avatarColorHex = "#00E5A3",
            participantIds = listOf(myUserId) + selectedContactIds,
            lastMessage = null,
            unreadCount = 0,
            isSafetyVerified = true
        )
        _conversations.update { listOf(newConv) + it }
        return newConvId
    }
}
