package dev.budsscope.app.data.model

import android.graphics.Bitmap

/**
 * Encapsulates playback metadata extracted safely from Android media sessions.
 */
data class MediaPlaybackInfo(
    val hasActiveSession: Boolean = false,
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val isPlaying: Boolean = false,
    val durationMs: Long = 0L,
    val positionMs: Long = 0L,
    val hasArtwork: Boolean = false,
    val artworkBitmap: Bitmap? = null,
    val packageName: String? = null,
    val speed: Float = 1.0f
) {
    val progressFraction: Float
        get() = if (durationMs > 0L) {
            (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

    val formattedPosition: String
        get() = formatTime(positionMs)

    val formattedDuration: String
        get() = formatTime(durationMs)

    companion object {
        fun empty(): MediaPlaybackInfo = MediaPlaybackInfo()

        private fun formatTime(millis: Long): String {
            if (millis <= 0L) return "0:00"
            val totalSeconds = millis / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }
    }
}
