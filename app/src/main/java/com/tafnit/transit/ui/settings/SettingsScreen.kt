package com.tafnit.transit.ui.settings

import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.tafnit.transit.BuildConfig
import com.tafnit.transit.R
import com.tafnit.transit.domain.model.AppLanguage
import com.tafnit.transit.domain.model.ThemeMode
import com.tafnit.transit.domain.model.TransitMode
import com.tafnit.transit.domain.model.UserPreferences
import com.tafnit.transit.domain.repository.PlacesRepository
import com.tafnit.transit.domain.repository.TransitRepository
import com.tafnit.transit.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val placesRepository: PlacesRepository,
    private val transitRepository: TransitRepository
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = preferencesRepository.preferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    fun setLanguage(lang: AppLanguage) {
        viewModelScope.launch {
            preferencesRepository.setLanguage(lang)
            val locales = if (lang == AppLanguage.SYSTEM) {
                LocaleListCompat.getEmptyLocaleList()
            } else {
                LocaleListCompat.forLanguageTags(lang.tag)
            }
            AppCompatDelegate.setApplicationLocales(locales)
        }
    }

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch {
        preferencesRepository.setThemeMode(mode)
    }

    fun setDynamicColor(enabled: Boolean) = viewModelScope.launch {
        preferencesRepository.setDynamicColor(enabled)
    }

    fun setGetOffStops(stops: Int) = viewModelScope.launch {
        preferencesRepository.setGetOffAlertStopsBefore(stops)
    }

    fun setAlertSound(enabled: Boolean) = viewModelScope.launch {
        preferencesRepository.setAlertSound(enabled)
    }

    fun setAlertVibration(enabled: Boolean) = viewModelScope.launch {
        preferencesRepository.setAlertVibration(enabled)
    }

    fun setLockScreenNotif(enabled: Boolean) = viewModelScope.launch {
        preferencesRepository.setLockScreenNotification(enabled)
    }

    fun toggleDefaultMode(mode: TransitMode) = viewModelScope.launch {
        val current = preferences.value.defaultModes
        val updated = if (mode in current) {
            (current - mode).ifEmpty { setOf(TransitMode.BUS) }
        } else {
            current + mode
        }
        preferencesRepository.setDefaultModes(updated)
    }

    fun setMaxWalkMeters(meters: Int) = viewModelScope.launch {
        preferencesRepository.setMaxWalkMeters(meters)
    }

    fun setPreferAccessible(enabled: Boolean) = viewModelScope.launch {
        preferencesRepository.setPreferAccessible(enabled)
    }

    fun setUseLiveApi(enabled: Boolean) = viewModelScope.launch {
        preferencesRepository.setUseLiveApi(enabled)
    }

    fun clearRecentSearches() = viewModelScope.launch {
        placesRepository.clearRecentSearches()
    }

    fun clearCache() = viewModelScope.launch {
        transitRepository.clearScheduleCache()
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showLicenses by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Language
            item {
                SettingsSectionCard(title = stringResource(R.string.settings_language)) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            AppLanguage.SYSTEM to R.string.lang_system,
                            AppLanguage.HEBREW to R.string.lang_he,
                            AppLanguage.ENGLISH to R.string.lang_en
                        ).forEach { (lang, labelRes) ->
                            FilterChip(
                                selected = prefs.language == lang,
                                onClick = { viewModel.setLanguage(lang) },
                                label = { Text(stringResource(labelRes)) }
                            )
                        }
                    }
                }
            }

            // Appearance
            item {
                SettingsSectionCard(title = stringResource(R.string.settings_appearance)) {
                    Text(
                        text = stringResource(R.string.theme_mode),
                        style = MaterialTheme.typography.labelLarge
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            ThemeMode.SYSTEM to R.string.theme_system,
                            ThemeMode.LIGHT to R.string.theme_light,
                            ThemeMode.DARK to R.string.theme_dark
                        ).forEach { (mode, labelRes) ->
                            FilterChip(
                                selected = prefs.themeMode == mode,
                                onClick = { viewModel.setThemeMode(mode) },
                                label = { Text(stringResource(labelRes)) }
                            )
                        }
                    }
                    SettingsSwitchRow(
                        title = stringResource(R.string.dynamic_color),
                        subtitle = stringResource(R.string.dynamic_color_desc),
                        checked = prefs.dynamicColor,
                        onCheckedChange = viewModel::setDynamicColor
                    )
                }
            }

            // Navigation & Get-Off Alerts
            item {
                SettingsSectionCard(title = stringResource(R.string.settings_navigation)) {
                    Text(
                        text = stringResource(R.string.get_off_trigger),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = prefs.getOffAlertStopsBefore == 1,
                            onClick = { viewModel.setGetOffStops(1) },
                            label = { Text(stringResource(R.string.stops_before_1)) }
                        )
                        FilterChip(
                            selected = prefs.getOffAlertStopsBefore == 2,
                            onClick = { viewModel.setGetOffStops(2) },
                            label = { Text(stringResource(R.string.stops_before_2)) }
                        )
                    }
                    SettingsSwitchRow(
                        title = stringResource(R.string.alert_sound),
                        checked = prefs.alertSound,
                        onCheckedChange = viewModel::setAlertSound
                    )
                    SettingsSwitchRow(
                        title = stringResource(R.string.alert_vibration),
                        checked = prefs.alertVibration,
                        onCheckedChange = viewModel::setAlertVibration
                    )
                    SettingsSwitchRow(
                        title = stringResource(R.string.lock_screen_notif),
                        checked = prefs.lockScreenNotification,
                        onCheckedChange = viewModel::setLockScreenNotif
                    )
                }
            }

            // Default Routing Preferences
            item {
                SettingsSectionCard(title = stringResource(R.string.settings_routing)) {
                    Text(
                        text = stringResource(R.string.transit_modes),
                        style = MaterialTheme.typography.labelLarge
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            TransitMode.BUS to R.string.mode_bus,
                            TransitMode.RAIL to R.string.mode_rail,
                            TransitMode.LIGHT_RAIL to R.string.mode_light_rail,
                            TransitMode.SHUTTLE to R.string.mode_shuttle
                        ).forEach { (mode, labelRes) ->
                            FilterChip(
                                selected = mode in prefs.defaultModes,
                                onClick = { viewModel.toggleDefaultMode(mode) },
                                label = { Text(stringResource(labelRes)) }
                            )
                        }
                    }
                    Text(
                        text = stringResource(R.string.max_walk_distance, prefs.maxWalkMeters),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Slider(
                        value = prefs.maxWalkMeters.toFloat(),
                        onValueChange = { viewModel.setMaxWalkMeters((it / 100f).roundToInt() * 100) },
                        valueRange = 400f..2500f
                    )
                    SettingsSwitchRow(
                        title = stringResource(R.string.prefer_accessible),
                        checked = prefs.preferAccessible,
                        onCheckedChange = viewModel::setPreferAccessible
                    )
                }
            }

            // Data & Storage
            item {
                SettingsSectionCard(title = stringResource(R.string.settings_data)) {
                    Text(
                        text = stringResource(R.string.data_mode),
                        style = MaterialTheme.typography.labelLarge
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = !prefs.useLiveApi,
                            onClick = { viewModel.setUseLiveApi(false) },
                            label = { Text(stringResource(R.string.data_mode_demo)) }
                        )
                        FilterChip(
                            selected = prefs.useLiveApi,
                            onClick = { viewModel.setUseLiveApi(true) },
                            label = { Text(stringResource(R.string.data_mode_live)) }
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                viewModel.clearRecentSearches()
                                Toast.makeText(context, context.getString(R.string.cleared_toast), Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.clear_recent_searches))
                        }
                        OutlinedButton(
                            onClick = {
                                viewModel.clearCache()
                                Toast.makeText(context, context.getString(R.string.cleared_toast), Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.clear_cache))
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.app_version, BuildConfig.VERSION_NAME),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = { showLicenses = true }) {
                            Text(stringResource(R.string.open_source_licenses))
                        }
                    }
                }
            }
        }
    }

    if (showLicenses) {
        AlertDialog(
            onDismissRequest = { showLicenses = false },
            title = { Text(stringResource(R.string.open_source_licenses)) },
            text = {
                Text(
                    "Tafnit (תפנית) is open-source software (MIT License).\n\n" +
                        "• Jetpack Compose & Material 3 (Apache 2.0)\n" +
                        "• Jetpack Glance (Apache 2.0)\n" +
                        "• osmdroid & OpenStreetMap (Apache 2.0 / ODbL)\n" +
                        "• OpenBus Stride API by Hasadna (MIT / Open Data)\n" +
                        "• Photon Geocoder by Komoot (Apache 2.0)"
                )
            },
            confirmButton = {
                TextButton(onClick = { showLicenses = false }) {
                    Text(stringResource(R.string.save))
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
