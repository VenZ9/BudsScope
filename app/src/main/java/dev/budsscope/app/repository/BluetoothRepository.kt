package dev.budsscope.app.repository

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import dev.budsscope.app.data.model.BatteryReading
import dev.budsscope.app.data.model.BluetoothDeviceInfo
import dev.budsscope.app.data.model.DeviceCapabilities
import dev.budsscope.app.data.model.EarbudBatteryState
import dev.budsscope.app.domain.BatteryValueValidator
import dev.budsscope.app.domain.DeviceCapabilityResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class BluetoothRepository(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private var a2dpProxy: BluetoothProfile? = null
    private var headsetProxy: BluetoothProfile? = null

    private val _activeDevice = MutableStateFlow<BluetoothDeviceInfo?>(null)
    val activeDevice: StateFlow<BluetoothDeviceInfo?> = _activeDevice.asStateFlow()

    private val _bondedDevices = MutableStateFlow<List<BluetoothDeviceInfo>>(emptyList())
    val bondedDevices: StateFlow<List<BluetoothDeviceInfo>> = _bondedDevices.asStateFlow()

    private val _nearbyDevices = MutableStateFlow<List<BluetoothDeviceInfo>>(emptyList())
    val nearbyDevices: StateFlow<List<BluetoothDeviceInfo>> = _nearbyDevices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private var scanJob: Job? = null

    // Standard Bluetooth SIG Battery Service UUIDs
    private val BATTERY_SERVICE_UUID = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb")
    private val BATTERY_LEVEL_CHAR_UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb")

    private val profileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            when (profile) {
                BluetoothProfile.A2DP -> a2dpProxy = proxy
                BluetoothProfile.HEADSET -> headsetProxy = proxy
            }
            refreshReadings()
        }

        override fun onServiceDisconnected(profile: Int) {
            when (profile) {
                BluetoothProfile.A2DP -> a2dpProxy = null
                BluetoothProfile.HEADSET -> headsetProxy = null
            }
            refreshReadings()
        }
    }

    init {
        initializeProfiles()
        refreshReadings()
    }

    private fun initializeProfiles() {
        bluetoothAdapter?.let { adapter ->
            try {
                adapter.getProfileProxy(context, profileListener, BluetoothProfile.A2DP)
                adapter.getProfileProxy(context, profileListener, BluetoothProfile.HEADSET)
            } catch (_: SecurityException) {
                // Permissions not yet granted
            }
        }
    }

    fun hasConnectPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun hasScanPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    @SuppressLint("MissingPermission")
    fun refreshReadings() {
        if (!hasConnectPermission()) {
            return
        }

        val adapter = bluetoothAdapter ?: return
        if (!adapter.isEnabled) {
            _activeDevice.value = null
            _bondedDevices.value = emptyList()
            return
        }

        // 1. Check for active connected A2DP / Headset devices
        val connectedDevices = mutableSetOf<BluetoothDevice>()

        a2dpProxy?.let { proxy ->
            try {
                connectedDevices.addAll(proxy.connectedDevices)
            } catch (_: SecurityException) {}
        }

        headsetProxy?.let { proxy ->
            try {
                connectedDevices.addAll(proxy.connectedDevices)
            } catch (_: SecurityException) {}
        }

        // 2. Query bonded devices
        val bonded = try {
            adapter.bondedDevices ?: emptySet()
        } catch (_: SecurityException) {
            emptySet()
        }

        val bondedList = bonded.map { device ->
            val isConnected = connectedDevices.contains(device)
            buildDeviceInfo(device, isConnected)
        }
        _bondedDevices.value = bondedList

        // 3. Find primary active device
        val primaryDevice = connectedDevices.firstOrNull()
        if (primaryDevice != null) {
            val info = buildDeviceInfo(primaryDevice, isConnected = true)
            _activeDevice.value = info

            // If battery is still unavailable, attempt GATT Battery read
            if (!info.batteryState.primaryReading.isValid) {
                attemptGattBatteryRead(primaryDevice)
            }
        } else {
            _activeDevice.value = null
        }
    }

    @SuppressLint("MissingPermission")
    private fun buildDeviceInfo(device: BluetoothDevice, isConnected: Boolean): BluetoothDeviceInfo {
        val name = try {
            device.name ?: device.alias ?: "Bluetooth Device"
        } catch (_: SecurityException) {
            "Bluetooth Device"
        }
        val address = device.address ?: "00:00:00:00:00:00"

        val profiles = mutableListOf<String>()
        var isA2dp = false
        var isHeadset = false

        if (isConnected) {
            try {
                if (a2dpProxy?.getConnectionState(device) == BluetoothProfile.STATE_CONNECTED) {
                    profiles.add("A2DP Audio")
                    isA2dp = true
                }
                if (headsetProxy?.getConnectionState(device) == BluetoothProfile.STATE_CONNECTED) {
                    profiles.add("HFP / Headset")
                    isHeadset = true
                }
            } catch (_: SecurityException) {}
        }

        // Read battery level via reflection getBatteryLevel() if supported
        val reflectionBattery = queryReflectionBatteryLevel(device)
        val validatedBattery = BatteryValueValidator.validate(reflectionBattery)

        val batteryState = if (validatedBattery.isValid) {
            EarbudBatteryState(
                combined = validatedBattery,
                hasSeparateReadings = false
            )
        } else {
            // Keep previous reading if available and fresh
            val existing = _activeDevice.value?.takeIf { it.address == address }?.batteryState
            existing ?: EarbudBatteryState(
                combined = BatteryReading.unavailable(),
                hasSeparateReadings = false
            )
        }

        val capabilities = DeviceCapabilityResolver.resolve(
            deviceAddress = address,
            deviceName = name,
            hasSeparateEarbudReadings = batteryState.hasSeparateReadings,
            hasCaseReading = batteryState.case.isValid,
            hasCombinedReading = batteryState.combined.isValid,
            isA2dpConnected = isA2dp,
            isHeadsetConnected = isHeadset
        )

        return BluetoothDeviceInfo(
            name = name,
            address = address,
            isConnected = isConnected,
            lastConnectedEpochMs = if (isConnected) System.currentTimeMillis() else null,
            profiles = profiles,
            batteryState = batteryState,
            capabilities = capabilities,
            lastUpdatedEpochMs = System.currentTimeMillis()
        )
    }

    /**
     * Reads battery level using Android's hidden getBatteryLevel() method via reflection safely.
     */
    private fun queryReflectionBatteryLevel(device: BluetoothDevice): Int? {
        return try {
            val method = device.javaClass.getMethod("getBatteryLevel")
            val result = method.invoke(device) as? Int
            if (result != null && result in 0..100) result else null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Updates battery reading when an external broadcast or service provides new values.
     */
    fun onBatteryLevelReceived(
        deviceAddress: String,
        batteryPercent: Int,
        isCharging: Boolean = false
    ) {
        val current = _activeDevice.value
        if (current != null && current.address.equals(deviceAddress, ignoreCase = true)) {
            val reading = BatteryValueValidator.validate(batteryPercent, isCharging)
            val updatedBattery = current.batteryState.copy(
                combined = reading,
                hasSeparateReadings = false
            )
            val updatedCapabilities = current.capabilities.copy(reportsCombinedBattery = true)
            _activeDevice.value = current.copy(
                batteryState = updatedBattery,
                capabilities = updatedCapabilities,
                lastUpdatedEpochMs = System.currentTimeMillis()
            )
        }
    }

    /**
     * Updates dual channel battery readings when manufacturer-specific or GATT reports L/R/Case.
     */
    fun onDualChannelBatteryReceived(
        deviceAddress: String,
        leftPercent: Int?,
        rightPercent: Int?,
        casePercent: Int?,
        isCharging: Boolean = false
    ) {
        val current = _activeDevice.value
        if (current != null && current.address.equals(deviceAddress, ignoreCase = true)) {
            val left = BatteryValueValidator.validate(leftPercent, isCharging)
            val right = BatteryValueValidator.validate(rightPercent, isCharging)
            val case = BatteryValueValidator.validate(casePercent, isCharging)

            val updatedBattery = EarbudBatteryState(
                left = left,
                right = right,
                case = case,
                hasSeparateReadings = left.isValid || right.isValid
            )
            val updatedCapabilities = current.capabilities.copy(
                reportsSeparateEarbuds = left.isValid || right.isValid,
                reportsCaseBattery = case.isValid
            )

            _activeDevice.value = current.copy(
                batteryState = updatedBattery,
                capabilities = updatedCapabilities,
                lastUpdatedEpochMs = System.currentTimeMillis()
            )
        }
    }

    /**
     * Connects to GATT Battery Service (0x180F) to read standard BLE battery level.
     */
    @SuppressLint("MissingPermission")
    private fun attemptGattBatteryRead(device: BluetoothDevice) {
        if (!hasConnectPermission()) return

        try {
            var gattRef: BluetoothGatt? = null
            val callback = object : BluetoothGattCallback() {
                override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        try {
                            gatt.discoverServices()
                        } catch (_: SecurityException) {
                            gatt.close()
                        }
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        gatt.close()
                    }
                }

                override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        val service = gatt.getService(BATTERY_SERVICE_UUID)
                        val characteristic = service?.getCharacteristic(BATTERY_LEVEL_CHAR_UUID)
                        if (characteristic != null) {
                            try {
                                gatt.readCharacteristic(characteristic)
                            } catch (_: SecurityException) {
                                gatt.close()
                            }
                        } else {
                            gatt.close()
                        }
                    } else {
                        gatt.close()
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onCharacteristicRead(
                    gatt: BluetoothGatt,
                    characteristic: BluetoothGattCharacteristic,
                    status: Int
                ) {
                    if (status == BluetoothGatt.GATT_SUCCESS && characteristic.uuid == BATTERY_LEVEL_CHAR_UUID) {
                        val level = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0)
                        if (level != null && level in 0..100) {
                            scope.launch(Dispatchers.Main) {
                                onBatteryLevelReceived(device.address, level)
                            }
                        }
                    }
                    gatt.close()
                }
            }

            gattRef = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                device.connectGatt(context, false, callback, BluetoothDevice.TRANSPORT_LE)
            } else {
                device.connectGatt(context, false, callback)
            }
        } catch (_: Exception) {
            // GATT connection may fail or not be supported on classic-only headphones
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        if (!hasScanPermission() || _isScanning.value) return
        val adapter = bluetoothAdapter ?: return
        if (!adapter.isEnabled) return

        _nearbyDevices.value = emptyList()
        _isScanning.value = true

        val scanner = adapter.bluetoothLeScanner
        if (scanner != null) {
            val scanCallback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult?) {
                    val device = result?.device ?: return
                    val name = try {
                        device.name ?: result.scanRecord?.deviceName
                    } catch (_: SecurityException) {
                        null
                    }
                    if (!name.isNullOrBlank()) {
                        val currentList = _nearbyDevices.value.toMutableList()
                        if (currentList.none { it.address.equals(device.address, ignoreCase = true) }) {
                            currentList.add(
                                BluetoothDeviceInfo(
                                    name = name,
                                    address = device.address,
                                    isConnected = false,
                                    batteryState = EarbudBatteryState(combined = BatteryReading.unavailable())
                                )
                            )
                            _nearbyDevices.value = currentList
                        }
                    }
                }

                override fun onScanFailed(errorCode: Int) {
                    _isScanning.value = false
                }
            }

            try {
                scanner.startScan(scanCallback)
            } catch (_: SecurityException) {
                _isScanning.value = false
                return
            }

            scanJob = scope.launch {
                delay(12_000L) // 12 seconds scan window
                try {
                    scanner.stopScan(scanCallback)
                } catch (_: Exception) {}
                _isScanning.value = false
            }
        } else {
            // Fallback classic discovery
            try {
                adapter.startDiscovery()
            } catch (_: SecurityException) {}

            scanJob = scope.launch {
                delay(12_000L)
                try {
                    adapter.cancelDiscovery()
                } catch (_: Exception) {}
                _isScanning.value = false
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        scanJob?.cancel()
        _isScanning.value = false
        try {
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(object : ScanCallback() {})
            bluetoothAdapter?.cancelDiscovery()
        } catch (_: Exception) {}
    }
}
