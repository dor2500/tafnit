package com.tafnit.transit.ui.stations

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import com.tafnit.transit.domain.model.LineArrival
import com.tafnit.transit.domain.model.TransitStop
import com.tafnit.transit.ui.components.EtaPill
import com.tafnit.transit.ui.components.LineBadgePill
import com.tafnit.transit.ui.components.TransitMapCanvas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StationDetailScreen(
    stopCode: String,
    onBack: () -> Unit,
    onNavigateToStop: (TransitStop) -> Unit,
    onOpenLineDetails: (String) -> Unit,
    viewModel: StationsViewModel = hiltViewModel()
) {
    LaunchedEffect(stopCode) {
        viewModel.loadStop(stopCode)
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val stop = state.selectedStop ?: state.stops.firstOrNull()
    val locale = LocalConfiguration.current.locales[0].language
    val isHebrew = locale == "iw" || locale == "he"
    var selectedTab by remember { mutableIntStateOf(0) }
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stop?.localizedName(isHebrew) ?: stringResource(R.string.station_details),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (stop != null) {
                            Text(
                                text = "${stringResource(R.string.stop_code_prefix, stop.code)} • ${stringResource(R.string.min_walk, stop.walkMinutes)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
                    if (stop != null) {
                        IconButton(onClick = { viewModel.toggleFavoriteStop(stop) }) {
                            Icon(
                                imageVector = if (stop.isFavorite) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                contentDescription = stringResource(R.string.toggle_favorite),
                                tint = if (stop.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (stop == null) return@Scaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Mini-map showing station & approaching buses
            TransitMapCanvas(
                center = stop.location,
                stops = listOf(stop),
                vehicles = state.liveVehicles,
                zoomLevel = 15.5,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(145.dp)
                    .clip(RoundedCornerShape(20.dp))
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stop.localizedCity(isHebrew),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FilledTonalButton(onClick = { onNavigateToStop(stop) }) {
                    Icon(
                        imageVector = Icons.Rounded.Directions,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.navigate_to_stop))
                }
            }

            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(stringResource(R.string.live_arrivals)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(stringResource(R.string.full_timetable)) }
                )
            }

            if (selectedTab == 0) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(stop.upcomingArrivals) { arrival ->
                        ArrivalRowCard(
                            arrival = arrival,
                            isHebrew = isHebrew,
                            onClick = { onOpenLineDetails(arrival.lineId) }
                        )
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = state.dateOffsetDays == 0,
                                onClick = { viewModel.setDateOffset(0) },
                                label = { Text(stringResource(R.string.today)) }
                            )
                        }
                        item {
                            FilterChip(
                                selected = state.dateOffsetDays == 1,
                                onClick = { viewModel.setDateOffset(1) },
                                label = { Text(stringResource(R.string.tomorrow)) }
                            )
                        }
                        item {
                            FilterChip(
                                selected = state.dateOffsetDays > 1,
                                onClick = { showDatePicker = true },
                                label = { Text(stringResource(R.string.pick_date)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.CalendarMonth,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                        item {
                            FilterChip(
                                selected = state.lineFilter == null,
                                onClick = { viewModel.setLineFilter(null) },
                                label = { Text(stringResource(R.string.all_lines)) }
                            )
                        }
                        items(stop.lines) { lineNo ->
                            FilterChip(
                                selected = state.lineFilter == lineNo,
                                onClick = { viewModel.setLineFilter(lineNo) },
                                label = { Text(lineNo) }
                            )
                        }
                    }

                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.timetable) { item ->
                            TimetableRow(
                                arrival = item,
                                isHebrew = isHebrew,
                                onClick = { onOpenLineDetails(item.lineId) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setDateOffset(2)
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun ArrivalRowCard(
    arrival: LineArrival,
    isHebrew: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LineBadgePill(
                lineNumber = arrival.lineNumber,
                operator = arrival.operator,
                mode = arrival.mode,
                isLive = arrival.isLive
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isHebrew) arrival.headsignHe else arrival.headsignEn,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${if (isHebrew) arrival.operator.displayNameHe else arrival.operator.displayNameEn} • ${arrival.scheduledTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            EtaPill(etaMinutes = arrival.etaMinutes, isLive = arrival.isLive)
        }
    }
}

@Composable
private fun TimetableRow(
    arrival: LineArrival,
    isHebrew: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = arrival.scheduledTime,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            LineBadgePill(
                lineNumber = arrival.lineNumber,
                operator = arrival.operator,
                mode = arrival.mode
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (isHebrew) arrival.headsignHe else arrival.headsignEn,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
