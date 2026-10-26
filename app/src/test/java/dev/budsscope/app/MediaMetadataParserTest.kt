package dev.budsscope.app

import dev.budsscope.app.data.model.MediaPlaybackInfo
import dev.budsscope.app.domain.MediaMetadataParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaMetadataParserTest {

    @Test
    fun `parse with null inputs returns empty playback info`() {
        val info = MediaMetadataParser.parse(null, null, null)
        assertFalse(info.hasActiveSession)
        assertFalse(info.isPlaying)
        assertEquals("", info.title)
        assertEquals("", info.artist)
        assertEquals(0L, info.durationMs)
        assertEquals(0L, info.positionMs)
        assertEquals("0:00", info.formattedPosition)
        assertEquals("0:00", info.formattedDuration)
        assertEquals(0f, info.progressFraction, 0.001f)
    }

    @Test
    fun `progressFraction computes correctly`() {
        val halfWay = MediaPlaybackInfo(
            hasActiveSession = true,
            title = "Test Track",
            artist = "Artist",
            durationMs = 200_000L,
            positionMs = 100_000L
        )
        assertEquals(0.5f, halfWay.progressFraction, 0.001f)

        val completed = MediaPlaybackInfo(
            hasActiveSession = true,
            durationMs = 180_000L,
            positionMs = 200_000L // Exceeds duration
        )
        assertEquals(1.0f, completed.progressFraction, 0.001f)
    }

    @Test
    fun `formattedPosition formats minutes and seconds cleanly`() {
        val info1 = MediaPlaybackInfo(positionMs = 65_000L) // 1m 5s
        assertEquals("1:05", info1.formattedPosition)

        val info2 = MediaPlaybackInfo(positionMs = 215_000L) // 3m 35s
        assertEquals("3:35", info2.formattedPosition)

        val infoZero = MediaPlaybackInfo(positionMs = 0L)
        assertEquals("0:00", infoZero.formattedPosition)
    }
}
