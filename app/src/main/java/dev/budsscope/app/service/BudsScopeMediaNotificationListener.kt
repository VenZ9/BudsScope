package dev.budsscope.app.service

import android.content.ComponentName
import android.content.Context
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import dev.budsscope.app.BudsScopeApplication

class BudsScopeMediaNotificationListener : NotificationListenerService() {

    private var sessionManager: MediaSessionManager? = null

    private val sessionListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        findAndAttachPrimarySession(controllers)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        try {
            sessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
            val componentName = ComponentName(this, BudsScopeMediaNotificationListener::class.java)
            sessionManager?.addOnActiveSessionsChangedListener(sessionListener, componentName)

            val initialSessions = sessionManager?.getActiveSessions(componentName)
            findAndAttachPrimarySession(initialSessions)
        } catch (_: SecurityException) {
            // Permission might be revoked
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        try {
            sessionManager?.removeOnActiveSessionsChangedListener(sessionListener)
        } catch (_: Exception) {}

        val app = applicationContext as? BudsScopeApplication
        app?.mediaSessionRepository?.setActiveController(null)
    }

    private fun findAndAttachPrimarySession(controllers: List<MediaController>?) {
        val app = applicationContext as? BudsScopeApplication ?: return
        if (controllers.isNullOrEmpty()) {
            app.mediaSessionRepository.setActiveController(null)
            return
        }

        // Prioritize actively playing controller
        val playingController = controllers.firstOrNull {
            it.playbackState?.state == PlaybackState.STATE_PLAYING
        } ?: controllers.firstOrNull()

        app.mediaSessionRepository.setActiveController(playingController)
    }
}
