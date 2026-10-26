package dev.budsscope.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "connection_events",
    indices = [
        Index(value = ["deviceAddress", "timestamp"])
    ]
)
data class ConnectionEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val deviceAddress: String,
    val deviceName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: String // "CONNECTED", "DISCONNECTED", "BONDED"
)
