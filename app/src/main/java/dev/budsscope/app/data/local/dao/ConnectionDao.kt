package dev.budsscope.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.budsscope.app.data.local.entity.ConnectionEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConnectionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: ConnectionEventEntity): Long

    @Query("SELECT * FROM connection_events ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentEventsFlow(limit: Int = 50): Flow<List<ConnectionEventEntity>>

    @Query("SELECT * FROM connection_events WHERE deviceAddress = :deviceAddress ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getEventsForDevice(deviceAddress: String, limit: Int = 50): List<ConnectionEventEntity>

    @Query("SELECT timestamp FROM connection_events WHERE deviceAddress = :deviceAddress AND eventType = 'CONNECTED' ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastConnectionTime(deviceAddress: String): Long?

    @Query("DELETE FROM connection_events")
    suspend fun clearAllEvents()
}
