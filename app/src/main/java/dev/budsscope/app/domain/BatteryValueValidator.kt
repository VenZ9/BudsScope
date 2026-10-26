package dev.budsscope.app.domain

import dev.budsscope.app.data.model.BatteryReading
import dev.budsscope.app.data.model.BatteryReadingStatus

object BatteryValueValidator {

    /**
     * Standard Bluetooth sentinel values representing missing, unknown, or error states.
     */
    private val INVALID_SENTINEL_VALUES = setOf(-1, 127, 255)

    /**
     * Stale threshold in milliseconds (default 15 minutes).
     */
    const val DEFAULT_STALENESS_THRESHOLD_MS = 15 * 60 * 1000L

    /**
     * Validates a raw battery percentage reading from Android APIs or Bluetooth intents.
     */
    fun validate(
        rawPercentage: Int?,
        isCharging: Boolean = false,
        timestampMs: Long = System.currentTimeMillis(),
        nowMs: Long = System.currentTimeMillis(),
        stalenessThresholdMs: Long = DEFAULT_STALENESS_THRESHOLD_MS
    ): BatteryReading {
        if (rawPercentage == null || rawPercentage in INVALID_SENTINEL_VALUES) {
            return BatteryReading.unavailable()
        }

        if (rawPercentage !in 0..100) {
            return BatteryReading.unavailable()
        }

        val age = (nowMs - timestampMs).coerceAtLeast(0L)
        val isStale = age > stalenessThresholdMs

        val status = when {
            isCharging -> BatteryReadingStatus.CHARGING
            isStale -> BatteryReadingStatus.STALE
            else -> BatteryReadingStatus.AVAILABLE
        }

        return BatteryReading(
            percentage = rawPercentage,
            status = status,
            timestampMs = timestampMs
        )
    }

    /**
     * Validates whether a battery reading has dropped into the low-battery alert zone.
     */
    fun isLowBattery(
        reading: BatteryReading,
        thresholdPercent: Int
    ): Boolean {
        return reading.isValid && !reading.isCharging && (reading.percentage ?: 100) <= thresholdPercent
    }
}
