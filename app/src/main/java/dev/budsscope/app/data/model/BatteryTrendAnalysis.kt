package dev.budsscope.app.data.model

/**
 * Observed battery drain metrics computed from real timestamped snapshots.
 */
data class BatteryTrendAnalysis(
    val drainRatePerHour: Float? = null,
    val estimatedRemainingHours: Float? = null,
    val dataPointCount: Int = 0,
    val observationPeriodMinutes: Long = 0L,
    val isCharging: Boolean = false,
    val summaryBasis: String = "",
    val hasSufficientData: Boolean = false
) {
    val formattedDrainRate: String
        get() = drainRatePerHour?.let { "%.1f%%/hr".format(it) } ?: "Collecting data"

    val formattedRemainingHours: String
        get() = estimatedRemainingHours?.let {
            val totalMinutes = (it * 60).toInt()
            val hours = totalMinutes / 60
            val mins = totalMinutes % 60
            if (hours > 0) "~%dh %dm".format(hours, mins) else "~%dm".format(mins)
        } ?: "Calculating…"
}
