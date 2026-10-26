package dev.budsscope.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import dev.budsscope.app.ui.theme.AccentCyan
import dev.budsscope.app.ui.theme.SurfaceCardBorder
import dev.budsscope.app.ui.theme.SurfaceGraphite

@Composable
fun EarbudVectorVisual(
    isConnected: Boolean,
    isCharging: Boolean = false,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(180.dp)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val ledAlpha by infiniteTransition.animateFloat(
        initialValue = if (isConnected) 0.4f else 0.15f,
        targetValue = if (isConnected) 1.0f else 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ledAlpha"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Subtle glow aura behind buds when connected
        if (isConnected) {
            val auraBrush = Brush.radialGradient(
                colors = listOf(
                    AccentCyan.copy(alpha = 0.08f * ledAlpha),
                    Color.Transparent
                ),
                center = Offset(width / 2f, height / 2f),
                radius = width * 0.45f
            )
            drawCircle(brush = auraBrush, radius = width * 0.45f, center = Offset(width / 2f, height / 2f))
        }

        // Draw Left Earbud
        val leftCenterX = width * 0.36f
        val earbudY = height * 0.46f
        drawEarbud(
            centerX = leftCenterX,
            centerY = earbudY,
            isLeft = true,
            isConnected = isConnected,
            isCharging = isCharging,
            ledAlpha = ledAlpha
        )

        // Draw Right Earbud
        val rightCenterX = width * 0.64f
        drawEarbud(
            centerX = rightCenterX,
            centerY = earbudY,
            isLeft = false,
            isConnected = isConnected,
            isCharging = isCharging,
            ledAlpha = ledAlpha
        )
    }
}

private fun DrawScope.drawEarbud(
    centerX: Float,
    centerY: Float,
    isLeft: Boolean,
    isConnected: Boolean,
    isCharging: Boolean,
    ledAlpha: Float
) {
    val scale = 1.0f
    val mirror = if (isLeft) -1f else 1f

    // Palette
    val bodyBase = SurfaceGraphite
    val bodyHighlight = Color(0xFF2C3444)
    val bodyStroke = SurfaceCardBorder
    val tipColor = Color(0xFF1A1F29)

    val bodyBrush = Brush.linearGradient(
        colors = listOf(bodyHighlight, bodyBase),
        start = Offset(centerX - 40f, centerY - 60f),
        end = Offset(centerX + 40f, centerY + 60f)
    )

    // 1. Silicone in-ear tip
    val tipCenter = Offset(centerX - (26f * mirror), centerY - 14f)
    drawOval(
        color = tipColor,
        topLeft = Offset(tipCenter.x - 14f, tipCenter.y - 18f),
        size = Size(28f, 36f)
    )
    drawOval(
        color = bodyStroke,
        topLeft = Offset(tipCenter.x - 14f, tipCenter.y - 18f),
        size = Size(28f, 36f),
        style = Stroke(width = 1.5f)
    )

    // 2. Main Earbud Head (Acoustic Chamber)
    val headPath = Path().apply {
        addRoundRect(
            RoundRect(
                left = centerX - 26f,
                top = centerY - 38f,
                right = centerX + 26f,
                bottom = centerY + 18f,
                cornerRadius = CornerRadius(24f, 24f)
            )
        )
    }
    drawPath(path = headPath, brush = bodyBrush)
    drawPath(path = headPath, color = bodyStroke, style = Stroke(width = 1.5f))

    // Acoustic nozzle accent line
    drawLine(
        color = Color(0xFF3E4A5E),
        start = Offset(centerX - (10f * mirror), centerY - 20f),
        end = Offset(centerX - (10f * mirror), centerY + 6f),
        strokeWidth = 2f
    )

    // 3. Earbud Stem (Downward column)
    val stemWidth = 14f
    val stemHeight = 65f
    val stemLeft = centerX - (stemWidth / 2f) + (6f * mirror)
    val stemTop = centerY + 4f

    val stemPath = Path().apply {
        addRoundRect(
            RoundRect(
                left = stemLeft,
                top = stemTop,
                right = stemLeft + stemWidth,
                bottom = stemTop + stemHeight,
                bottomLeftCornerRadius = CornerRadius(7f, 7f),
                bottomRightCornerRadius = CornerRadius(7f, 7f),
                topLeftCornerRadius = CornerRadius(4f, 4f),
                topRightCornerRadius = CornerRadius(4f, 4f)
            )
        )
    }
    drawPath(path = stemPath, brush = bodyBrush)
    drawPath(path = stemPath, color = bodyStroke, style = Stroke(width = 1.5f))

    // 4. Gold Charging Contacts at bottom of stem
    val contactY = stemTop + stemHeight - 5f
    drawCircle(
        color = Color(0xFFD4AF37),
        radius = 2.2f,
        center = Offset(stemLeft + 4f, contactY)
    )
    drawCircle(
        color = Color(0xFFD4AF37),
        radius = 2.2f,
        center = Offset(stemLeft + stemWidth - 4f, contactY)
    )

    // 5. LED Status Dot
    val ledCenter = Offset(stemLeft + (stemWidth / 2f), stemTop + 14f)
    val ledColor = when {
        isCharging -> Color(0xFF10B981)
        isConnected -> AccentCyan.copy(alpha = ledAlpha)
        else -> Color(0xFF4A5568)
    }
    drawCircle(
        color = ledColor,
        radius = 3.2f,
        center = ledCenter
    )

    // LED Glow Halo
    if (isConnected || isCharging) {
        drawCircle(
            color = ledColor.copy(alpha = 0.35f * ledAlpha),
            radius = 8f,
            center = ledCenter
        )
    }
}
