package dev.budsscope.app.domain

import dev.budsscope.app.data.model.DeviceCapabilities

object DeviceCapabilityResolver {

    /**
     * Resolves device capabilities based on observed profiles, battery endpoints, and reports.
     */
    fun resolve(
        deviceAddress: String,
        deviceName: String,
        hasSeparateEarbudReadings: Boolean = false,
        hasCaseReading: Boolean = false,
        hasCombinedReading: Boolean = false,
        isA2dpConnected: Boolean = false,
        isHeadsetConnected: Boolean = false,
        hasGattBatteryService: Boolean = false
    ): DeviceCapabilities {
        return DeviceCapabilities(
            deviceAddress = deviceAddress,
            deviceName = deviceName.ifBlank { "Unknown Earbuds" },
            reportsCombinedBattery = hasCombinedReading || (!hasSeparateEarbudReadings && hasGattBatteryService),
            reportsSeparateEarbuds = hasSeparateEarbudReadings,
            reportsCaseBattery = hasCaseReading,
            supportsA2dp = isA2dpConnected,
            supportsHeadset = isHeadsetConnected,
            supportsGattBattery = hasGattBatteryService
        )
    }

    /**
     * Determines the capability description string for UI and diagnostics.
     */
    fun getReportingTypeLabel(capabilities: DeviceCapabilities): String {
        return when {
            capabilities.reportsSeparateEarbuds && capabilities.reportsCaseBattery ->
                "Dual Channels + Case"
            capabilities.reportsSeparateEarbuds ->
                "Dual Channels (L / R)"
            capabilities.reportsCombinedBattery ->
                "Combined Audio Channel"
            capabilities.supportsGattBattery ->
                "Standard BLE Battery Service"
            else ->
                "Standard Audio Profile (No Battery Channel)"
        }
    }
}
