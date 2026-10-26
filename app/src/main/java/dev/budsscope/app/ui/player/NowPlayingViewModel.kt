package dev.budsscope.app.ui.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dev.budsscope.app.BudsScopeApplication
import dev.budsscope.app.data.model.MediaPlaybackInfo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class NowPlayingViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as BudsScopeApplication
    val mediaSessionRepository = app.mediaSessionRepository
    val preferencesRepository = app.preferencesRepository

    val playbackInfo: StateFlow<MediaPlaybackInfo> = mediaSessionRepository.playbackInfo

    val fluidAnimationsEnabled: StateFlow<Boolean> = preferencesRepository.fluidWaveAnimationEnabled
        .stateIn(
            scope = app.applicationScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    fun hasNotificationPermission(): Boolean = mediaSessionRepository.isNotificationAccessGranted()

    fun togglePlayPause() {
        mediaSessionRepository.togglePlayPause()
    }

    fun skipNext() {
        mediaSessionRepository.skipNext()
    }

    fun skipPrevious() {
        mediaSessionRepository.skipPrevious()
    }
}
