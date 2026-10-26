package dev.budsscope.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dev.budsscope.app.data.local.dao.BatteryDao
import dev.budsscope.app.data.local.dao.ConnectionDao
import dev.budsscope.app.data.local.entity.BatterySnapshotEntity
import dev.budsscope.app.data.local.entity.ConnectionEventEntity

@Database(
    entities = [
        BatterySnapshotEntity::class,
        ConnectionEventEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun batteryDao(): BatteryDao
    abstract fun connectionDao(): ConnectionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budsscope.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}
