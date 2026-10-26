package dev.budsscope.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.budsscope.app.data.local.entity.BatterySnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BatteryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnapshot(snapshot: BatterySnapshotEntity): Long

    @Query("SELECT * FROM battery_snapshots WHERE deviceAddress = :deviceAddress ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentSnapshotsFlow(deviceAddress: String, limit: Int = 100): Flow<List<BatterySnapshotEntity>>

    @Query("SELECT * FROM battery_snapshots WHERE deviceAddress = :deviceAddress ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentSnapshots(deviceAddress: String, limit: Int = 100): List<BatterySnapshotEntity>

    @Query("SELECT * FROM battery_snapshots WHERE deviceAddress = :deviceAddress AND timestamp >= :sinceTimestamp ORDER BY timestamp ASC")
    fun getSnapshotsSinceFlow(deviceAddress: String, sinceTimestamp: Long): Flow<List<BatterySnapshotEntity>>

    @Query("SELECT * FROM battery_snapshots WHERE deviceAddress = :deviceAddress AND timestamp >= :sinceTimestamp ORDER BY timestamp ASC")
    suspend fun getSnapshotsSince(deviceAddress: String, sinceTimestamp: Long): List<BatterySnapshotEntity>

    @Query("SELECT * FROM battery_snapshots WHERE deviceAddress = :deviceAddress ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestSnapshot(deviceAddress: String): BatterySnapshotEntity?

    @Query("DELETE FROM battery_snapshots WHERE deviceAddress = :deviceAddress")
    suspend fun clearSnapshotsForDevice(deviceAddress: String)

    @Query("DELETE FROM battery_snapshots")
    suspend fun clearAllSnapshots()
}
