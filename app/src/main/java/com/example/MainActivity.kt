package com.example

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CryptoDetailsDialog
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ChatViewModel
import com.example.viewmodel.ScreenState

class MainActivity : ComponentActivity() {

    private val viewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val conversations by viewModel.conversations.collectAsStateWithLifecycle()
                val contacts by viewModel.contacts.collectAsStateWithLifecycle()
                val messagesMap by viewModel.messages.collectAsStateWithLifecycle()
                val serverSettings by viewModel.serverSettings.collectAsStateWithLifecycle()
                val securityPrefs by viewModel.securityPreferences.collectAsStateWithLifecycle()
                val activeCall by viewModel.activeCall.collectAsStateWithLifecycle()

                var showNewChatDialog by remember { mutableStateOf(false) }

                // Manage FLAG_SECURE for screenshot blocking
                LaunchedEffect(securityPrefs.blockScreenshots) {
                    if (securityPrefs.blockScreenshots) {
                        window.setFlags(
                            WindowManager.LayoutParams.FLAG_SECURE,
                            WindowManager.LayoutParams.FLAG_SECURE
                        )
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    }
                }

                // Handle Android System Back button
                if (uiState.currentScreen != ScreenState.MAIN && uiState.currentScreen != ScreenState.LOCK) {
                    BackHandler {
                        viewModel.navigateBack()
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    when (uiState.currentScreen) {
                        ScreenState.LOCK -> {
                            LockScreen(
                                enteredPin = uiState.enteredPin,
                                pinError = uiState.pinError,
                                onDigitClick = { viewModel.enterPinDigit(it) },
                                onBackspace = { viewModel.deletePinDigit() },
                                onBiometricClick = { viewModel.unlockWithBiometrics() }
                            )
                        }

                        ScreenState.MAIN -> {
                            ChatListScreen(
                                conversations = conversations,
                                contacts = contacts,
                                selectedTab = uiState.selectedTab,
                                filterType = uiState.filterType,
                                searchQuery = uiState.searchQuery,
                                onTabSelected = { viewModel.selectTab(it) },
                                onFilterSelected = { viewModel.setFilterType(it) },
                                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                onConversationClick = { convId ->
                                    viewModel.openConversation(convId)
                                },
                                onStartCallClick = { contact, isVideo ->
                                    viewModel.startCall(contact, isVideo)
                                },
                                onContactSafetyClick = { contact ->
                                    viewModel.openSafetyNumber(contact)
                                },
                                onOpenCryptoInspector = {
                                    viewModel.openCryptoInspector()
                                },
                                onOpenServerConfig = {
                                    viewModel.openServerConfig()
                                },
                                onNewChatClick = {
                                    showNewChatDialog = true
                                }
                            )
                        }

                        ScreenState.CHAT_DETAIL -> {
                            val activeConv = conversations.find { it.id == uiState.activeConversationId }
                            val activeMessages = messagesMap[uiState.activeConversationId] ?: emptyList()
                            val otherContact = contacts.find { contact ->
                                activeConv?.participantIds?.contains(contact.id) == true && contact.id != viewModel.repository.myUserId
                            }

                            if (activeConv != null) {
                                ChatDetailScreen(
                                    conversation = activeConv,
                                    messages = activeMessages,
                                    contact = otherContact,
                                    peerIsTyping = uiState.peerIsTyping,
                                    onBackClick = { viewModel.navigateBack() },
                                    onSendMessage = { text, type, fileSize, duration, thumbColor, replyTo ->
                                        viewModel.sendMessage(
                                            text = text,
                                            type = type,
                                            fileSizeStr = fileSize,
                                            mediaDurationSec = duration,
                                            mediaThumbnailColor = thumbColor,
                                            replyTo = replyTo
                                        )
                                    },
                                    onStartCall = { contact, isVideo ->
                                        viewModel.startCall(contact, isVideo)
                                    },
                                    onInspectCrypto = { msg ->
                                        viewModel.inspectMessageCrypto(msg)
                                    },
                                    onAddReaction = { msgId, emoji ->
                                        viewModel.addReaction(msgId, emoji)
                                    },
                                    onDeleteMessage = { msgId ->
                                        viewModel.deleteMessage(msgId)
                                    },
                                    onOpenSafetyNumber = { contact ->
                                        viewModel.openSafetyNumber(contact)
                                    },
                                    onSetDisappearingTimer = { seconds ->
                                        viewModel.setDisappearingTimer(seconds)
                                    }
                                )
                            }
                        }

                        ScreenState.CALL_ACTIVE -> {
                            activeCall?.let { callSession ->
                                CallScreen(
                                    callSession = callSession,
                                    onEndCall = { viewModel.endCall() },
                                    onToggleMute = { viewModel.repository.toggleMuteCall() },
                                    onToggleCamera = { viewModel.repository.toggleCameraCall() },
                                    onToggleSpeaker = { viewModel.repository.toggleSpeakerCall() }
                                )
                            }
                        }

                        ScreenState.SAFETY_NUMBER -> {
                            uiState.activeSafetyContact?.let { contact ->
                                SafetyNumberScreen(
                                    contact = contact,
                                    myFingerprint = viewModel.repository.myIdentityFingerprint,
                                    onBackClick = { viewModel.navigateBack() },
                                    onToggleVerified = { contactId ->
                                        viewModel.toggleSafetyVerification(contactId)
                                    }
                                )
                            }
                        }

                        ScreenState.CRYPTO_INSPECTOR -> {
                            CryptoInspectorScreen(
                                repository = viewModel.repository,
                                onBackClick = { viewModel.navigateBack() }
                            )
                        }

                        ScreenState.SERVER_CONFIG -> {
                            ServerConfigScreen(
                                serverSettings = serverSettings,
                                isTesting = uiState.isServerTesting,
                                testResult = uiState.serverPingResult,
                                onBackClick = { viewModel.navigateBack() },
                                onTestConnection = { viewModel.testServerConnection() },
                                onSaveSettings = { newSettings ->
                                    viewModel.repository.updateServerSettings(newSettings)
                                }
                            )
                        }
                    }

                    // Encryption Inspector Dialog Modal
                    uiState.selectedMessageForCryptoDetails?.let { message ->
                        CryptoDetailsDialog(
                            message = message,
                            onDismiss = { viewModel.inspectMessageCrypto(null) }
                        )
                    }

                    // New Chat Dialog
                    if (showNewChatDialog) {
                        NewChatDialog(
                            contacts = contacts,
                            onDismiss = { showNewChatDialog = false },
                            onSelectContact = { contact ->
                                val convId = viewModel.repository.createNewChat(contact)
                                viewModel.openConversation(convId)
                            },
                            onCreateGroup = { name, memberIds ->
                                val convId = viewModel.repository.createGroupChat(name, memberIds)
                                viewModel.openConversation(convId)
                            }
                        )
                    }
                }
            }
        }
    }
}
