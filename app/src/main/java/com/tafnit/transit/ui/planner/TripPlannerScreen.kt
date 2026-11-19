package com.tafnit.transit.ui.planner

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Accessible
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tafnit.transit.R
import com.tafnit.transit.domain.model.RouteOptimization
import com.tafnit.transit.domain.model.TimeOption
import com.tafnit.transit.domain.model.TransitMode
import com.tafnit.transit.domain.model.TransitOperator
import com.tafnit.transit.domain.model.TripItinerary
import com.tafnit.transit.ui.components.LineBadgePill
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripPlannerScreen(
    viewModel: TripPlannerViewModel,
    onBack: () -> Unit,
    onOpenSearchForDest: () -> Unit,
    onOpenItinerary: (TripItinerary) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val locale = LocalConfiguration.current.locales[0].language
    val isHebrew = locale == "iw" || locale == "he"

    TripPlannerContent(
        state = state,
        isHebrew = isHebrew,
        onBack = onBack,
        onSwap = viewModel::swapOriginDestination,
        onEditDestination = onOpenSearchForDest,
        onApplyFilter = viewModel::updateFilter,
        onTimeOptionChange = viewModel::updateTimeOption,
        onSelectItinerary = {
            viewModel.selectItinerary(it)
            onOpenItinerary(it)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TripPlannerContent(
    state: TripPlannerUiState,
    isHebrew: Boolean,
    onBack: () -> Unit,
    onSwap: () -> Unit,
    onEditDestination: () -> Unit,
    onApplyFilter: (Set<TransitMode>, RouteOptimization) -> Unit,
    onTimeOptionChange: (TimeOption) -> Unit,
    onSelectItinerary: (TripItinerary) -> Unit
) {
    var showFilterSheet by remember { mutableStateOf(false) }
    var showTimeDialog by remember { mutableStateOf(false) }

    val timeLabel = when (val opt = state.filter.timeOption) {
        TimeOption.LeaveNow -> stringResource(R.string.leave_now)
        is TimeOption.DepartAt -> {
            val t = java.time.Instant.ofEpochMilli(opt.epochMillis)
                .atZone(ZoneId.systemDefault()).toLocalTime()
                .format(DateTimeFormatter.ofPattern("HH:mm"))
            stringResource(R.string.depart_at, t)
        }
        is TimeOption.ArriveBy -> {
            val t = java.time.Instant.ofEpochMilli(opt.epochMillis)
                .atZone(ZoneId.systemDefault()).toLocalTime()
                .format(DateTimeFormatter.ofPattern("HH:mm"))
            stringResource(R.string.arrive_by, t)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.trip_planner)) },
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
            // Origin / Destination Card with Swap
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer
                        ) {
                            Text(
                                text = state.origin.localizedName(isHebrew),
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            )
                        }
                        Surface(
                            modifier = Modifier.clickable(onClick = onEditDestination),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer
                        ) {
                            Text(
                                text = state.destination.localizedName(isHebrew),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onSwap) {
                        Icon(
                            imageVector = Icons.Rounded.SwapVert,
                            contentDescription = stringResource(R.string.swap_origin_dest),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Time & Filter Chips Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = state.filter.timeOption !is TimeOption.LeaveNow,
                    onClick = { showTimeDialog = true },
                    label = { Text(timeLabel) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                FilterChip(
                    selected = state.filter.optimization != RouteOptimization.FASTEST,
                    onClick = { showFilterSheet = true },
                    label = { Text(stringResource(R.string.route_filters)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }

            // Route Comparison Cards
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.itineraries, key = { it.id }) { itinerary ->
                    RouteComparisonCard(
                        itinerary = itinerary,
                        onClick = { onSelectItinerary(itinerary) }
                    )
                }
            }
        }
    }

    if (showFilterSheet) {
        var selectedModes by remember { mutableStateOf(state.filter.modes) }
        var selectedOpt by remember { mutableStateOf(state.filter.optimization) }

        ModalBottomSheet(onDismissRequest = { showFilterSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.transit_modes),
                    style = MaterialTheme.typography.titleMedium
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        TransitMode.BUS to R.string.mode_bus,
                        TransitMode.RAIL to R.string.mode_rail,
                        TransitMode.LIGHT_RAIL to R.string.mode_light_rail,
                        TransitMode.SHUTTLE to R.string.mode_shuttle
                    ).forEach { (mode, labelRes) ->
                        val active = mode in selectedModes
                        FilterChip(
                            selected = active,
                            onClick = {
                                selectedModes = if (active) selectedModes - mode else selectedModes + mode
                            },
                            label = { Text(stringResource(labelRes)) }
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.route_optimization),
                    style = MaterialTheme.typography.titleMedium
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        RouteOptimization.FASTEST to R.string.opt_fastest,
                        RouteOptimization.LEAST_WALKING to R.string.opt_least_walking,
                        RouteOptimization.FEWEST_TRANSFERS to R.string.opt_fewest_transfers,
                        RouteOptimization.ACCESSIBLE to R.string.opt_accessible
                    ).forEach { (opt, labelRes) ->
                        FilterChip(
                            selected = selectedOpt == opt,
                            onClick = { selectedOpt = opt },
                            label = { Text(stringResource(labelRes)) }
                        )
                    }
                }

                Button(
                    onClick = {
                        onApplyFilter(selectedModes, selectedOpt)
                        showFilterSheet = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.apply_filters))
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    if (showTimeDialog) {
        val timePickerState = rememberTimePickerState(is24Hour = true)
        var modeIdx by remember { mutableIntStateOf(0) } // 0=LeaveNow, 1=DepartAt, 2=ArriveBy

        AlertDialog(
            onDismissRequest = { showTimeDialog = false },
            title = { Text(stringResource(R.string.select_time)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = modeIdx == 0,
                            onClick = { modeIdx = 0 },
                            label = { Text(stringResource(R.string.leave_now)) }
                        )
                        FilterChip(
                            selected = modeIdx == 1,
                            onClick = { modeIdx = 1 },
                            label = { Text(stringResource(R.string.depart_at_option)) }
                        )
                        FilterChip(
                            selected = modeIdx == 2,
                            onClick = { modeIdx = 2 },
                            label = { Text(stringResource(R.string.arrive_by_option)) }
                        )
                    }
                    if (modeIdx != 0) {
                        TimePicker(state = timePickerState)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val option = when (modeIdx) {
                            1 -> {
                                val millis = LocalDate.now()
                                    .atTime(LocalTime.of(timePickerState.hour, timePickerState.minute))
                                    .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                TimeOption.DepartAt(millis)
                            }
                            2 -> {
                                val millis = LocalDate.now()
                                    .atTime(LocalTime.of(timePickerState.hour, timePickerState.minute))
                                    .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                TimeOption.ArriveBy(millis)
                            }
                            else -> TimeOption.LeaveNow
                        }
                        onTimeOptionChange(option)
                        showTimeDialog = false
                    }
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimeDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun RouteComparisonCard(
    itinerary: TripItinerary,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.min_away, itinerary.totalDurationMinutes),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Text(
                            text = stringResource(R.string.leave_in_min, itinerary.leaveInMinutes),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
                Text(
                    text = "${itinerary.departureTime} – ${itinerary.arrivalTime}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Horizontal Leg Summary
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itinerary.legs.forEachIndexed { idx, leg ->
                    if (leg.mode == TransitMode.WALK) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.DirectionsWalk,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${leg.durationMinutes}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LineBadgePill(
                            lineNumber = leg.lineNumber ?: "",
                            operator = leg.operator ?: TransitOperator.OTHER,
                            mode = leg.mode,
                            isLive = leg.isLive
                        )
                    }
                    if (idx < itinerary.legs.lastIndex) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.total_walk_min, itinerary.totalWalkMinutes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (itinerary.transfersCount == 0) stringResource(R.string.direct_route)
                    else stringResource(R.string.transfers_count, itinerary.transfersCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (itinerary.isWheelchairAccessible) {
                    Icon(
                        imageVector = Icons.Rounded.Accessible,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
