package dev.budsscope.app.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.budsscope.app.BudsScopeApplication
import dev.budsscope.app.data.local.entity.BatterySnapshotEntity
import dev.budsscope.app.data.model.BatteryTrendAnalysis
import dev.budsscope.app.data.model.BluetoothDeviceInfo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BatteryHistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as BudsScopeApplication
    val batteryRepository = app.batteryRepository
    val bluetoothRepository = app.bluetoothRepository

    val activeDevice: StateFlow<BluetoothDeviceInfo?> = bluetoothRepository.activeDevice
    val trendAnalysis: StateFlow<BatteryTrendAnalysis> = batteryRepository.trendAnalysis

    val historySnapshots: StateFlow<List<BatterySnapshotEntity>> = activeDevice
        .flatMapLatest { device ->
            if (device != null) {
                batteryRepository.getSnapshotsForDevice(device.address)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun clearHistory() {
        viewModelScope.launch {
            val address = activeDevice.value?.address
            if (address != null) {
                batteryRepository.clearHistoryForDevice(address)
            } else {
                batteryRepository.clearAllHistory()
            }
        }
    }
}
