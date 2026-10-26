package dev.budsscope.app

import dev.budsscope.app.domain.DeviceCapabilityResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceCapabilityResolverTest {

    @Test
    fun `resolve dual channels and case reports properly`() {
        val caps = DeviceCapabilityResolver.resolve(
            deviceAddress = "AA:BB:CC:DD:EE:FF",
            deviceName = "AirPro TWS",
            hasSeparateEarbudReadings = true,
            hasCaseReading = true,
            hasCombinedReading = false,
            isA2dpConnected = true,
            isHeadsetConnected = true
        )

        assertTrue(caps.reportsSeparateEarbuds)
        assertTrue(caps.reportsCaseBattery)
        assertFalse(caps.reportsCombinedBattery)
        assertTrue(caps.supportsA2dp)
        assertTrue(caps.supportsHeadset)
        assertTrue(caps.exposesAnyBattery)
        assertEquals("Dual Channels + Case", DeviceCapabilityResolver.getReportingTypeLabel(caps))
    }

    @Test
    fun `resolve combined single channel audio profile`() {
        val caps = DeviceCapabilityResolver.resolve(
            deviceAddress = "11:22:33:44:55:66",
            deviceName = "Classic Buds",
            hasSeparateEarbudReadings = false,
            hasCaseReading = false,
            hasCombinedReading = true,
            isA2dpConnected = true
        )

        assertFalse(caps.reportsSeparateEarbuds)
        assertFalse(caps.reportsCaseBattery)
        assertTrue(caps.reportsCombinedBattery)
        assertEquals("Combined Audio Channel", DeviceCapabilityResolver.getReportingTypeLabel(caps))
    }

    @Test
    fun `resolve device with no battery exposure`() {
        val caps = DeviceCapabilityResolver.resolve(
            deviceAddress = "99:88:77:66:55:44",
            deviceName = "Vintage Bluetooth",
            hasSeparateEarbudReadings = false,
            hasCaseReading = false,
            hasCombinedReading = false
        )

        assertFalse(caps.exposesAnyBattery)
        assertEquals("Standard Audio Profile (No Battery Channel)", DeviceCapabilityResolver.getReportingTypeLabel(caps))
    }

    @Test
    fun `resolve device with blank name defaults gracefully`() {
        val caps = DeviceCapabilityResolver.resolve(
            deviceAddress = "00:11:22:33:44:55",
            deviceName = "   "
        )

        assertEquals("Unknown Earbuds", caps.deviceName)
    }
}
