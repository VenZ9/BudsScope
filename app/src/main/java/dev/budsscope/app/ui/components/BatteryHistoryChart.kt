package dev.budsscope.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.budsscope.app.data.local.entity.BatterySnapshotEntity
import dev.budsscope.app.ui.theme.AccentCyan
import dev.budsscope.app.ui.theme.SurfaceCardBorder
import dev.budsscope.app.ui.theme.TextMuted

@Composable
fun BatteryHistoryChart(
    snapshotsAscending: List<BatterySnapshotEntity>,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(160.dp)
) {
    if (snapshotsAscending.size < 2) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Recording snapshots over time…",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextMuted,
                    fontSize = 12.sp
                )
            )
        }
        return
    }

    val validPoints = snapshotsAscending.mapNotNull { it.primaryPercentage }
    if (validPoints.size < 2) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Awaiting valid percentage readings…",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted)
            )
        }
        return
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 12.dp, horizontal = 8.dp)
    ) {
        val width = size.width
        val height = size.height
        val paddingBottom = 16f
        val chartHeight = height - paddingBottom

        // Draw horizontal grid lines (100%, 50%, 0%)
        val gridLines = listOf(0f, chartHeight * 0.5f, chartHeight)
        gridLines.forEach { y ->
            drawLine(
                color = SurfaceCardBorder,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        }

        val stepX = width / (validPoints.size - 1)
        val strokePath = Path()
        val fillPath = Path()

        validPoints.forEachIndexed { index, percentage ->
            val x = index * stepX
            // Map 0..100 percentage to chart coordinates (inverted Y)
            val normalizedY = 1.0f - (percentage.toFloat() / 100f).coerceIn(0f, 1f)
            val y = normalizedY * chartHeight

            if (index == 0) {
                strokePath.moveTo(x, y)
                fillPath.moveTo(x, chartHeight)
                fillPath.lineTo(x, y)
            } else {
                strokePath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        fillPath.lineTo(width, chartHeight)
        fillPath.close()

        // Draw soft gradient area fill
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    AccentCyan.copy(alpha = 0.25f),
                    Color.Transparent
                ),
                startY = 0f,
                endY = chartHeight
            )
        )

        // Draw data curve line
        drawPath(
            path = strokePath,
            color = AccentCyan,
            style = Stroke(width = 2.5f, cap = StrokeCap.Round)
        )

        // Draw points
        validPoints.forEachIndexed { index, percentage ->
            val x = index * stepX
            val normalizedY = 1.0f - (percentage.toFloat() / 100f).coerceIn(0f, 1f)
            val y = normalizedY * chartHeight
            drawCircle(
                color = AccentCyan,
                radius = 3.5f,
                center = Offset(x, y)
            )
        }
    }
}
