package dev.budsscope.app.service

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import dev.budsscope.app.BudsScopeApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class EarbudMonitorService : Service {

    constructor() : super()

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var observeJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val app = applicationContext as? BudsScopeApplication
        val notifController = app?.notificationController
        val bluetoothRepo = app?.bluetoothRepository

        if (notifController != null) {
            val initialNotification = notifController.buildForegroundServiceNotification(
                bluetoothRepo?.activeDevice?.value?.name
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    startForeground(
                        ConnectionNotificationController.NOTIF_ID_SERVICE,
                        initialNotification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                    )
                } else {
                    startForeground(
                        ConnectionNotificationController.NOTIF_ID_SERVICE,
                        initialNotification
                    )
                }
            } else {
                startForeground(
                    ConnectionNotificationController.NOTIF_ID_SERVICE,
                    initialNotification
                )
            }
        }

        observeJob?.cancel()
        observeJob = serviceScope.launch {
            bluetoothRepo?.activeDevice?.collectLatest { active ->
                if (notifController != null) {
                    val updatedNotification = notifController.buildForegroundServiceNotification(active?.name)
                    val manager = getSystemService(NOTIFICATION_SERVICE) as? android.app.NotificationManager
                    manager?.notify(ConnectionNotificationController.NOTIF_ID_SERVICE, updatedNotification)
                }
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        observeJob?.cancel()
    }
}
