package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.crypto.CryptoEngine
import com.example.ui.theme.PrimaryEmerald
import com.example.ui.theme.SurfaceDark

@Composable
fun QrCodeCanvas(
    data: String,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    moduleColor: Color = PrimaryEmerald,
    backgroundColor: Color = SurfaceDark
) {
    val matrix = remember(data) {
        CryptoEngine.generateQrMatrix(data)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size - 28.dp)) {
            val matrixSize = matrix.size
            val moduleWidth = this.size.width / matrixSize
            val moduleHeight = this.size.height / matrixSize

            for (r in 0 until matrixSize) {
                for (c in 0 until matrixSize) {
                    if (matrix[r][c]) {
                        drawRoundRect(
                            color = moduleColor,
                            topLeft = Offset(c * moduleWidth, r * moduleHeight),
                            size = Size(moduleWidth * 0.95f, moduleHeight * 0.95f),
                            cornerRadius = CornerRadius(moduleWidth * 0.2f, moduleHeight * 0.2f)
                        )
                    }
                }
            }
        }
    }
}
