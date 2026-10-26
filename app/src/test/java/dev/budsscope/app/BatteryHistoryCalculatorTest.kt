package dev.budsscope.app

import dev.budsscope.app.data.local.entity.BatterySnapshotEntity
import dev.budsscope.app.domain.BatteryHistoryCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BatteryHistoryCalculatorTest {

    @Test
    fun `computeTrend returns insufficient data when empty or fewer than 3 samples`() {
        val emptyAnalysis = BatteryHistoryCalculator.computeTrend(emptyList(), 80)
        assertFalse(emptyAnalysis.hasSufficientData)

        val oneSample = listOf(
            BatterySnapshotEntity(
                deviceAddress = "AA:BB",
                deviceName = "Buds",
                timestamp = 1000L,
                batteryCombined = 90
            )
        )
        val oneAnalysis = BatteryHistoryCalculator.computeTrend(oneSample, 90)
        assertFalse(oneAnalysis.hasSufficientData)
    }

    @Test
    fun `computeTrend returns insufficient when observation under 5 minutes`() {
        val start = 1_000_000L
        val snapshots = listOf(
            BatterySnapshotEntity(deviceAddress = "A", deviceName = "B", timestamp = start, batteryCombined = 100),
            BatterySnapshotEntity(deviceAddress = "A", deviceName = "B", timestamp = start + 60_000L, batteryCombined = 99),
            BatterySnapshotEntity(deviceAddress = "A", deviceName = "B", timestamp = start + 120_000L, batteryCombined = 98)
        ) // Only 2 minutes total

        val analysis = BatteryHistoryCalculator.computeTrend(snapshots, 98)
        assertFalse(analysis.hasSufficientData)
        assertTrue(analysis.summaryBasis.contains("under the 5-minute threshold"))
    }

    @Test
    fun `computeTrend calculates accurate drain rate and remaining listening hours`() {
        val start = 1_000_000L
        val oneHourMs = 3600_000L

        // 10% drop over 1 hour -> 10.0%/hr drain
        val snapshots = listOf(
            BatterySnapshotEntity(deviceAddress = "A", deviceName = "B", timestamp = start, batteryCombined = 90),
            BatterySnapshotEntity(deviceAddress = "A", deviceName = "B", timestamp = start + (oneHourMs / 2), batteryCombined = 85),
            BatterySnapshotEntity(deviceAddress = "A", deviceName = "B", timestamp = start + oneHourMs, batteryCombined = 80)
        )

        val analysis = BatteryHistoryCalculator.computeTrend(snapshots, currentBatteryLevel = 80)
        assertTrue(analysis.hasSufficientData)
        assertNotNull(analysis.drainRatePerHour)
        assertEquals(10.0f, analysis.drainRatePerHour!!, 0.1f)

        // 80% remaining / 10%/hr = 8.0 hours remaining
        assertNotNull(analysis.estimatedRemainingHours)
        assertEquals(8.0f, analysis.estimatedRemainingHours!!, 0.1f)
    }

    @Test
    fun `computeTrend ignores charging snapshots for drain calculation`() {
        val start = 1_000_000L
        val halfHourMs = 1800_000L

        val snapshots = listOf(
            BatterySnapshotEntity(deviceAddress = "A", deviceName = "B", timestamp = start, batteryCombined = 90, isCharging = false),
            // Charging anomaly point should be filtered out
            BatterySnapshotEntity(deviceAddress = "A", deviceName = "B", timestamp = start + 300_000L, batteryCombined = 95, isCharging = true),
            BatterySnapshotEntity(deviceAddress = "A", deviceName = "B", timestamp = start + 600_000L, batteryCombined = 88, isCharging = false),
            BatterySnapshotEntity(deviceAddress = "A", deviceName = "B", timestamp = start + halfHourMs, batteryCombined = 85, isCharging = false)
        )

        val analysis = BatteryHistoryCalculator.computeTrend(snapshots, currentBatteryLevel = 85)
        assertTrue(analysis.hasSufficientData)
        // 5% drop over 0.5 hour = 10.0%/hr
        assertEquals(10.0f, analysis.drainRatePerHour!!, 0.1f)
    }

    @Test
    fun `shouldTriggerLowBatteryAlert prevents spamming duplicate notifications`() {
        val threshold = 20

        // Normal level: no alert
        assertFalse(BatteryHistoryCalculator.shouldTriggerLowBatteryAlert(80, false, threshold, null))

        // Charging level: no alert even if low
        assertFalse(BatteryHistoryCalculator.shouldTriggerLowBatteryAlert(15, true, threshold, null))

        // First drop to 15%: alert!
        assertTrue(BatteryHistoryCalculator.shouldTriggerLowBatteryAlert(15, false, threshold, null))

        // Same 15% later: do not re-alert (prevent spam)
        assertFalse(BatteryHistoryCalculator.shouldTriggerLowBatteryAlert(15, false, threshold, 15))

        // Further drop to 10%: alert again!
        assertTrue(BatteryHistoryCalculator.shouldTriggerLowBatteryAlert(10, false, threshold, 15))
    }
}
