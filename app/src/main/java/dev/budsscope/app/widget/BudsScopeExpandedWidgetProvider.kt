package dev.budsscope.app.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import dev.budsscope.app.BudsScopeApplication

class BudsScopeExpandedWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val app = context.applicationContext as? BudsScopeApplication
        val activeDevice = app?.bluetoothRepository?.activeDevice?.value
        WidgetUpdateHelper.updateAllWidgets(context, activeDevice)
    }

    override fun onEnabled(context: Context) {
        val app = context.applicationContext as? BudsScopeApplication
        val activeDevice = app?.bluetoothRepository?.activeDevice?.value
        WidgetUpdateHelper.updateAllWidgets(context, activeDevice)
    }
}
