package dev.budsscope.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import dev.budsscope.app.ui.theme.AccentBlue
import dev.budsscope.app.ui.theme.AccentCyan
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun LiquidSoundWave(
    isPlaying: Boolean,
    animationsEnabled: Boolean = true,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(100.dp)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveMotion")

    // Smooth phase animation
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isPlaying && animationsEnabled) (2 * PI).toFloat() else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    // Amplitude decays gracefully to near-zero when paused
    val amplitudeMultiplier by animateFloatAsState(
        targetValue = if (isPlaying) 1.0f else 0.08f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "amplitude"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2f

        if (width <= 0f || height <= 0f) return@Canvas

        // Draw Layer 1: Electric Cyan fluid ribbon
        drawWaveRibbon(
            width = width,
            centerY = centerY,
            amplitude = height * 0.38f * amplitudeMultiplier,
            frequency = 1.8f,
            phase = phase,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    AccentCyan.copy(alpha = 0.85f),
                    AccentBlue.copy(alpha = 0.95f),
                    AccentCyan.copy(alpha = 0.85f)
                )
            ),
            strokeWidth = 3.5f
        )

        // Draw Layer 2: Muted Blue secondary wave (offset phase & inverted frequency)
        drawWaveRibbon(
            width = width,
            centerY = centerY,
            amplitude = height * 0.26f * amplitudeMultiplier,
            frequency = 2.4f,
            phase = phase * 1.3f + 1.2f,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    AccentBlue.copy(alpha = 0.45f),
                    AccentCyan.copy(alpha = 0.65f),
                    AccentBlue.copy(alpha = 0.45f)
                )
            ),
            strokeWidth = 2.0f
        )

        // Draw Layer 3: Faint background ambient glow ribbon
        drawWaveRibbon(
            width = width,
            centerY = centerY,
            amplitude = height * 0.18f * amplitudeMultiplier,
            frequency = 3.1f,
            phase = -phase * 0.9f,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    AccentCyan.copy(alpha = 0.35f * amplitudeMultiplier),
                    Color.Transparent
                )
            ),
            strokeWidth = 6.0f
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawWaveRibbon(
    width: Float,
    centerY: Float,
    amplitude: Float,
    frequency: Float,
    phase: Float,
    brush: Brush,
    strokeWidth: Float
) {
    val path = Path()
    val step = 4f
    var x = 0f

    // Windowing envelope so edges taper smoothly to zero at screen boundaries
    fun envelope(xPos: Float): Float {
        val norm = xPos / width
        return sin(norm * PI).toFloat()
    }

    path.moveTo(0f, centerY)

    while (x <= width) {
        val env = envelope(x)
        val angle = (x / width) * 2f * PI.toFloat() * frequency + phase
        val y = centerY + sin(angle) * amplitude * env
        path.lineTo(x, y)
        x += step
    }

    drawPath(
        path = path,
        brush = brush,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
}
