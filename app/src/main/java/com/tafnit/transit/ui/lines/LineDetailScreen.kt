package com.tafnit.transit.ui.lines

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tafnit.transit.R
import com.tafnit.transit.data.mock.IsraelTransitDataset
import com.tafnit.transit.domain.model.LineStopEntry
import com.tafnit.transit.domain.model.TransitLine
import com.tafnit.transit.ui.components.EtaPill
import com.tafnit.transit.ui.components.LineBadgePill
import com.tafnit.transit.ui.components.TransitMapCanvas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LineDetailScreen(
    lineId: String,
    onBack: () -> Unit,
    onOpenStopDetails: (String) -> Unit,
    onOpenAlerts: () -> Unit,
    viewModel: LinesViewModel = hiltViewModel()
) {
    LaunchedEffect(lineId) {
        viewModel.selectLine(lineId)
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val line = state.selectedLine ?: state.allLines.firstOrNull() ?: IsraelTransitDataset.lines.first()
    val locale = LocalConfiguration.current.locales[0].language
    val isHebrew = locale == "iw" || locale == "he"

    LineDetailContent(
        line = line,
        state = state,
        isHebrew = isHebrew,
        onBack = onBack,
        onToggleFavorite = { viewModel.toggleFavoriteLine(line) },
        onOpenStopDetails = onOpenStopDetails,
        onOpenAlerts = onOpenAlerts
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LineDetailContent(
    line: TransitLine,
    state: LinesUiState,
    isHebrew: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenStopDetails: (String) -> Unit,
    onOpenAlerts: () -> Unit
) {
    var directionIdx by remember { mutableIntStateOf(0) }
    var tabIdx by remember { mutableIntStateOf(0) }

    val activeStops = if (directionIdx == 0) line.outboundStops else line.returnStops
    val activePolyline = if (directionIdx == 0) line.outboundPolyline else line.returnPolyline
    val activeSchedule = if (directionIdx == 0) line.scheduleTimesOutbound else line.scheduleTimesReturn
    val mapCenter = activeStops.firstOrNull()?.stop?.location ?: IsraelTransitDataset.DEFAULT_CENTRAL_ISRAEL

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LineBadgePill(
                            lineNumber = line.number,
                            operator = line.operator,
                            mode = line.mode,
                            isLive = true
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = line.localizedRouteTitle(isHebrew),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (line.isFavorite) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                            contentDescription = stringResource(R.string.toggle_favorite),
                            tint = if (line.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
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
            TransitMapCanvas(
                center = mapCenter,
                stops = activeStops.map { it.stop },
                vehicles = state.liveVehicles.filter { it.lineNumber == line.number },
                polylines = listOf(activePolyline to false),
                zoomLevel = 13.0,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(155.dp)
                    .clip(RoundedCornerShape(20.dp))
            )

            if (line.hasActiveAlert) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable(onClick = onOpenAlerts),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WarningAmber,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.active_disruption_banner),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                SegmentedButton(
                    selected = directionIdx == 0,
                    onClick = { directionIdx = 0 },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text(stringResource(R.string.direction_outbound))
                }
                SegmentedButton(
                    selected = directionIdx == 1,
                    onClick = { directionIdx = 1 },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text(stringResource(R.string.direction_return))
                }
            }

            TabRow(selectedTabIndex = tabIdx) {
                Tab(
                    selected = tabIdx == 0,
                    onClick = { tabIdx = 0 },
                    text = { Text(stringResource(R.string.stops_timeline)) }
                )
                Tab(
                    selected = tabIdx == 1,
                    onClick = { tabIdx = 1 },
                    text = { Text(stringResource(R.string.schedule_tab)) }
                )
            }

            if (tabIdx == 0) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(activeStops, key = { "${directionIdx}_${it.stop.code}" }) { entry ->
                        LineStopTimelineRow(
                            entry = entry,
                            isLast = entry == activeStops.lastOrNull(),
                            isHebrew = isHebrew,
                            onClick = { onOpenStopDetails(entry.stop.code) }
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(activeSchedule.chunked(3)) { rowTimes ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowTimes.forEach { time ->
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainer
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = time,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                            repeat(3 - rowTimes.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LineStopTimelineRow(
    entry: LineStopEntry,
    isLast: Boolean,
    isHebrew: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(34.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(
                        if (entry.isBusApproaching) MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.primary
                    )
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(42.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.stop.localizedName(isHebrew),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${stringResource(R.string.stop_code_prefix, entry.stop.code)} • ${entry.stop.localizedCity(isHebrew)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (entry.isBusApproaching) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DirectionsBus,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.bus_approaching_here),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }

        EtaPill(etaMinutes = entry.etaMinutes, isLive = entry.isLiveEta)
    }
}
