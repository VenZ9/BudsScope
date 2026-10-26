package dev.budsscope.app.domain

import dev.budsscope.app.data.local.entity.BatterySnapshotEntity
import dev.budsscope.app.data.model.BatteryTrendAnalysis
import kotlin.math.roundToInt

object BatteryHistoryCalculator {

    /**
     * Minimum observation duration (5 minutes) before computing drain rate.
     */
    const val MIN_OBSERVATION_DURATION_MS = 5 * 60 * 1000L

    /**
     * Minimum number of non-charging data snapshots required to compute a drain trend.
     */
    const val MIN_DATA_POINTS = 3

    /**
     * Computes the drain trend analysis from a chronological sequence of snapshots.
     */
    fun computeTrend(
        snapshotsAscending: List<BatterySnapshotEntity>,
        currentBatteryLevel: Int?
    ): BatteryTrendAnalysis {
        if (snapshotsAscending.isEmpty()) {
            return BatteryTrendAnalysis(
                dataPointCount = 0,
                observationPeriodMinutes = 0L,
                summaryBasis = "No historical snapshots recorded yet.",
                hasSufficientData = false
            )
        }

        // Filter for non-charging snapshots with valid primary percentages
        val dischargePoints = snapshotsAscending.filter { !it.isCharging && it.primaryPercentage != null }

        val first = dischargePoints.firstOrNull()
        val last = dischargePoints.lastOrNull()

        if (first == null || last == null || dischargePoints.size < MIN_DATA_POINTS) {
            return BatteryTrendAnalysis(
                dataPointCount = dischargePoints.size,
                observationPeriodMinutes = 0L,
                summaryBasis = "Need at least $MIN_DATA_POINTS discharge samples to determine trend.",
                hasSufficientData = false
            )
        }

        val durationMs = last.timestamp - first.timestamp
        val durationMinutes = durationMs / (60 * 1000L)

        if (durationMs < MIN_OBSERVATION_DURATION_MS) {
            return BatteryTrendAnalysis(
                dataPointCount = dischargePoints.size,
                observationPeriodMinutes = durationMinutes,
                summaryBasis = "Observation time ($durationMinutes min) is under the 5-minute threshold.",
                hasSufficientData = false
            )
        }

        val startPercent = first.primaryPercentage!!
        val endPercent = last.primaryPercentage!!
        val deltaPercent = startPercent - endPercent

        if (deltaPercent <= 0) {
            // No drain observed yet during this window (battery held steady)
            return BatteryTrendAnalysis(
                drainRatePerHour = 0.0f,
                estimatedRemainingHours = null,
                dataPointCount = dischargePoints.size,
                observationPeriodMinutes = durationMinutes,
                summaryBasis = "Battery held steady at $endPercent% over $durationMinutes minutes.",
                hasSufficientData = true
            )
        }

        val hoursObserved = durationMs.toFloat() / (1000f * 60f * 60f)
        val drainRatePerHour = deltaPercent.toFloat() / hoursObserved

        val remainingHours = if (currentBatteryLevel != null && currentBatteryLevel > 0 && drainRatePerHour > 0.1f) {
            (currentBatteryLevel.toFloat() / drainRatePerHour).coerceAtMost(48.0f)
        } else {
            null
        }

        val basis = "Observed ${deltaPercent}% drain across $durationMinutes min (${dischargePoints.size} samples)"

        return BatteryTrendAnalysis(
            drainRatePerHour = (drainRatePerHour * 10).roundToInt() / 10f,
            estimatedRemainingHours = remainingHours?.let { (it * 10).roundToInt() / 10f },
            dataPointCount = dischargePoints.size,
            observationPeriodMinutes = durationMinutes,
            summaryBasis = basis,
            hasSufficientData = true
        )
    }

    /**
     * Determines if a low-battery notification should be triggered.
     * Prevents repeated spam if the reading remains low.
     */
    fun shouldTriggerLowBatteryAlert(
        currentLevel: Int?,
        isCharging: Boolean,
        thresholdPercent: Int,
        lastAlertLevel: Int?
    ): Boolean {
        if (currentLevel == null || isCharging) return false
        if (currentLevel > thresholdPercent) return false

        // Only alert if we haven't alerted for this exact level or lower already
        return lastAlertLevel == null || currentLevel < lastAlertLevel
    }
}
