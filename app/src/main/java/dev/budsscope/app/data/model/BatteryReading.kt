package dev.budsscope.app.data.model

/**
 * Represents the discrete state of a hardware battery reading.
 */
enum class BatteryReadingStatus {
    AVAILABLE,
    CHARGING,
    TEMPORARILY_UNAVAILABLE,
    STALE,
    UNSUPPORTED
}

/**
 * Encapsulates an individual battery reading for an earbud component or combined device.
 */
data class BatteryReading(
    val percentage: Int? = null,
    val status: BatteryReadingStatus = BatteryReadingStatus.TEMPORARILY_UNAVAILABLE,
    val timestampMs: Long = System.currentTimeMillis()
) {
    val isValid: Boolean
        get() = percentage != null && percentage in 0..100 &&
                (status == BatteryReadingStatus.AVAILABLE || status == BatteryReadingStatus.CHARGING)

    val isCharging: Boolean
        get() = status == BatteryReadingStatus.CHARGING

    companion object {
        fun unavailable(): BatteryReading =
            BatteryReading(percentage = null, status = BatteryReadingStatus.TEMPORARILY_UNAVAILABLE)

        fun unsupported(): BatteryReading =
            BatteryReading(percentage = null, status = BatteryReadingStatus.UNSUPPORTED)

        fun stale(percentage: Int?): BatteryReading =
            BatteryReading(percentage = percentage, status = BatteryReadingStatus.STALE)

        fun available(percentage: Int): BatteryReading =
            BatteryReading(percentage = percentage.coerceIn(0, 100), status = BatteryReadingStatus.AVAILABLE)

        fun charging(percentage: Int): BatteryReading =
            BatteryReading(percentage = percentage.coerceIn(0, 100), status = BatteryReadingStatus.CHARGING)
    }
}

/**
 * State representing left, right, and case readings.
 */
data class EarbudBatteryState(
    val left: BatteryReading = BatteryReading.unavailable(),
    val right: BatteryReading = BatteryReading.unavailable(),
    val case: BatteryReading = BatteryReading.unsupported(),
    val combined: BatteryReading = BatteryReading.unavailable(),
    val hasSeparateReadings: Boolean = false
) {
    /**
     * The primary reading to display if a single glanceable percentage is needed.
     */
    val primaryReading: BatteryReading
        get() = when {
            hasSeparateReadings && left.isValid && right.isValid ->
                BatteryReading.available((left.percentage!! + right.percentage!!) / 2)
            hasSeparateReadings && left.isValid -> left
            hasSeparateReadings && right.isValid -> right
            combined.isValid -> combined
            else -> BatteryReading.unavailable()
        }

    val isAnyCharging: Boolean
        get() = left.isCharging || right.isCharging || case.isCharging || combined.isCharging
}
