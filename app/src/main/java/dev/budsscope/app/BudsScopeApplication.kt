package dev.budsscope.app

import android.app.Application
import dev.budsscope.app.data.local.AppDatabase
import dev.budsscope.app.data.preferences.UserPreferencesRepository
import dev.budsscope.app.repository.BatteryRepository
import dev.budsscope.app.repository.BluetoothRepository
import dev.budsscope.app.repository.MediaSessionRepository
import dev.budsscope.app.service.ConnectionNotificationController
import dev.budsscope.app.widget.WidgetUpdateHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BudsScopeApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var database: AppDatabase
        private set

    lateinit var preferencesRepository: UserPreferencesRepository
        private set

    lateinit var notificationController: ConnectionNotificationController
        private set

    lateinit var bluetoothRepository: BluetoothRepository
        private set

    lateinit var batteryRepository: BatteryRepository
        private set

    lateinit var mediaSessionRepository: MediaSessionRepository
        private set

    override fun onCreate() {
        super.onCreate()

        database = AppDatabase.getInstance(this)
        preferencesRepository = UserPreferencesRepository(this)
        notificationController = ConnectionNotificationController(this)

        bluetoothRepository = BluetoothRepository(this, applicationScope)
        batteryRepository = BatteryRepository(
            batteryDao = database.batteryDao(),
            preferencesRepository = preferencesRepository,
            notificationController = notificationController,
            scope = applicationScope
        )
        mediaSessionRepository = MediaSessionRepository(this, applicationScope)

        // Observe active device to record battery snapshots & refresh widgets
        applicationScope.launch {
            bluetoothRepository.activeDevice.collectLatest { active ->
                if (active != null) {
                    batteryRepository.recordSnapshotIfAppropriate(active)
                }
                WidgetUpdateHelper.updateAllWidgets(this@BudsScopeApplication, active)
            }
        }
    }
}
