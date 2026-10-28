package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryEmerald
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun VoiceWaveformVisualizer(
    durationSec: Int = 24,
    modifier: Modifier = Modifier,
    barColor: Color = PrimaryEmerald
) {
    var isPlaying by remember { mutableStateOf(false) }
    var currentPlaybackSec by remember { mutableStateOf(0) }

    // Waveform sample heights (normalized 0.15 to 1.0)
    val bars = remember {
        listOf(
            0.25f, 0.45f, 0.70f, 0.90f, 0.60f, 0.35f, 0.50f, 0.85f,
            1.00f, 0.75f, 0.40f, 0.30f, 0.65f, 0.80f, 0.95f, 0.55f,
            0.40f, 0.70f, 0.85f, 0.60f, 0.45f, 0.30f, 0.50f, 0.75f,
            0.90f, 0.65f, 0.40f, 0.25f
        )
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isPlaying && currentPlaybackSec < durationSec) {
                delay(1000)
                currentPlaybackSec++
            }
            if (currentPlaybackSec >= durationSec) {
                isPlaying = false
                currentPlaybackSec = 0
            }
        }
    }

    val progress = if (durationSec > 0) currentPlaybackSec.toFloat() / durationSec.toFloat() else 0f

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Play / Pause Circle
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(barColor)
                .clickable { isPlaying = !isPlaying },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.Black,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Waveform Bars Canvas
        Column(modifier = Modifier.weight(1f)) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
            ) {
                val totalBars = bars.size
                val barWidth = (size.width / totalBars) * 0.65f
                val spacing = (size.width - (totalBars * barWidth)) / (totalBars - 1)
                val activeBarIndex = (progress * totalBars).toInt()

                bars.forEachIndexed { index, heightMultiplier ->
                    val x = index * (barWidth + spacing)
                    val barHeight = size.height * heightMultiplier
                    val y = (size.height - barHeight) / 2

                    val isBarActive = index <= activeBarIndex
                    val color = if (isBarActive) barColor else barColor.copy(alpha = 0.3f)

                    drawRoundRect(
                        color = color,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Time indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val currentMin = currentPlaybackSec / 60
                val currentRemSec = currentPlaybackSec % 60
                val totalMin = durationSec / 60
                val totalRemSec = durationSec % 60

                Text(
                    text = String.format("%d:%02d", currentMin, currentRemSec),
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Text(
                    text = String.format("%d:%02d", totalMin, totalRemSec),
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
