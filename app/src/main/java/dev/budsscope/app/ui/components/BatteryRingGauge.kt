package dev.budsscope.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.budsscope.app.data.model.BatteryReading
import dev.budsscope.app.data.model.BatteryReadingStatus
import dev.budsscope.app.ui.theme.AccentCyan
import dev.budsscope.app.ui.theme.StatusAmber
import dev.budsscope.app.ui.theme.StatusGreen
import dev.budsscope.app.ui.theme.StatusRed
import dev.budsscope.app.ui.theme.SurfaceCardBorder
import dev.budsscope.app.ui.theme.TextMuted
import dev.budsscope.app.ui.theme.TextPrimary
import dev.budsscope.app.ui.theme.TextSecondary

@Composable
fun BatteryRingGauge(
    reading: BatteryReading,
    label: String,
    modifier: Modifier = Modifier,
    size: Dp = 90.dp,
    strokeWidth: Dp = 7.dp
) {
    val targetFraction = when {
        reading.isValid -> (reading.percentage!!.toFloat() / 100f).coerceIn(0f, 1f)
        else -> 0f
    }

    val animatedFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = tween(durationMillis = 800),
        label = "gaugeSweep"
    )

    val activeColor = when {
        reading.status == BatteryReadingStatus.CHARGING -> StatusGreen
        reading.percentage != null && reading.percentage <= 10 -> StatusRed
        reading.percentage != null && reading.percentage <= 20 -> StatusAmber
        reading.status == BatteryReadingStatus.STALE -> TextSecondary
        reading.isValid -> AccentCyan
        else -> SurfaceCardBorder
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(size)) {
                val strokePx = strokeWidth.toPx()
                val diameter = this.size.minDimension - strokePx
                val topLeft = Offset(strokePx / 2f, strokePx / 2f)
                val arcSize = Size(diameter, diameter)

                // Background track
                drawArc(
                    color = SurfaceCardBorder,
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )

                // Active Progress Arc
                if (animatedFraction > 0.01f) {
                    drawArc(
                        color = activeColor,
                        startAngle = 135f,
                        sweepAngle = 270f * animatedFraction,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )
                }
            }

            // Center Text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (reading.status == BatteryReadingStatus.CHARGING) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Charging",
                        tint = StatusGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }

                val displayText = when {
                    reading.isValid -> "${reading.percentage}%"
                    reading.status == BatteryReadingStatus.STALE && reading.percentage != null -> "${reading.percentage}%"
                    reading.status == BatteryReadingStatus.UNSUPPORTED -> "N/A"
                    else -> "--"
                }

                Text(
                    text = displayText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = if (size > 100.dp) 20.sp else 16.sp,
                        color = if (reading.isValid) TextPrimary else TextMuted
                    )
                )

                if (reading.status == BatteryReadingStatus.STALE) {
                    Text(
                        text = "Stale",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            color = StatusAmber
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
        )
    }
}
