package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import com.example.viewmodel.MainTab
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    conversations: List<Conversation>,
    contacts: List<Contact>,
    selectedTab: MainTab,
    filterType: String,
    searchQuery: String,
    onTabSelected: (MainTab) -> Unit,
    onFilterSelected: (String) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onConversationClick: (String) -> Unit,
    onStartCallClick: (Contact, Boolean) -> Unit,
    onContactSafetyClick: (Contact) -> Unit,
    onOpenCryptoInspector: () -> Unit,
    onOpenServerConfig: () -> Unit,
    onNewChatClick: () -> Unit
) {
    var isSearchActive by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = BackgroundDark,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .statusBarsPadding()
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Logo Shield
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(PrimaryContainerEmerald),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "E2EE",
                                tint = PrimaryEmerald,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ChatMe",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryEmerald)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "E2EE • Self-Hosted Node",
                                    fontSize = 11.sp,
                                    color = PrimaryEmerald
                                )
                            }
                        }
                    }

                    // Action Icons
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { isSearchActive = !isSearchActive }) {
                            Icon(
                                imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = "Search",
                                tint = TextPrimary
                            )
                        }
                        IconButton(onClick = onOpenCryptoInspector) {
                            Icon(
                                imageVector = Icons.Default.VpnKey,
                                contentDescription = "Cryptographic Keys",
                                tint = SecondaryCyan
                            )
                        }
                        IconButton(onClick = onOpenServerConfig) {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = "Server Config",
                                tint = PrimaryEmerald
                            )
                        }
                    }
                }

                // Search Bar Input (if active)
                if (isSearchActive) {
                    TextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        placeholder = { Text("Search encrypted chats & contacts...", color = TextSecondary, fontSize = 14.sp) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = SurfaceVariantDark,
                            unfocusedContainerColor = SurfaceVariantDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Filter Chips (Only for Chats tab)
                if (selectedTab == MainTab.CHATS) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterPill(title = "All", isSelected = filterType == "ALL") { onFilterSelected("ALL") }
                        FilterPill(title = "Direct", isSelected = filterType == "DIRECT") { onFilterSelected("DIRECT") }
                        FilterPill(title = "Groups", isSelected = filterType == "GROUPS") { onFilterSelected("GROUPS") }
                        FilterPill(title = "Unread", isSelected = filterType == "UNREAD") { onFilterSelected("UNREAD") }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark,
                contentColor = TextPrimary,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = selectedTab == MainTab.CHATS,
                    onClick = { onTabSelected(MainTab.CHATS) },
                    icon = { Icon(imageVector = Icons.Default.Chat, contentDescription = "Chats") },
                    label = { Text("Chats", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = PrimaryContainerEmerald,
                        selectedIconColor = PrimaryEmerald,
                        selectedTextColor = PrimaryEmerald,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.CALLS,
                    onClick = { onTabSelected(MainTab.CALLS) },
                    icon = { Icon(imageVector = Icons.Default.Call, contentDescription = "Calls") },
                    label = { Text("Calls", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = PrimaryContainerEmerald,
                        selectedIconColor = PrimaryEmerald,
                        selectedTextColor = PrimaryEmerald,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.CONTACTS,
                    onClick = { onTabSelected(MainTab.CONTACTS) },
                    icon = { Icon(imageVector = Icons.Default.People, contentDescription = "Contacts") },
                    label = { Text("Contacts", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = PrimaryContainerEmerald,
                        selectedIconColor = PrimaryEmerald,
                        selectedTextColor = PrimaryEmerald,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.VAULT,
                    onClick = { onTabSelected(MainTab.VAULT) },
                    icon = { Icon(imageVector = Icons.Default.Security, contentDescription = "Security") },
                    label = { Text("Vault", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = PrimaryContainerEmerald,
                        selectedIconColor = PrimaryEmerald,
                        selectedTextColor = PrimaryEmerald,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    )
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == MainTab.CHATS || selectedTab == MainTab.CONTACTS) {
                FloatingActionButton(
                    onClick = onNewChatClick,
                    containerColor = PrimaryEmerald,
                    contentColor = OnPrimaryEmerald,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "New Encrypted Chat")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                MainTab.CHATS -> {
                    ChatsTabContent(
                        conversations = conversations,
                        filterType = filterType,
                        searchQuery = searchQuery,
                        onConversationClick = onConversationClick
                    )
                }
                MainTab.CALLS -> {
                    CallsTabContent(
                        contacts = contacts,
                        onStartCall = onStartCallClick
                    )
                }
                MainTab.CONTACTS -> {
                    ContactsTabContent(
                        contacts = contacts,
                        onContactClick = { contact ->
                            // Open direct conversation
                            onContactSafetyClick(contact)
                        },
                        onVerifyClick = onContactSafetyClick
                    )
                }
                MainTab.VAULT -> {
                    VaultTabContent(
                        onOpenInspector = onOpenCryptoInspector,
                        onOpenServerConfig = onOpenServerConfig
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterPill(title: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) PrimaryEmerald else SurfaceVariantDark)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) OnPrimaryEmerald else TextSecondary
        )
    }
}

@Composable
private fun ChatsTabContent(
    conversations: List<Conversation>,
    filterType: String,
    searchQuery: String,
    onConversationClick: (String) -> Unit
) {
    val filtered = remember(conversations, filterType, searchQuery) {
        conversations.filter { conv ->
            val matchesFilter = when (filterType) {
                "DIRECT" -> !conv.isGroup
                "GROUPS" -> conv.isGroup
                "UNREAD" -> conv.unreadCount > 0
                else -> true
            }
            val matchesSearch = conv.name.contains(searchQuery, ignoreCase = true) ||
                    (conv.lastMessage?.textContent?.contains(searchQuery, ignoreCase = true) == true)
            matchesFilter && matchesSearch
        }
    }

    if (filtered.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = null,
                    tint = TextTertiary,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("No encrypted chats found", color = TextSecondary, fontSize = 15.sp)
                Text("Start an E2EE conversation using the pen button below", color = TextTertiary, fontSize = 12.sp)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(filtered, key = { it.id }) { conv ->
                ConversationItemRow(conv = conv, onClick = { onConversationClick(conv.id) })
                HorizontalDivider(
                    modifier = Modifier.padding(start = 76.dp, end = 16.dp),
                    color = BorderDark.copy(alpha = 0.5f),
                    thickness = 0.5.dp
                )
            }
        }
    }
}

@Composable
private fun ConversationItemRow(
    conv: Conversation,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar with safety ring
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(Color(android.graphics.Color.parseColor(conv.avatarColorHex))),
            contentAlignment = Alignment.Center
        ) {
            if (conv.isGroup) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = "Group",
                    tint = Color.Black,
                    modifier = Modifier.size(26.dp)
                )
            } else {
                Text(
                    text = conv.name.take(1),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Center Content
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = conv.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (conv.isSafetyVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified Safety Number",
                            tint = StatusVerifiedGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Time
                conv.lastMessage?.let { msg ->
                    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp))
                    Text(
                        text = timeStr,
                        fontSize = 11.sp,
                        color = if (conv.unreadCount > 0) PrimaryEmerald else TextTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle & Unread Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val lastMsg = conv.lastMessage
                val previewText = when {
                    lastMsg == null -> "Tap to start end-to-end encrypted chat"
                    lastMsg.type == MessageType.IMAGE -> "📷 Encrypted Photo"
                    lastMsg.type == MessageType.VOICE_NOTE -> "🎤 Encrypted Voice Note (0:24)"
                    lastMsg.type == MessageType.DOCUMENT -> "📄 Encrypted Document"
                    else -> lastMsg.textContent
                }

                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (lastMsg != null && lastMsg.senderId == "user_self") {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Delivered",
                            tint = if (lastMsg.status == MessageStatus.READ) PrimaryEmerald else TextTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = previewText,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (conv.unreadCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(PrimaryEmerald),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = conv.unreadCount.toString(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnPrimaryEmerald
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CallsTabContent(
    contacts: List<Contact>,
    onStartCall: (Contact, Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceVariantDark)
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = PrimaryEmerald,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "WebRTC P2P Media Flow",
                            color = PrimaryEmerald,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Direct DTLS-SRTP 256-bit media encryption • Coturn relay fallback",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        items(contacts) { contact ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderDark))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(contact.avatarColorHex))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(contact.displayName.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(contact.displayName, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                if (contact.isVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.Verified, null, tint = StatusVerifiedGreen, modifier = Modifier.size(14.dp))
                                }
                            }
                            Text(contact.aboutText, fontSize = 11.sp, color = TextTertiary, maxLines = 1)
                        }
                    }

                    Row {
                        IconButton(onClick = { onStartCall(contact, false) }) {
                            Icon(Icons.Default.Call, contentDescription = "Voice Call", tint = PrimaryEmerald)
                        }
                        IconButton(onClick = { onStartCall(contact, true) }) {
                            Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = SecondaryCyan)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactsTabContent(
    contacts: List<Contact>,
    onContactClick: (Contact) -> Unit,
    onVerifyClick: (Contact) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(contacts) { contact ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onContactClick(contact) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderDark))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(contact.avatarColorHex))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(contact.displayName.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(contact.displayName, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(contact.username, fontSize = 12.sp, color = SecondaryCyan)
                            }
                            Text(
                                text = "Key: ${contact.identityKeyFingerprint.take(14)}...",
                                fontSize = 11.sp,
                                color = TextTertiary
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = { onVerifyClick(contact) },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (contact.isVerified) StatusVerifiedGreen else StatusUnverifiedOrange
                        )
                    ) {
                        Icon(
                            imageVector = if (contact.isVerified) Icons.Default.VerifiedUser else Icons.Default.QrCode,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (contact.isVerified) "Verified" else "Verify", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun VaultTabContent(
    onOpenInspector: () -> Unit,
    onOpenServerConfig: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Vault Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = PrimaryEmerald, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("ChatMe Zero-Knowledge Vault", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "All chat databases are encrypted locally using SQLCipher / AES-256. Identity keys never leave your device.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        // Action Buttons
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenInspector),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderDark))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = SecondaryCyan)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Cryptographic Key Ring", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text("Inspect Identity Key, PreKeys & Double Ratchet", fontSize = 12.sp, color = TextSecondary)
                    }
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextTertiary)
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenServerConfig),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderDark))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Dns, contentDescription = null, tint = PrimaryEmerald)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Self-Hosted Infrastructure", fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text("Node/Go WSS • MinIO S3 • Coturn TURN • Postgres", fontSize = 12.sp, color = TextSecondary)
                    }
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextTertiary)
            }
        }
    }
}
