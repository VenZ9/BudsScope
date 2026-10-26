package dev.budsscope.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "battery_snapshots",
    indices = [
        Index(value = ["deviceAddress", "timestamp"])
    ]
)
data class BatterySnapshotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val deviceAddress: String,
    val deviceName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val batteryLeft: Int? = null,
    val batteryRight: Int? = null,
    val batteryCase: Int? = null,
    val batteryCombined: Int? = null,
    val isCharging: Boolean = false
) {
    val primaryPercentage: Int?
        get() = when {
            batteryLeft != null && batteryRight != null -> (batteryLeft + batteryRight) / 2
            batteryLeft != null -> batteryLeft
            batteryRight != null -> batteryRight
            batteryCombined != null -> batteryCombined
            else -> null
        }
}
