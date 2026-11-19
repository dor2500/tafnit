package com.tafnit.transit.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tafnit.transit.domain.model.AppLanguage
import com.tafnit.transit.domain.model.ThemeMode
import com.tafnit.transit.domain.model.TransitMode
import com.tafnit.transit.domain.model.UserPreferences
import com.tafnit.transit.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "tafnit_prefs")

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UserPreferencesRepository {

    private object Keys {
        val LANGUAGE = stringPreferencesKey("language")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val GET_OFF_STOPS = intPreferencesKey("get_off_stops")
        val ALERT_SOUND = booleanPreferencesKey("alert_sound")
        val ALERT_VIBRATION = booleanPreferencesKey("alert_vibration")
        val LOCK_SCREEN_NOTIF = booleanPreferencesKey("lock_screen_notif")
        val DEFAULT_MODES = stringSetPreferencesKey("default_modes")
        val MAX_WALK_METERS = intPreferencesKey("max_walk_meters")
        val PREFER_ACCESSIBLE = booleanPreferencesKey("prefer_accessible")
        val USE_LIVE_API = booleanPreferencesKey("use_live_api")
    }

    override val preferences: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val lang = prefs[Keys.LANGUAGE]?.let { name ->
            AppLanguage.entries.firstOrNull { it.name == name }
        } ?: AppLanguage.SYSTEM

        val theme = prefs[Keys.THEME_MODE]?.let { name ->
            ThemeMode.entries.firstOrNull { it.name == name }
        } ?: ThemeMode.SYSTEM

        val modes = prefs[Keys.DEFAULT_MODES]?.mapNotNull { m ->
            TransitMode.entries.firstOrNull { it.name == m }
        }?.toSet()?.takeIf { it.isNotEmpty() } ?: setOf(
            TransitMode.BUS,
            TransitMode.RAIL,
            TransitMode.LIGHT_RAIL,
            TransitMode.SHUTTLE
        )

        UserPreferences(
            language = lang,
            themeMode = theme,
            dynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: true,
            getOffAlertStopsBefore = prefs[Keys.GET_OFF_STOPS] ?: 2,
            alertSound = prefs[Keys.ALERT_SOUND] ?: true,
            alertVibration = prefs[Keys.ALERT_VIBRATION] ?: true,
            lockScreenNotification = prefs[Keys.LOCK_SCREEN_NOTIF] ?: true,
            defaultModes = modes,
            maxWalkMeters = prefs[Keys.MAX_WALK_METERS] ?: 1200,
            preferAccessible = prefs[Keys.PREFER_ACCESSIBLE] ?: false,
            useLiveApi = prefs[Keys.USE_LIVE_API] ?: false
        )
    }

    override suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language.name }
    }

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = themeMode.name }
    }

    override suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
    }

    override suspend fun setGetOffAlertStopsBefore(stops: Int) {
        context.dataStore.edit { it[Keys.GET_OFF_STOPS] = stops.coerceIn(1, 2) }
    }

    override suspend fun setAlertSound(enabled: Boolean) {
        context.dataStore.edit { it[Keys.ALERT_SOUND] = enabled }
    }

    override suspend fun setAlertVibration(enabled: Boolean) {
        context.dataStore.edit { it[Keys.ALERT_VIBRATION] = enabled }
    }

    override suspend fun setLockScreenNotification(enabled: Boolean) {
        context.dataStore.edit { it[Keys.LOCK_SCREEN_NOTIF] = enabled }
    }

    override suspend fun setDefaultModes(modes: Set<TransitMode>) {
        context.dataStore.edit { it[Keys.DEFAULT_MODES] = modes.map { m -> m.name }.toSet() }
    }

    override suspend fun setMaxWalkMeters(meters: Int) {
        context.dataStore.edit { it[Keys.MAX_WALK_METERS] = meters }
    }

    override suspend fun setPreferAccessible(enabled: Boolean) {
        context.dataStore.edit { it[Keys.PREFER_ACCESSIBLE] = enabled }
    }

    override suspend fun setUseLiveApi(enabled: Boolean) {
        context.dataStore.edit { it[Keys.USE_LIVE_API] = enabled }
    }
}
