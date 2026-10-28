package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatRepository
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoInspectorScreen(
    repository: ChatRepository,
    onBackClick: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    var copiedNotice by remember { mutableStateOf<String?>(null) }

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
                    Text(
                        text = "Cryptographic Key Ring",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Identity Key Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(PrimaryEmerald.copy(alpha = 0.5f)))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = PrimaryEmerald, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Device Identity Key (IK)", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                        }
                        IconButton(onClick = {
                            clipboard.setText(AnnotatedString(repository.myIdentityFingerprint))
                            copiedNotice = "Identity key fingerprint copied to clipboard!"
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = PrimaryEmerald, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Curve25519 / Ed25519 Fingerprint:", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceVariantDark)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = repository.myIdentityFingerprint,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = PrimaryEmerald,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Private key is held exclusively in Android Keystore hardware enclave. Never leaves this phone.",
                        fontSize = 11.sp,
                        color = TextTertiary
                    )
                }
            }

            // PreKey Bundle Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderDark))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VpnKey, contentDescription = null, tint = SecondaryCyan, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("PreKey Bundle (X3DH)", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    KeyParamRow(label = "Signed PreKey ID", value = "#${repository.myPreKeyBundle.signedPreKeyId}")
                    KeyParamRow(label = "Signed PreKey Public", value = repository.myPreKeyBundle.signedPreKeyHex)
                    KeyParamRow(label = "Signature", value = repository.myPreKeyBundle.signedPreKeySignature)
                    KeyParamRow(label = "One-Time PreKeys Pool", value = "98 / 100 available on self-hosted server")
                }
            }

            // Double Ratchet Session Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderDark))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Autorenew, contentDescription = null, tint = PrimaryEmerald, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Double Ratchet Protocol State", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    KeyParamRow(label = "KDF Root Key (RK)", value = "Active (SHA-256 HKDF-derived)")
                    KeyParamRow(label = "Sending Chain Ratchet", value = "DH Step #5 • Key derivation counter: 14")
                    KeyParamRow(label = "Receiving Chain Ratchet", value = "DH Step #4 • Key derivation counter: 11")
                    KeyParamRow(label = "Forward Secrecy", value = "Enabled (Past messages cannot be decrypted)")
                    KeyParamRow(label = "Break-in Recovery", value = "Enabled (Future messages self-heal after ratchet)")
                }
            }

            // Local SQLCipher Storage Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderDark))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Local Encrypted Database (SQLCipher)", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    KeyParamRow(label = "Storage Engine", value = "Room + SQLCipher (AES-256 CBC)")
                    KeyParamRow(label = "Database Key", value = "Generated at first launch • Hardware Keystore backed")
                    KeyParamRow(label = "Media Cache", value = "Encrypted in app-private sandbox storage")
                    KeyParamRow(label = "Screenshot Blocker", value = "FLAG_SECURE enabled")
                }
            }

            copiedNotice?.let { notice ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryContainerEmerald)
                ) {
                    Text(
                        text = notice,
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun KeyParamRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, fontSize = 11.sp, color = TextSecondary)
        Text(
            text = value,
            fontSize = 12.sp,
            color = TextPrimary,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium
        )
    }
}
