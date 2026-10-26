package dev.budsscope.app.domain

import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.PlaybackState
import dev.budsscope.app.data.model.MediaPlaybackInfo

object MediaMetadataParser {

    /**
     * Parses standard Android MediaMetadata and PlaybackState into a clean domain model.
     */
    fun parse(
        metadata: MediaMetadata?,
        playbackState: PlaybackState?,
        packageName: String?
    ): MediaPlaybackInfo {
        if (metadata == null && playbackState == null) {
            return MediaPlaybackInfo.empty()
        }

        val rawTitle = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
        val rawArtist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE)
        val rawAlbum = metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM)
            ?: metadata?.getString(MediaMetadata.METADATA_KEY_DISPLAY_DESCRIPTION)

        val duration = metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION)?.coerceAtLeast(0L) ?: 0L

        // Artwork bitmap safely retrieved if available
        val artwork: Bitmap? = try {
            metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
                ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)
                ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON)
        } catch (_: Exception) {
            null
        }

        val isPlaying = playbackState?.state == PlaybackState.STATE_PLAYING ||
                playbackState?.state == PlaybackState.STATE_FAST_FORWARDING ||
                playbackState?.state == PlaybackState.STATE_REWINDING

        val rawPosition = playbackState?.position?.coerceAtLeast(0L) ?: 0L
        val playbackSpeed = playbackState?.playbackSpeed ?: 1.0f

        val title = rawTitle?.trim().takeIf { !it.isNullOrBlank() } ?: "Unknown Track"
        val artist = rawArtist?.trim().takeIf { !it.isNullOrBlank() } ?: "Unknown Artist"
        val album = rawAlbum?.trim().takeIf { !it.isNullOrBlank() } ?: "Unknown Album"

        return MediaPlaybackInfo(
            hasActiveSession = true,
            title = title,
            artist = artist,
            album = album,
            isPlaying = isPlaying,
            durationMs = duration,
            positionMs = rawPosition,
            hasArtwork = artwork != null,
            artworkBitmap = artwork,
            packageName = packageName,
            speed = playbackSpeed
        )
    }
}
