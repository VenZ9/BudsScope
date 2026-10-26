package dev.budsscope.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "budsscope_settings")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val LOW_BATTERY_ALERT = booleanPreferencesKey("low_battery_alert")
        val LOW_BATTERY_THRESHOLD = intPreferencesKey("low_battery_threshold")
        val DISCONNECT_ALERT = booleanPreferencesKey("disconnect_alert")
        val PERSISTENT_SERVICE = booleanPreferencesKey("persistent_service")
        val FLUID_WAVE_ANIMATION = booleanPreferencesKey("fluid_wave_animation")
    }

    val lowBatteryAlertEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.LOW_BATTERY_ALERT] ?: true
        }

    val lowBatteryThreshold: Flow<Int> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.LOW_BATTERY_THRESHOLD] ?: 20
        }

    val disconnectAlertEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.DISCONNECT_ALERT] ?: true
        }

    val persistentServiceEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.PERSISTENT_SERVICE] ?: false
        }

    val fluidWaveAnimationEnabled: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.FLUID_WAVE_ANIMATION] ?: true
        }

    suspend fun setLowBatteryAlert(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LOW_BATTERY_ALERT] = enabled
        }
    }

    suspend fun setLowBatteryThreshold(threshold: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LOW_BATTERY_THRESHOLD] = threshold.coerceIn(5, 50)
        }
    }

    suspend fun setDisconnectAlert(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DISCONNECT_ALERT] = enabled
        }
    }

    suspend fun setPersistentService(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PERSISTENT_SERVICE] = enabled
        }
    }

    suspend fun setFluidWaveAnimation(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FLUID_WAVE_ANIMATION] = enabled
        }
    }

    suspend fun clearSettings() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
