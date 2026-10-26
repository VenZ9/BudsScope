package dev.budsscope.app.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import dev.budsscope.app.BudsScopeApplication
import dev.budsscope.app.data.model.BluetoothDeviceInfo
import kotlinx.coroutines.flow.StateFlow

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as BudsScopeApplication
    val bluetoothRepository = app.bluetoothRepository

    val activeDevice: StateFlow<BluetoothDeviceInfo?> = bluetoothRepository.activeDevice

    fun hasConnectPermission(): Boolean = bluetoothRepository.hasConnectPermission()

    fun refresh() {
        bluetoothRepository.refreshReadings()
    }
}
