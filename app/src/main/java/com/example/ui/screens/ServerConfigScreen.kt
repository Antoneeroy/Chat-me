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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ServerSettings
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerConfigScreen(
    serverSettings: ServerSettings,
    isTesting: Boolean,
    testResult: String?,
    onBackClick: () -> Unit,
    onTestConnection: () -> Unit,
    onSaveSettings: (ServerSettings) -> Unit
) {
    var wsUrl by remember { mutableStateOf(serverSettings.wsUrl) }
    var minioUrl by remember { mutableStateOf(serverSettings.minioUrl) }
    var turnUrl by remember { mutableStateOf(serverSettings.turnUrl) }
    var pushUrl by remember { mutableStateOf(serverSettings.pushUrl) }

    var selectedSection by remember { mutableStateOf(0) } // 0: Server Setup, 1: Technical & Cost Brief (Hindi/Hinglish)

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
                        text = "Self-Hosted Infrastructure",
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
            // Segmented Switch: Endpoints vs Technical & Cost Brief
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceVariantDark)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedSection == 0) PrimaryEmerald else Color.Transparent)
                        .clickable { selectedSection = 0 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Endpoints & Ping",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedSection == 0) OnPrimaryEmerald else TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedSection == 1) PrimaryEmerald else Color.Transparent)
                        .clickable { selectedSection = 1 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Cost & Tech Roadmap",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedSection == 1) OnPrimaryEmerald else TextSecondary
                    )
                }
            }

            if (selectedSection == 0) {
                // Section 0: Server Endpoints & Ping
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderDark))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Connect to Your Own Self-Hosted Stack",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "No Firebase, AWS, or Twilio. Zero third-party data tracking. All media stored on private MinIO S3 bucket.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        // WebSocket Gateway URL
                        OutlinedTextField(
                            value = wsUrl,
                            onValueChange = { wsUrl = it },
                            label = { Text("WebSocket Gateway (WSS)", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryEmerald,
                                unfocusedBorderColor = BorderDark,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        // MinIO Object Storage URL
                        OutlinedTextField(
                            value = minioUrl,
                            onValueChange = { minioUrl = it },
                            label = { Text("MinIO S3 Media Endpoint", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryEmerald,
                                unfocusedBorderColor = BorderDark,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        // Coturn STUN/TURN URL
                        OutlinedTextField(
                            value = turnUrl,
                            onValueChange = { turnUrl = it },
                            label = { Text("Coturn TURN/STUN Server", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryEmerald,
                                unfocusedBorderColor = BorderDark,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        // Push Notification Distributor
                        OutlinedTextField(
                            value = pushUrl,
                            onValueChange = { pushUrl = it },
                            label = { Text("UnifiedPush / ntfy Alert Endpoint", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryEmerald,
                                unfocusedBorderColor = BorderDark,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onTestConnection,
                                enabled = !isTesting,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryEmerald, contentColor = OnPrimaryEmerald),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                if (isTesting) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = OnPrimaryEmerald, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Pinging...")
                                } else {
                                    Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Test Ping")
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    onSaveSettings(
                                        serverSettings.copy(
                                            wsUrl = wsUrl,
                                            minioUrl = minioUrl,
                                            turnUrl = turnUrl,
                                            pushUrl = pushUrl
                                        )
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = SecondaryCyan),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Save Settings")
                            }
                        }
                    }
                }

                testResult?.let { result ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceVariantDark),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(PrimaryEmerald))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = PrimaryEmerald, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(result, color = TextPrimary, fontSize = 12.sp, lineHeight = 18.sp)
                        }
                    }
                }
            } else {
                // Section 1: Detailed Technical Brief & Answers in Hindi/Hinglish
                RoadmapCard(
                    title = "1. AGPL License Ka Asar Mere App Pe?",
                    highlight = "Matrix/Element fork vs Clean Architecture",
                    content = "• AGPL v3 ka niyam hai: Agar aap unka server-side backend code modify karke online host karte hain, to modified server code open source karna zaroori hota hai.\n• Solution for You: Agar aap client app (mobile) ko Matrix-Rust-SDK ya standard libsignal client use karke banate hain, to mobile app proprietary reh sakti hai!\n• AGPL se bachne ka sabse safe tareeka: Backend ke liye Node.js/Go ka lightweight custom API gateway use karo (MIT/Apache 2.0 compatible) aur standard Libsignal library use karo."
                )

                RoadmapCard(
                    title = "2. Total Time Aur Kharcha Kitna Lagega?",
                    highlight = "Phased Delivery: 4-6 Mahine",
                    content = "• Phase 1 (Chat + Signal E2EE + Auth): 4-6 hafte\n• Phase 2 (Encrypted Photo/Video/MinIO): 3-4 hafte\n• Phase 3 & 4 (Voice & Video Call via Coturn): 4-6 hafte\n• Phase 5 & 6 (Group Call + Polish): 4 hafte\n• Total Time: 4 se 6 mahine\n• Team Kharcha (Lean/Freelance): 1 Mobile Dev + 1 Backend/WebRTC Dev = approx ₹3,00,000 - ₹5,00,000 ($3,500 - $6,000)."
                )

                RoadmapCard(
                    title = "3. Server Kahan Chalega & Monthly Kharcha?",
                    highlight = "Hetzner Cloud ya DigitalOcean (~$25-$40/month)",
                    content = "• Germany/Finland (Hetzner Cloud CPX21 - 3 vCPU, 4GB RAM): Sirf €8/month (~₹750/mo) for App Server + Postgres + Redis.\n• Dedicated TURN Server (Coturn with Public IP): €5/month (~₹450/mo) for relaying media when NAT is strict.\n• MinIO Storage Server (100GB-500GB SSD): €10/month (~₹900/mo).\n• Total Monthly Hosting: ~$25 - $40 / month (approx ₹2,500 - ₹3,500 / month) for 5,000 - 15,000 active users!"
                )

                RoadmapCard(
                    title = "4. Security Kaise Check Hogi?",
                    highlight = "Audited Libs + Penetration Testing",
                    content = "• Audited Crypto Only: Khud ka cryptography code nahi likhna; standard libsignal / Sodium / Matrix Olm use hota hai.\n• WebRTC DTLS-SRTP: End-to-end media encryption verification via SAS security words.\n• Local Storage: SQLCipher (AES-256) on-device encryption + Android Keystore.\n• Pen-Testing: Launch se pehle automated OWASP MASVS checks and 3rd party penetration test to ensure zero plaintext leak."
                )
            }
        }
    }
}

@Composable
private fun RoadmapCard(
    title: String,
    highlight: String,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderDark))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(highlight, fontSize = 12.sp, color = PrimaryEmerald, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(content, fontSize = 12.sp, color = TextSecondary, lineHeight = 19.sp)
        }
    }
}
