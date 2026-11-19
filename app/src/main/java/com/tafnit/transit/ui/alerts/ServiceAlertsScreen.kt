package com.tafnit.transit.ui.alerts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.tafnit.transit.R
import com.tafnit.transit.domain.model.AlertSeverity
import com.tafnit.transit.domain.model.ServiceAlert
import com.tafnit.transit.domain.repository.TransitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class ServiceAlertsUiState(
    val allAlerts: List<ServiceAlert> = emptyList(),
    val favoriteLineNumbers: Set<String> = emptySet(),
    val favoriteStopCodes: Set<String> = emptySet()
)

@HiltViewModel
class ServiceAlertsViewModel @Inject constructor(
    transitRepository: TransitRepository
) : ViewModel() {
    val uiState: StateFlow<ServiceAlertsUiState> = combine(
        transitRepository.observeServiceAlerts(),
        transitRepository.observeLines(""),
        transitRepository.observeFavoriteStopCodes()
    ) { alerts, lines, favStops ->
        val favLines = lines.filter { it.isFavorite }.map { it.number }.toSet()
        ServiceAlertsUiState(
            allAlerts = alerts,
            favoriteLineNumbers = favLines,
            favoriteStopCodes = favStops
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ServiceAlertsUiState())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceAlertsScreen(
    onBack: () -> Unit,
    viewModel: ServiceAlertsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val locale = LocalConfiguration.current.locales[0].language
    val isHebrew = locale == "iw" || locale == "he"
    var favoritesOnly by remember { mutableStateOf(false) }

    val displayed = if (favoritesOnly) {
        state.allAlerts.filter { alert ->
            alert.affectedLineNumbers.any { it in state.favoriteLineNumbers } ||
                alert.affectedStopCodes.any { it in state.favoriteStopCodes }
        }
    } else {
        state.allAlerts
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.alerts_title)) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = !favoritesOnly,
                    onClick = { favoritesOnly = false },
                    label = { Text(stringResource(R.string.filter_all_nearby)) }
                )
                FilterChip(
                    selected = favoritesOnly,
                    onClick = { favoritesOnly = true },
                    label = { Text(stringResource(R.string.filter_my_favorites)) }
                )
            }

            if (displayed.isEmpty()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Text(
                        text = stringResource(R.string.no_alerts_favorites),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayed, key = { it.id }) { alert ->
                        val isWarning = alert.severity != AlertSeverity.INFO
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = if (isWarning) MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.surfaceContainer
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isWarning) Icons.Rounded.WarningAmber else Icons.Rounded.Info,
                                        contentDescription = null,
                                        tint = if (isWarning) MaterialTheme.colorScheme.onErrorContainer
                                        else MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = alert.localizedTitle(isHebrew),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (isWarning) MaterialTheme.colorScheme.onErrorContainer
                                        else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = alert.updatedAt,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isWarning) MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = alert.localizedDescription(isHebrew),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isWarning) MaterialTheme.colorScheme.onErrorContainer
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(
                                        R.string.affected_lines,
                                        alert.affectedLineNumbers.joinToString(", ")
                                    ),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isWarning) MaterialTheme.colorScheme.onErrorContainer
                                    else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
