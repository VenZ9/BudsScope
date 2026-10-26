package dev.budsscope.app.ui.radar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.budsscope.app.BudsScopeApplication
import dev.budsscope.app.data.model.BluetoothDeviceInfo
import dev.budsscope.app.domain.ConnectionStateTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReconnectRadarViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as BudsScopeApplication
    val bluetoothRepository = app.bluetoothRepository
    val connectionDao = app.database.connectionDao()

    val bondedDevices: StateFlow<List<BluetoothDeviceInfo>> = bluetoothRepository.bondedDevices
    val nearbyDevices: StateFlow<List<BluetoothDeviceInfo>> = bluetoothRepository.nearbyDevices
    val isScanning: StateFlow<Boolean> = bluetoothRepository.isScanning

    private val _lastSeenMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val lastSeenMap: StateFlow<Map<String, String>> = _lastSeenMap.asStateFlow()

    init {
        loadLastSeenTimes()
    }

    fun loadLastSeenTimes() {
        viewModelScope.launch {
            val map = mutableMapOf<String, String>()
            val bonded = bondedDevices.value
            for (device in bonded) {
                val lastTime = connectionDao.getLastConnectionTime(device.address)
                map[device.address] = ConnectionStateTracker.formatLastSeen(lastTime)
            }
            _lastSeenMap.value = map
        }
    }

    fun startScan() {
        bluetoothRepository.startScan()
    }

    fun stopScan() {
        bluetoothRepository.stopScan()
    }
}
