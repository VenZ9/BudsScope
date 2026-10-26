package dev.budsscope.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import dev.budsscope.app.R
import dev.budsscope.app.data.model.BluetoothDeviceInfo
import dev.budsscope.app.ui.MainActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object WidgetUpdateHelper {

    fun updateAllWidgets(context: Context, deviceInfo: BluetoothDeviceInfo?) {
        val appWidgetManager = AppWidgetManager.getInstance(context)

        // Update Compact Widgets
        val compactComponent = ComponentName(context, BudsScopeCompactWidgetProvider::class.java)
        val compactIds = appWidgetManager.getAppWidgetIds(compactComponent)
        for (id in compactIds) {
            updateCompactWidget(context, appWidgetManager, id, deviceInfo)
        }

        // Update Expanded Widgets
        val expandedComponent = ComponentName(context, BudsScopeExpandedWidgetProvider::class.java)
        val expandedIds = appWidgetManager.getAppWidgetIds(expandedComponent)
        for (id in expandedIds) {
            updateExpandedWidget(context, appWidgetManager, id, deviceInfo)
        }
    }

    private fun updateCompactWidget(
        context: Context,
        manager: AppWidgetManager,
        widgetId: Int,
        deviceInfo: BluetoothDeviceInfo?
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_compact)

        val clickIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            widgetId,
            clickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_compact_root, pendingIntent)

        if (deviceInfo != null && deviceInfo.isConnected) {
            views.setTextViewText(R.id.widget_compact_device_name, deviceInfo.name)
            views.setTextViewText(R.id.widget_compact_status, context.getString(R.string.status_connected))

            val reading = deviceInfo.batteryState.primaryReading
            if (reading.isValid) {
                val prefix = if (reading.isCharging) "⚡ " else ""
                views.setTextViewText(R.id.widget_compact_battery, "$prefix${reading.percentage}%")
            } else {
                views.setTextViewText(R.id.widget_compact_battery, "--")
            }
        } else {
            views.setTextViewText(R.id.widget_compact_device_name, context.getString(R.string.widget_no_device))
            views.setTextViewText(R.id.widget_compact_status, context.getString(R.string.status_disconnected))
            views.setTextViewText(R.id.widget_compact_battery, "--")
        }

        manager.updateAppWidget(widgetId, views)
    }

    private fun updateExpandedWidget(
        context: Context,
        manager: AppWidgetManager,
        widgetId: Int,
        deviceInfo: BluetoothDeviceInfo?
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_expanded)

        val clickIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            widgetId + 1000,
            clickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_expanded_root, pendingIntent)

        if (deviceInfo != null && deviceInfo.isConnected) {
            views.setTextViewText(R.id.widget_expanded_device_name, deviceInfo.name)
            views.setTextViewText(R.id.widget_expanded_status_badge, context.getString(R.string.status_connected))

            val battery = deviceInfo.batteryState

            // Left
            val leftText = when {
                battery.left.isCharging -> "⚡ ${battery.left.percentage}%"
                battery.left.isValid -> "${battery.left.percentage}%"
                battery.combined.isValid -> "${battery.combined.percentage}%"
                else -> "--"
            }
            views.setTextViewText(R.id.widget_battery_left, leftText)

            // Right
            val rightText = when {
                battery.right.isCharging -> "⚡ ${battery.right.percentage}%"
                battery.right.isValid -> "${battery.right.percentage}%"
                battery.combined.isValid -> "${battery.combined.percentage}%"
                else -> "--"
            }
            views.setTextViewText(R.id.widget_battery_right, rightText)

            // Case
            val caseText = when {
                battery.case.isCharging -> "⚡ ${battery.case.percentage}%"
                battery.case.isValid -> "${battery.case.percentage}%"
                else -> "N/A"
            }
            views.setTextViewText(R.id.widget_battery_case, caseText)

            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            views.setTextViewText(R.id.widget_expanded_timestamp, "Updated " + timeFormat.format(Date()))
        } else {
            views.setTextViewText(R.id.widget_expanded_device_name, context.getString(R.string.widget_no_device))
            views.setTextViewText(R.id.widget_expanded_status_badge, context.getString(R.string.status_disconnected))
            views.setTextViewText(R.id.widget_battery_left, "--")
            views.setTextViewText(R.id.widget_battery_right, "--")
            views.setTextViewText(R.id.widget_battery_case, "--")
            views.setTextViewText(R.id.widget_expanded_timestamp, "")
        }

        manager.updateAppWidget(widgetId, views)
    }
}
