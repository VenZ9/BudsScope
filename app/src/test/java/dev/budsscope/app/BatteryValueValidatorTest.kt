package dev.budsscope.app

import dev.budsscope.app.data.model.BatteryReadingStatus
import dev.budsscope.app.domain.BatteryValueValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BatteryValueValidatorTest {

    @Test
    fun `validate normal valid percentage returns available`() {
        val reading = BatteryValueValidator.validate(85)
        assertTrue(reading.isValid)
        assertEquals(85, reading.percentage)
        assertEquals(BatteryReadingStatus.AVAILABLE, reading.status)
        assertFalse(reading.isCharging)
    }

    @Test
    fun `validate boundary values 0 and 100`() {
        val min = BatteryValueValidator.validate(0)
        assertTrue(min.isValid)
        assertEquals(0, min.percentage)

        val max = BatteryValueValidator.validate(100)
        assertTrue(max.isValid)
        assertEquals(100, max.percentage)
    }

    @Test
    fun `validate sentinel values like -1, 127, 255 return unavailable`() {
        val minusOne = BatteryValueValidator.validate(-1)
        assertFalse(minusOne.isValid)
        assertNull(minusOne.percentage)
        assertEquals(BatteryReadingStatus.TEMPORARILY_UNAVAILABLE, minusOne.status)

        val sentinel127 = BatteryValueValidator.validate(127)
        assertFalse(sentinel127.isValid)

        val sentinel255 = BatteryValueValidator.validate(255)
        assertFalse(sentinel255.isValid)
    }

    @Test
    fun `validate out-of-range negative and greater than 100 return unavailable`() {
        val negative = BatteryValueValidator.validate(-20)
        assertFalse(negative.isValid)

        val over100 = BatteryValueValidator.validate(150)
        assertFalse(over100.isValid)
    }

    @Test
    fun `validate null raw value returns unavailable`() {
        val reading = BatteryValueValidator.validate(null)
        assertFalse(reading.isValid)
        assertNull(reading.percentage)
        assertEquals(BatteryReadingStatus.TEMPORARILY_UNAVAILABLE, reading.status)
    }

    @Test
    fun `validate charging flag produces charging status`() {
        val reading = BatteryValueValidator.validate(50, isCharging = true)
        assertTrue(reading.isValid)
        assertTrue(reading.isCharging)
        assertEquals(BatteryReadingStatus.CHARGING, reading.status)
    }

    @Test
    fun `validate reading older than threshold marked as stale`() {
        val now = 1_000_000L
        val oldTimestamp = now - (20 * 60 * 1000L) // 20 minutes ago
        val threshold = 15 * 60 * 1000L // 15 minutes threshold

        val reading = BatteryValueValidator.validate(
            rawPercentage = 60,
            isCharging = false,
            timestampMs = oldTimestamp,
            nowMs = now,
            stalenessThresholdMs = threshold
        )

        assertEquals(60, reading.percentage)
        assertEquals(BatteryReadingStatus.STALE, reading.status)
    }

    @Test
    fun `isLowBattery correctly checks threshold`() {
        val lowReading = BatteryValueValidator.validate(15)
        val normalReading = BatteryValueValidator.validate(50)
        val chargingLow = BatteryValueValidator.validate(10, isCharging = true)

        assertTrue(BatteryValueValidator.isLowBattery(lowReading, 20))
        assertFalse(BatteryValueValidator.isLowBattery(normalReading, 20))
        // Charging device should not trigger low battery alarm
        assertFalse(BatteryValueValidator.isLowBattery(chargingLow, 20))
    }
}
