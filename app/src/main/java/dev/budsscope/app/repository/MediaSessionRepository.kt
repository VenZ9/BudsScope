package dev.budsscope.app.repository

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.provider.Settings
import dev.budsscope.app.data.model.MediaPlaybackInfo
import dev.budsscope.app.domain.MediaMetadataParser
import dev.budsscope.app.service.BudsScopeMediaNotificationListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MediaSessionRepository(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val _playbackInfo = MutableStateFlow(MediaPlaybackInfo.empty())
    val playbackInfo: StateFlow<MediaPlaybackInfo> = _playbackInfo.asStateFlow()

    private var activeController: MediaController? = null

    private val controllerCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            updateFromController(activeController)
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            updateFromController(activeController)
        }

        override fun onSessionDestroyed() {
            activeController = null
            _playbackInfo.value = MediaPlaybackInfo.empty()
        }
    }

    fun isNotificationAccessGranted(): Boolean {
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ) ?: return false

        val myComponent = ComponentName(context, BudsScopeMediaNotificationListener::class.java).flattenToString()
        return enabledListeners.contains(myComponent)
    }

    fun setActiveController(controller: MediaController?) {
        try {
            activeController?.unregisterCallback(controllerCallback)
        } catch (_: Exception) {}

        activeController = controller
        if (controller != null) {
            try {
                controller.registerCallback(controllerCallback)
            } catch (_: Exception) {}
            updateFromController(controller)
        } else {
            _playbackInfo.value = MediaPlaybackInfo.empty()
        }
    }

    private fun updateFromController(controller: MediaController?) {
        if (controller == null) {
            _playbackInfo.value = MediaPlaybackInfo.empty()
            return
        }

        val metadata = try { controller.metadata } catch (_: Exception) { null }
        val playbackState = try { controller.playbackState } catch (_: Exception) { null }
        val pkg = controller.packageName

        val parsed = MediaMetadataParser.parse(metadata, playbackState, pkg)
        scope.launch(Dispatchers.Main) {
            _playbackInfo.value = parsed
        }
    }

    fun togglePlayPause() {
        val controller = activeController ?: return
        val state = controller.playbackState?.state
        try {
            if (state == PlaybackState.STATE_PLAYING) {
                controller.transportControls.pause()
            } else {
                controller.transportControls.play()
            }
        } catch (_: Exception) {}
    }

    fun skipNext() {
        try {
            activeController?.transportControls?.skipToNext()
        } catch (_: Exception) {}
    }

    fun skipPrevious() {
        try {
            activeController?.transportControls?.skipToPrevious()
        } catch (_: Exception) {}
    }
}
