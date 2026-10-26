package dev.budsscope.app.service

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.budsscope.app.BudsScopeApplication
import dev.budsscope.app.data.local.entity.ConnectionEventEntity
import dev.budsscope.app.domain.ConnectionStateTracker
import dev.budsscope.app.widget.WidgetUpdateHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BluetoothMonitorReceiver : BroadcastReceiver() {

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val app = context.applicationContext as? BudsScopeApplication ?: return
        val bluetoothRepo = app.bluetoothRepository
        val batteryRepo = app.batteryRepository
        val connectionDao = app.database.connectionDao()
        val prefs = app.preferencesRepository
        val notifController = app.notificationController

        when (action) {
            BluetoothDevice.ACTION_ACL_CONNECTED -> {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                val deviceName = try { device?.name ?: "Bluetooth Device" } catch (_: SecurityException) { "Bluetooth Device" }
                val deviceAddress = device?.address ?: "00:00:00:00:00:00"

                CoroutineScope(Dispatchers.IO).launch {
                    val recent = connectionDao.getEventsForDevice(deviceAddress, limit = 1).firstOrNull()
                    if (ConnectionStateTracker.shouldRecordEvent("CONNECTED", deviceAddress, recent)) {
                        connectionDao.insertEvent(
                            ConnectionEventEntity(
                                deviceAddress = deviceAddress,
                                deviceName = deviceName,
                                eventType = "CONNECTED"
                            )
                        )
                    }
                }

                bluetoothRepo.refreshReadings()
                WidgetUpdateHelper.updateAllWidgets(context, bluetoothRepo.activeDevice.value)
            }

            BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                val deviceName = try { device?.name ?: "Bluetooth Device" } catch (_: SecurityException) { "Bluetooth Device" }
                val deviceAddress = device?.address ?: "00:00:00:00:00:00"

                CoroutineScope(Dispatchers.IO).launch {
                    val recent = connectionDao.getEventsForDevice(deviceAddress, limit = 1).firstOrNull()
                    if (ConnectionStateTracker.shouldRecordEvent("DISCONNECTED", deviceAddress, recent)) {
                        connectionDao.insertEvent(
                            ConnectionEventEntity(
                                deviceAddress = deviceAddress,
                                deviceName = deviceName,
                                eventType = "DISCONNECTED"
                            )
                        )
                    }

                    val disconnectAlertEnabled = prefs.disconnectAlertEnabled.first()
                    if (disconnectAlertEnabled) {
                        notifController.notifyDisconnection(deviceName)
                    }
                }

                bluetoothRepo.refreshReadings()
                WidgetUpdateHelper.updateAllWidgets(context, null)
            }

            "android.bluetooth.device.action.BATTERY_LEVEL_CHANGED" -> {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                val batteryLevel = intent.getIntExtra("android.bluetooth.device.extra.BATTERY_LEVEL", -1)

                if (device != null && batteryLevel in 0..100) {
                    val deviceAddress = device.address ?: ""
                    bluetoothRepo.onBatteryLevelReceived(deviceAddress, batteryLevel)
                    bluetoothRepo.activeDevice.value?.let { active ->
                        if (active.address.equals(deviceAddress, ignoreCase = true)) {
                            batteryRepo.recordSnapshotIfAppropriate(active)
                            WidgetUpdateHelper.updateAllWidgets(context, active)
                        }
                    }
                }
            }

            BluetoothAdapter.ACTION_STATE_CHANGED -> {
                bluetoothRepo.refreshReadings()
                WidgetUpdateHelper.updateAllWidgets(context, bluetoothRepo.activeDevice.value)
            }
        }
    }
}
