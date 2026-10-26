package dev.budsscope.app.data.model

/**
 * Model capturing what capabilities a connected Bluetooth device actually exposes.
 */
data class DeviceCapabilities(
    val deviceAddress: String,
    val deviceName: String,
    val reportsCombinedBattery: Boolean = false,
    val reportsSeparateEarbuds: Boolean = false,
    val reportsCaseBattery: Boolean = false,
    val supportsA2dp: Boolean = false,
    val supportsHeadset: Boolean = false,
    val supportsGattBattery: Boolean = false
) {
    val exposesAnyBattery: Boolean
        get() = reportsCombinedBattery || reportsSeparateEarbuds || supportsGattBattery
}

/**
 * High-level state of a Bluetooth device.
 */
data class BluetoothDeviceInfo(
    val name: String,
    val address: String,
    val isConnected: Boolean,
    val lastConnectedEpochMs: Long? = null,
    val profiles: List<String> = emptyList(),
    val batteryState: EarbudBatteryState = EarbudBatteryState(),
    val capabilities: DeviceCapabilities = DeviceCapabilities(deviceAddress = address, deviceName = name),
    val lastUpdatedEpochMs: Long = System.currentTimeMillis()
)
