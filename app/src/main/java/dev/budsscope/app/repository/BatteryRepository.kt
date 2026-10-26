package dev.budsscope.app.repository

import dev.budsscope.app.data.local.dao.BatteryDao
import dev.budsscope.app.data.local.entity.BatterySnapshotEntity
import dev.budsscope.app.data.model.BatteryTrendAnalysis
import dev.budsscope.app.data.model.BluetoothDeviceInfo
import dev.budsscope.app.data.preferences.UserPreferencesRepository
import dev.budsscope.app.domain.BatteryHistoryCalculator
import dev.budsscope.app.service.ConnectionNotificationController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BatteryRepository(
    private val batteryDao: BatteryDao,
    private val preferencesRepository: UserPreferencesRepository,
    private val notificationController: ConnectionNotificationController,
    private val scope: CoroutineScope
) {
    private val _trendAnalysis = MutableStateFlow(BatteryTrendAnalysis())
    val trendAnalysis: StateFlow<BatteryTrendAnalysis> = _trendAnalysis.asStateFlow()

    private var lastAlertedBatteryLevel: Int? = null

    companion object {
        const val MIN_PERSIST_INTERVAL_MS = 10 * 60 * 1000L // 10 minutes
    }

    fun getSnapshotsForDevice(deviceAddress: String): Flow<List<BatterySnapshotEntity>> {
        return batteryDao.getRecentSnapshotsFlow(deviceAddress, limit = 150)
    }

    /**
     * Records a timestamped battery reading if meaningful or interval elapsed.
     */
    fun recordSnapshotIfAppropriate(device: BluetoothDeviceInfo) {
        val battery = device.batteryState
        val primary = battery.primaryReading
        if (!primary.isValid) return

        scope.launch(Dispatchers.IO) {
            val last = batteryDao.getLatestSnapshot(device.address)
            val now = System.currentTimeMillis()

            val shouldPersist = when {
                last == null -> true
                last.primaryPercentage != primary.percentage -> true
                last.isCharging != primary.isCharging -> true
                (now - last.timestamp) >= MIN_PERSIST_INTERVAL_MS -> true
                else -> false
            }

            if (shouldPersist) {
                val entity = BatterySnapshotEntity(
                    deviceAddress = device.address,
                    deviceName = device.name,
                    timestamp = now,
                    batteryLeft = if (battery.left.isValid) battery.left.percentage else null,
                    batteryRight = if (battery.right.isValid) battery.right.percentage else null,
                    batteryCase = if (battery.case.isValid) battery.case.percentage else null,
                    batteryCombined = if (battery.combined.isValid) battery.combined.percentage else null,
                    isCharging = primary.isCharging
                )
                batteryDao.insertSnapshot(entity)
            }

            // Check low-battery alert threshold
            checkLowBatteryThreshold(device.name, primary.percentage, primary.isCharging)

            // Recompute trend analysis
            updateTrendAnalysis(device.address, primary.percentage)
        }
    }

    private suspend fun checkLowBatteryThreshold(
        deviceName: String,
        currentPercent: Int?,
        isCharging: Boolean
    ) {
        val alertsEnabled = preferencesRepository.lowBatteryAlertEnabled.first()
        if (!alertsEnabled) return

        val threshold = preferencesRepository.lowBatteryThreshold.first()

        val shouldAlert = BatteryHistoryCalculator.shouldTriggerLowBatteryAlert(
            currentLevel = currentPercent,
            isCharging = isCharging,
            thresholdPercent = threshold,
            lastAlertLevel = lastAlertedBatteryLevel
        )

        if (shouldAlert && currentPercent != null) {
            lastAlertedBatteryLevel = currentPercent
            notificationController.notifyLowBattery(deviceName, currentPercent)
        } else if (currentPercent != null && currentPercent > threshold) {
            // Reset alert memory once charged above threshold
            lastAlertedBatteryLevel = null
        }
    }

    suspend fun updateTrendAnalysis(deviceAddress: String, currentPercent: Int?) {
        val snapshots = batteryDao.getRecentSnapshots(deviceAddress, limit = 50).reversed()
        val analysis = BatteryHistoryCalculator.computeTrend(snapshots, currentPercent)
        withContext(Dispatchers.Main) {
            _trendAnalysis.value = analysis
        }
    }

    suspend fun clearHistoryForDevice(deviceAddress: String) {
        withContext(Dispatchers.IO) {
            batteryDao.clearSnapshotsForDevice(deviceAddress)
            _trendAnalysis.value = BatteryTrendAnalysis()
        }
    }

    suspend fun clearAllHistory() {
        withContext(Dispatchers.IO) {
            batteryDao.clearAllSnapshots()
            _trendAnalysis.value = BatteryTrendAnalysis()
        }
    }
}
