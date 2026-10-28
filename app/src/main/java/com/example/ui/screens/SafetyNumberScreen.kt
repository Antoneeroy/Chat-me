package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crypto.CryptoEngine
import com.example.model.Contact
import com.example.ui.components.QrCodeCanvas
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafetyNumberScreen(
    contact: Contact,
    myFingerprint: String,
    onBackClick: () -> Unit,
    onToggleVerified: (String) -> Unit
) {
    val clipboard = LocalClipboardManager.current
    var isScanMode by remember { mutableStateOf(false) }
    var scanSuccessMessage by remember { mutableStateOf<String?>(null) }

    val safetyNumber = remember(contact.identityKeyFingerprint, myFingerprint) {
        CryptoEngine.computeSafetyNumber(myFingerprint, contact.identityKeyFingerprint)
    }

    val blocks = remember(safetyNumber) {
        safetyNumber.split(" ")
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
                    Text(
                        text = "Verify Safety Number",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    IconButton(onClick = {
                        clipboard.setText(AnnotatedString(safetyNumber))
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Number", tint = PrimaryEmerald)
                    }
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mode Selector (Your Code vs Scan Code)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceVariantDark)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!isScanMode) PrimaryEmerald else Color.Transparent)
                        .clickable { isScanMode = false }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Your QR Code",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (!isScanMode) OnPrimaryEmerald else TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isScanMode) PrimaryEmerald else Color.Transparent)
                        .clickable { isScanMode = true }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Scan Contact's QR",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isScanMode) OnPrimaryEmerald else TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!isScanMode) {
                // QR Code
                QrCodeCanvas(
                    data = safetyNumber,
                    size = 220.dp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Signal Protocol 60-Digit Safety Number",
                    fontSize = 12.sp,
                    color = PrimaryEmerald,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 12 blocks of 5 digits displayed in 3 columns of 4 rows
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderDark))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (row in 0 until 4) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                for (col in 0 until 3) {
                                    val index = row * 3 + col
                                    if (index < blocks.size) {
                                        Text(
                                            text = blocks[index],
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Simulated Camera Scanner View
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0F1E29)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = PrimaryEmerald,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Point camera at ${contact.displayName}'s QR Code",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                onToggleVerified(contact.id)
                                scanSuccessMessage = "Safety Number Verified! Identity keys matched with 100% cryptographic integrity."
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald, contentColor = OnPrimaryEmerald),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Simulate Camera QR Match")
                        }
                    }
                }

                scanSuccessMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = PrimaryContainerEmerald)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = PrimaryEmerald, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(msg, color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Verified Status Switch
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (contact.isVerified) Icons.Default.VerifiedUser else Icons.Default.Security,
                            contentDescription = null,
                            tint = if (contact.isVerified) StatusVerifiedGreen else StatusUnverifiedOrange,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Mark as Verified", fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(
                                if (contact.isVerified) "Verified • Green shield active" else "Unverified • Compare safety number",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Switch(
                        checked = contact.isVerified,
                        onCheckedChange = { onToggleVerified(contact.id) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PrimaryEmerald,
                            checkedTrackColor = PrimaryContainerEmerald
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Explanation Card
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Why verify safety numbers?",
                        fontWeight = FontWeight.Bold,
                        color = SecondaryCyan,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Comparing safety numbers verifies that your messages and calls are encrypted with ${contact.displayName} and cannot be intercepted by any server, ISP, or man-in-the-middle attacker. If their phone changes or keys are re-installed, this number changes.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}
