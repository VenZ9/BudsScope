package dev.budsscope.app.ui.settings

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.budsscope.app.BudsScopeApplication
import dev.budsscope.app.service.EarbudMonitorService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as BudsScopeApplication
    val preferencesRepository = app.preferencesRepository
    val bluetoothRepository = app.bluetoothRepository
    val batteryRepository = app.batteryRepository
    val mediaSessionRepository = app.mediaSessionRepository

    val lowBatteryAlertEnabled: StateFlow<Boolean> = preferencesRepository.lowBatteryAlertEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val lowBatteryThreshold: StateFlow<Int> = preferencesRepository.lowBatteryThreshold
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 20)

    val disconnectAlertEnabled: StateFlow<Boolean> = preferencesRepository.disconnectAlertEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val persistentServiceEnabled: StateFlow<Boolean> = preferencesRepository.persistentServiceEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val fluidWaveAnimationEnabled: StateFlow<Boolean> = preferencesRepository.fluidWaveAnimationEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun setLowBatteryAlert(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setLowBatteryAlert(enabled)
        }
    }

    fun setLowBatteryThreshold(threshold: Int) {
        viewModelScope.launch {
            preferencesRepository.setLowBatteryThreshold(threshold)
        }
    }

    fun setDisconnectAlert(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setDisconnectAlert(enabled)
        }
    }

    fun setPersistentService(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setPersistentService(enabled)
            val intent = Intent(app, EarbudMonitorService::class.java)
            if (enabled) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    app.startForegroundService(intent)
                } else {
                    app.startService(intent)
                }
            } else {
                app.stopService(intent)
            }
        }
    }

    fun setFluidWaveAnimation(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setFluidWaveAnimation(enabled)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            batteryRepository.clearAllHistory()
            app.database.connectionDao().clearAllEvents()
            preferencesRepository.clearSettings()
        }
    }

    // Diagnostics helper queries
    fun isBluetoothAdapterEnabled(): Boolean =
        bluetoothRepository.bluetoothAdapter?.isEnabled == true

    fun hasConnectPermission(): Boolean =
        bluetoothRepository.hasConnectPermission()

    fun hasScanPermission(): Boolean =
        bluetoothRepository.hasScanPermission()

    fun hasNotificationListenerAccess(): Boolean =
        mediaSessionRepository.isNotificationAccessGranted()
}
