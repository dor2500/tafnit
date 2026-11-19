package com.tafnit.transit.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tafnit.transit.R
import com.tafnit.transit.data.mock.IsraelTransitDataset
import com.tafnit.transit.domain.model.ActiveNavigationState
import com.tafnit.transit.domain.model.NavigationPhase
import com.tafnit.transit.domain.model.TransitMode
import com.tafnit.transit.domain.model.TransitOperator
import com.tafnit.transit.service.LiveNavigationManager
import com.tafnit.transit.ui.components.LineBadgePill
import com.tafnit.transit.ui.components.TransitMapCanvas
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class LiveNavigationViewModel @Inject constructor(
    private val navigationManager: LiveNavigationManager
) : ViewModel() {
    val state: StateFlow<ActiveNavigationState> = navigationManager.state

    fun advanceStep() = navigationManager.advanceSimulationStep()
    fun toggleSimulation(enabled: Boolean) = navigationManager.toggleSimulation(enabled)
    fun stopNavigation() = navigationManager.stopNavigation()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveNavigationScreen(
    onBack: () -> Unit,
    viewModel: LiveNavigationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LiveNavigationContent(
        state = state,
        onBack = onBack,
        onAdvanceStep = viewModel::advanceStep,
        onToggleSim = viewModel::toggleSimulation,
        onStopNavigation = {
            viewModel.stopNavigation()
            onBack()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveNavigationContent(
    state: ActiveNavigationState,
    onBack: () -> Unit,
    onAdvanceStep: () -> Unit,
    onToggleSim: (Boolean) -> Unit,
    onStopNavigation: () -> Unit
) {
    val activeTransitLeg = state.itinerary?.legs?.firstOrNull { it.mode != TransitMode.WALK }
    val lineNum = activeTransitLeg?.lineNumber ?: "274"
    val operator = activeTransitLeg?.operator ?: TransitOperator.EGGED
    val mode = activeTransitLeg?.mode ?: TransitMode.BUS

    val phaseTitle = when (state.phase) {
        NavigationPhase.WALKING_TO_STOP -> stringResource(R.string.leg_walking)
        NavigationPhase.WAITING_AT_STOP -> stringResource(R.string.leg_waiting, lineNum)
        NavigationPhase.ON_BOARD -> stringResource(R.string.leg_onboard, lineNum)
        NavigationPhase.ARRIVED -> stringResource(R.string.leg_arrived)
    }

    val progress = when (state.phase) {
        NavigationPhase.WALKING_TO_STOP -> 0.25f
        NavigationPhase.WAITING_AT_STOP -> 0.45f
        NavigationPhase.ON_BOARD -> (1f - (state.stopsRemaining * 0.15f)).coerceIn(0.5f, 0.9f)
        NavigationPhase.ARRIVED -> 1f
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.live_navigation)) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TransitMapCanvas(
                center = activeTransitLeg?.boardingStop?.location ?: IsraelTransitDataset.DEFAULT_CENTRAL_ISRAEL,
                polylines = state.itinerary?.legs?.map { it.polyline to (it.mode == TransitMode.WALK) } ?: emptyList(),
                zoomLevel = 14.0,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(24.dp))
            )

            // Active Leg Status Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LineBadgePill(
                            lineNumber = lineNum,
                            operator = operator,
                            mode = mode,
                            isLive = true
                        )
                        Text(
                            text = stringResource(R.string.min_away, state.etaMinutes),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Text(
                        text = phaseTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    if (state.phase == NavigationPhase.ON_BOARD) {
                        Text(
                            text = stringResource(R.string.stops_remaining, state.stopsRemaining),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )

                    Text(
                        text = stringResource(R.string.next_stop_label, state.nextStopName),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = stringResource(R.string.alighting_stop_label, state.alightingStopName),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }
            }

            // Get-Off Alert Banner when triggered
            if (state.alertStageTriggered != 0) {
                val (alertTitle, alertBody) = when (state.alertStageTriggered) {
                    2 -> stringResource(R.string.alert_two_stops_title) to stringResource(R.string.alert_two_stops_body, state.alightingStopName)
                    1 -> stringResource(R.string.alert_one_stop_title) to stringResource(R.string.alert_one_stop_body, state.alightingStopName)
                    else -> stringResource(R.string.alert_arrived_title) to stringResource(R.string.alert_arrived_body, state.alightingStopName)
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.NotificationsActive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = alertTitle,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                text = alertBody,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }

            // Demo Simulation Controls
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.simulate_trip),
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = stringResource(R.string.simulating_active),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = state.isSimulating,
                        onCheckedChange = onToggleSim
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(
                    onClick = onAdvanceStep,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.SkipNext, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.advance_step))
                }
                Button(
                    onClick = onStopNavigation,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.Stop, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.stop_navigation))
                }
            }
        }
    }
}
