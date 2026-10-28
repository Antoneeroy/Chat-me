package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.VoiceWaveformVisualizer
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    conversation: Conversation,
    messages: List<Message>,
    contact: Contact?,
    peerIsTyping: Boolean,
    onBackClick: () -> Unit,
    onSendMessage: (String, MessageType, String, Int, String, Message?) -> Unit,
    onStartCall: (Contact, Boolean) -> Unit,
    onInspectCrypto: (Message) -> Unit,
    onAddReaction: (String, String) -> Unit,
    onDeleteMessage: (String) -> Unit,
    onOpenSafetyNumber: (Contact) -> Unit,
    onSetDisappearingTimer: (Int) -> Unit
) {
    var textInput by remember { mutableStateOf("") }
    var replyingTo by remember { mutableStateOf<Message?>(null) }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var showDisappearingDialog by remember { mutableStateOf(false) }
    var selectedMessageForActions by remember { mutableStateOf<Message?>(null) }
    var viewingPhotoMessage by remember { mutableStateOf<Message?>(null) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Scroll to bottom on new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark,
                    titleContentColor = TextPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            if (contact != null) onOpenSafetyNumber(contact)
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(conversation.avatarColorHex))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(conversation.name.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = conversation.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                if (conversation.isSafetyVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified",
                                        tint = StatusVerifiedGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (peerIsTyping) "typing..." else if (contact?.isOnline == true) "Online" else "Signal Double Ratchet",
                                fontSize = 11.sp,
                                color = if (peerIsTyping) PrimaryEmerald else TextSecondary
                            )
                        }
                    }
                },
                actions = {
                    if (contact != null) {
                        IconButton(onClick = { onStartCall(contact, false) }) {
                            Icon(Icons.Default.Call, contentDescription = "Voice Call", tint = PrimaryEmerald)
                        }
                        IconButton(onClick = { onStartCall(contact, true) }) {
                            Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = SecondaryCyan)
                        }
                    }
                    IconButton(onClick = { showDisappearingDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Disappearing Messages",
                            tint = if (conversation.disappearingSeconds > 0) PrimaryEmerald else TextSecondary
                        )
                    }
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .imePadding()
            ) {
                // Reply Quote Preview
                replyingTo?.let { replyMsg ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceVariantDark)
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Replying to ${replyMsg.senderName}", color = PrimaryEmerald, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(replyMsg.textContent.take(50), color = TextSecondary, fontSize = 12.sp, maxLines = 1)
                        }
                        IconButton(onClick = { replyingTo = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel reply", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // Input Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showAttachmentSheet = true }) {
                        Icon(Icons.Default.AttachFile, contentDescription = "Attach File", tint = PrimaryEmerald)
                    }

                    TextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        placeholder = { Text("End-to-End Encrypted Message...", color = TextSecondary, fontSize = 14.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = SurfaceVariantDark,
                            unfocusedContainerColor = SurfaceVariantDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    val canSend = textInput.isNotBlank()
                    IconButton(
                        onClick = {
                            if (canSend) {
                                onSendMessage(textInput.trim(), MessageType.TEXT, "", 0, "", replyingTo)
                                textInput = ""
                                replyingTo = null
                            } else {
                                // Send simulated instant encrypted voice note!
                                onSendMessage("Encrypted Voice Note (0:24)", MessageType.VOICE_NOTE, "384 KB", 24, "", replyingTo)
                                replyingTo = null
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PrimaryEmerald)
                    ) {
                        Icon(
                            imageVector = if (canSend) Icons.Default.Send else Icons.Default.Mic,
                            contentDescription = if (canSend) "Send" else "Record Voice Note",
                            tint = OnPrimaryEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // E2EE Info Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryContainerEmerald.copy(alpha = 0.35f))
                    .clickable {
                        if (contact != null) onOpenSafetyNumber(contact)
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = PrimaryEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Messages and calls are E2EE with Double Ratchet. No third party or server can read them.",
                        fontSize = 11.sp,
                        color = PrimaryEmerald
                    )
                }
            }

            // Disappearing Messages Banner (if active)
            if (conversation.disappearingSeconds > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceVariantDark)
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "⏱ Disappearing messages active (${conversation.disappearingSeconds}s timer)",
                        fontSize = 11.sp,
                        color = SecondaryCyan
                    )
                }
            }

            // Messages LazyColumn
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    val isSelf = message.senderId == "user_self"

                    MessageBubbleItem(
                        message = message,
                        isSelf = isSelf,
                        onInspectCrypto = { onInspectCrypto(message) },
                        onPhotoClick = { viewingPhotoMessage = message },
                        onLongPress = { selectedMessageForActions = message }
                    )
                }

                // Peer typing indicator bubble
                if (peerIsTyping) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SurfaceVariantDark)
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("${conversation.name} is typing", fontSize = 12.sp, color = TextSecondary)
                                    TypingDotsIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Attachment Modal Bottom Sheet
    if (showAttachmentSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAttachmentSheet = false },
            containerColor = SurfaceDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Share Encrypted Media (AES-256)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = "Files are encrypted on your phone with a random AES-256 key before upload to self-hosted MinIO vault.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Encrypted Photo Option
                    MediaOptionItem(
                        icon = Icons.Default.Image,
                        label = "Encrypted Photo",
                        color = PrimaryEmerald
                    ) {
                        showAttachmentSheet = false
                        onSendMessage(
                            "High-Resolution Architecture Diagram (E2EE Encrypted)",
                            MessageType.IMAGE,
                            "1.6 MB",
                            0,
                            "#0A324C",
                            replyingTo
                        )
                    }

                    // Encrypted Voice Note Option
                    MediaOptionItem(
                        icon = Icons.Default.Mic,
                        label = "Voice Note",
                        color = SecondaryCyan
                    ) {
                        showAttachmentSheet = false
                        onSendMessage(
                            "Encrypted Voice Note (0:24)",
                            MessageType.VOICE_NOTE,
                            "384 KB",
                            24,
                            "",
                            replyingTo
                        )
                    }

                    // Encrypted Document Option
                    MediaOptionItem(
                        icon = Icons.Default.Description,
                        label = "Document",
                        color = AccentAmber
                    ) {
                        showAttachmentSheet = false
                        onSendMessage(
                            "coturn-turnserver.conf (Encrypted)",
                            MessageType.DOCUMENT,
                            "28.4 KB",
                            0,
                            "",
                            replyingTo
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Disappearing Messages Dialog
    if (showDisappearingDialog) {
        AlertDialog(
            onDismissRequest = { showDisappearingDialog = false },
            containerColor = SurfaceDark,
            title = { Text("Disappearing Messages", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select self-destruction timer for this chat:", fontSize = 13.sp, color = TextSecondary)
                    val options = listOf(
                        0 to "Off",
                        30 to "30 seconds",
                        300 to "5 minutes",
                        3600 to "1 hour",
                        86400 to "24 hours"
                    )
                    options.forEach { (seconds, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onSetDisappearingTimer(seconds)
                                    showDisappearingDialog = false
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, color = TextPrimary, fontSize = 14.sp)
                            if (conversation.disappearingSeconds == seconds) {
                                Icon(Icons.Default.Check, null, tint = PrimaryEmerald)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDisappearingDialog = false }) {
                    Text("Close", color = PrimaryEmerald)
                }
            }
        )
    }

    // Message Actions Sheet (Reply, Emoji Reactions, Inspect Crypto, Delete)
    selectedMessageForActions?.let { msg ->
        ModalBottomSheet(
            onDismissRequest = { selectedMessageForActions = null },
            containerColor = SurfaceDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Quick Emoji Reactions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    val emojis = listOf("👍", "❤️", "😂", "🔥", "🔐", "🙏")
                    emojis.forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 26.sp,
                            modifier = Modifier
                                .clickable {
                                    onAddReaction(msg.id, emoji)
                                    selectedMessageForActions = null
                                }
                                .padding(6.dp)
                        )
                    }
                }

                HorizontalDivider(color = BorderDark)

                // Action Items
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            replyingTo = msg
                            selectedMessageForActions = null
                        }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Reply, null, tint = TextPrimary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Reply with Quote", color = TextPrimary, fontSize = 15.sp)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedMessageForActions = null
                            onInspectCrypto(msg)
                        }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.VpnKey, null, tint = SecondaryCyan)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("View Encryption Details (AES-256-GCM / IV)", color = SecondaryCyan, fontSize = 15.sp)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDeleteMessage(msg.id)
                            selectedMessageForActions = null
                        }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Delete, null, tint = AccentRed)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Delete Message", color = AccentRed, fontSize = 15.sp)
                }
            }
        }
    }

    // Photo Fullscreen Viewer Dialog
    viewingPhotoMessage?.let { photoMsg ->
        AlertDialog(
            onDismissRequest = { viewingPhotoMessage = null },
            containerColor = SurfaceDark,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Decrypted Media Viewer", fontWeight = FontWeight.Bold, color = TextPrimary)
                    IconButton(onClick = { viewingPhotoMessage = null }) {
                        Icon(Icons.Default.Close, null, tint = TextSecondary)
                    }
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // High-contrast encrypted image visual canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFF0D2538),
                                        Color(0xFF143B57),
                                        Color(0xFF0A1B29)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = PrimaryEmerald,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Real-Time E2EE Photo", fontWeight = FontWeight.Bold, color = Color.White)
                            Text("AES-256 Cipher Decrypted", fontSize = 12.sp, color = PrimaryEmerald)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(photoMsg.textContent, fontSize = 13.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "SHA-256: ${photoMsg.fileChecksumSha256.take(24)}...",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onInspectCrypto(photoMsg)
                    viewingPhotoMessage = null
                }) {
                    Text("Inspect Cipher", color = SecondaryCyan)
                }
            }
        )
    }
}

@Composable
private fun MessageBubbleItem(
    message: Message,
    isSelf: Boolean,
    onInspectCrypto: () -> Unit,
    onPhotoClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onLongPress() },
                    onTap = {
                        if (message.type == MessageType.IMAGE) {
                            onPhotoClick()
                        } else {
                            onInspectCrypto()
                        }
                    }
                )
            },
        horizontalAlignment = if (isSelf) Alignment.End else Alignment.Start
    ) {
        // Message Card
        Box(
            modifier = Modifier
                .widthIn(max = 310.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isSelf) 16.dp else 2.dp,
                        bottomEnd = if (isSelf) 2.dp else 16.dp
                    )
                )
                .background(if (isSelf) SentBubbleDark else ReceivedBubbleDark)
                .padding(12.dp)
        ) {
            Column {
                // Sender name in group if not self
                if (!isSelf) {
                    Text(
                        text = message.senderName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryCyan
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                // Quoted reply snippet
                if (message.replyToSender != null && message.replyToSnippet != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.25f))
                            .padding(6.dp)
                    ) {
                        Column {
                            Text(message.replyToSender, color = PrimaryEmerald, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(message.replyToSnippet, color = TextSecondary, fontSize = 11.sp, maxLines = 1)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Message Body by Type
                when (message.type) {
                    MessageType.TEXT -> {
                        Text(
                            text = message.textContent,
                            fontSize = 14.sp,
                            color = if (isSelf) SentBubbleText else ReceivedBubbleText,
                            lineHeight = 20.sp
                        )
                    }

                    MessageType.IMAGE -> {
                        Column {
                            // Encrypted photo preview card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color(0xFF0F2B3E), Color(0xFF143B57), Color(0xFF091924))
                                        )
                                    )
                                    .clickable(onClick = onPhotoClick),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Encrypted Image",
                                        tint = PrimaryEmerald,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "AES-256 Encrypted Photo",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Tap to decrypt & view",
                                        fontSize = 11.sp,
                                        color = PrimaryEmerald
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = message.textContent,
                                fontSize = 13.sp,
                                color = if (isSelf) SentBubbleText else ReceivedBubbleText
                            )
                            if (message.fileSizeStr.isNotEmpty()) {
                                Text(
                                    text = "Size: ${message.fileSizeStr} • MinIO Encrypted Vault",
                                    fontSize = 10.sp,
                                    color = TextTertiary
                                )
                            }
                        }
                    }

                    MessageType.VOICE_NOTE -> {
                        Column {
                            Text(
                                text = "🔒 Encrypted Voice Note",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryEmerald
                            )
                            VoiceWaveformVisualizer(
                                durationSec = if (message.mediaDurationSec > 0) message.mediaDurationSec else 24,
                                barColor = if (isSelf) PrimaryEmerald else SecondaryCyan
                            )
                        }
                    }

                    MessageType.DOCUMENT -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.2f))
                                .padding(8.dp)
                        ) {
                            Icon(Icons.Default.InsertDriveFile, null, tint = AccentAmber, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(message.textContent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Text("${message.fileSizeStr} • SHA-256 Verified", fontSize = 10.sp, color = TextSecondary)
                            }
                            Icon(Icons.Default.Download, null, tint = PrimaryEmerald, modifier = Modifier.size(20.dp))
                        }
                    }

                    MessageType.VIDEO -> {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF0C2436))
                                    .clickable(onClick = onPhotoClick),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Videocam, "Encrypted Video", tint = SecondaryCyan, modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Encrypted Video (DTLS-SRTP)", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(message.textContent, fontSize = 13.sp, color = if (isSelf) SentBubbleText else ReceivedBubbleText)
                        }
                    }

                    MessageType.SYSTEM -> {
                        Text(
                            text = message.textContent,
                            fontSize = 12.sp,
                            color = PrimaryEmerald,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Footer: Timestamp + Encryption lock + delivery ticks
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Encrypted",
                        tint = TextTertiary,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = timeStr,
                        fontSize = 10.sp,
                        color = TextTertiary
                    )
                    if (isSelf) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Status",
                            tint = if (message.status == MessageStatus.READ) PrimaryEmerald else TextTertiary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }

        // Reactions Pills below bubble
        if (message.reactions.isNotEmpty()) {
            Row(
                modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                message.reactions.forEach { (emoji, count) ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceVariantDark)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("$emoji $count", fontSize = 11.sp, color = TextPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, fontSize = 12.sp, color = TextPrimary)
    }
}

@Composable
private fun TypingDotsIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(PrimaryEmerald.copy(alpha = alpha))
            )
        }
    }
}
