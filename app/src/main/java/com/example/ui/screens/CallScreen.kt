package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CallSession
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun CallScreen(
    callSession: CallSession,
    onEndCall: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleCamera: () -> Unit,
    onToggleSpeaker: () -> Unit
) {
    var elapsedSeconds by remember { mutableStateOf(14) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            elapsedSeconds++
        }
    }

    val min = elapsedSeconds / 60
    val sec = elapsedSeconds % 60
    val timerStr = String.format("%02d:%02d", min, sec)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070E14))
    ) {
        // Video Background / Simulated Stream
        if (callSession.isVideo && !callSession.isCameraOff) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF0F2636),
                                Color(0xFF163E56),
                                Color(0xFF0A1721)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color(android.graphics.Color.parseColor(callSession.contact.avatarColorHex))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            callSession.contact.displayName.take(1),
                            color = Color.White,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Encrypted WebRTC Video Stream",
                        fontSize = 13.sp,
                        color = SecondaryCyan
                    )
                }
            }

            // Local Camera PiP (Top Right)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 16.dp, end = 16.dp)
                    .size(width = 110.dp, height = 150.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF1B3547)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "You",
                        tint = PrimaryEmerald,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("You (Front Cam)", fontSize = 10.sp, color = Color.White)
                }
            }
        } else {
            // Voice Call Center Avatar
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color(android.graphics.Color.parseColor(callSession.contact.avatarColorHex))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        callSession.contact.displayName.take(1),
                        color = Color.White,
                        fontSize = 54.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = callSession.contact.displayName,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = timerStr,
                    fontSize = 16.sp,
                    color = PrimaryEmerald,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Top Info & Encryption Header
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            // DTLS-SRTP Encryption Badge
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.9f)),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(PrimaryEmerald.copy(alpha = 0.6f)))
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = PrimaryEmerald,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DTLS-SRTP 256-bit Media Encrypted",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryEmerald
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "SAS Security Code: ${callSession.sasVerificationCode}",
                        fontSize = 11.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "ICE: ${callSession.iceConnectionState} • Coturn Ready",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Bottom Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 36.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mute Mic Button
                CallControlButton(
                    icon = if (callSession.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    isActive = callSession.isMuted,
                    activeColor = AccentRed,
                    onClick = onToggleMute,
                    label = if (callSession.isMuted) "Unmute" else "Mute"
                )

                // Camera Toggle Button
                CallControlButton(
                    icon = if (callSession.isCameraOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                    isActive = callSession.isCameraOff,
                    activeColor = AccentRed,
                    onClick = onToggleCamera,
                    label = if (callSession.isCameraOff) "Start Cam" else "Stop Cam"
                )

                // Speaker Toggle Button
                CallControlButton(
                    icon = if (callSession.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                    isActive = callSession.isSpeakerOn,
                    activeColor = PrimaryEmerald,
                    onClick = onToggleSpeaker,
                    label = "Speaker"
                )

                // End Call Red Button
                IconButton(
                    onClick = onEndCall,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(AccentRed)
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CallControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(if (isActive) activeColor.copy(alpha = 0.25f) else SurfaceVariantDark)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) activeColor else TextPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, fontSize = 11.sp, color = TextSecondary)
    }
}
