package dev.budsscope.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import dev.budsscope.app.R
import dev.budsscope.app.ui.MainActivity

class ConnectionNotificationController(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_BATTERY = "budsscope_battery_alerts"
        const val CHANNEL_CONNECTION = "budsscope_connection_alerts"
        const val CHANNEL_SERVICE = "budsscope_monitor_service"

        const val NOTIF_ID_BATTERY = 1001
        const val NOTIF_ID_CONNECTION = 1002
        const val NOTIF_ID_SERVICE = 1003
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val batteryChannel = NotificationChannel(
                CHANNEL_BATTERY,
                context.getString(R.string.notif_channel_battery),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notif_channel_battery_desc)
                enableVibration(true)
            }

            val connectionChannel = NotificationChannel(
                CHANNEL_CONNECTION,
                context.getString(R.string.notif_channel_connection),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notif_channel_connection_desc)
            }

            val serviceChannel = NotificationChannel(
                CHANNEL_SERVICE,
                context.getString(R.string.notif_channel_service),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.notif_channel_service_desc)
                setShowBadge(false)
            }

            notificationManager.createNotificationChannel(batteryChannel)
            notificationManager.createNotificationChannel(connectionChannel)
            notificationManager.createNotificationChannel(serviceChannel)
        }
    }

    fun notifyLowBattery(deviceName: String, batteryPercentage: Int) {
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = context.getString(R.string.notif_low_battery_title, batteryPercentage)
        val body = context.getString(R.string.notif_low_battery_content, deviceName, batteryPercentage)

        val notification = NotificationCompat.Builder(context, CHANNEL_BATTERY)
            .setSmallIcon(R.drawable.ic_widget_earbud)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        try {
            notificationManager.notify(NOTIF_ID_BATTERY, notification)
        } catch (_: SecurityException) {
            // Permission POST_NOTIFICATIONS may not be granted by user yet
        }
    }

    fun notifyDisconnection(deviceName: String) {
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = context.getString(R.string.notif_disconnected_title)
        val body = context.getString(R.string.notif_disconnected_content, deviceName)

        val notification = NotificationCompat.Builder(context, CHANNEL_CONNECTION)
            .setSmallIcon(R.drawable.ic_widget_earbud)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        try {
            notificationManager.notify(NOTIF_ID_CONNECTION, notification)
        } catch (_: SecurityException) {
            // Permission POST_NOTIFICATIONS may not be granted by user yet
        }
    }

    fun buildForegroundServiceNotification(deviceName: String?): android.app.Notification {
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            2,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val text = if (deviceName != null) {
            "Monitoring: $deviceName"
        } else {
            "Monitoring Bluetooth audio connections"
        }

        return NotificationCompat.Builder(context, CHANNEL_SERVICE)
            .setSmallIcon(R.drawable.ic_widget_earbud)
            .setContentTitle("BudsScope Active Monitor")
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
