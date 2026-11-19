package com.tafnit.transit.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tafnit.transit.R
import com.tafnit.transit.domain.model.FavoritePlace
import com.tafnit.transit.domain.model.TransitStop
import com.tafnit.transit.ui.components.EtaPill
import com.tafnit.transit.ui.components.LineBadgePill
import com.tafnit.transit.ui.components.LivePulseDot
import com.tafnit.transit.ui.components.TransitMapCanvas
import com.tafnit.transit.ui.components.favoriteIconByKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenSearch: () -> Unit,
    onOpenAlerts: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenFavoriteTrip: (FavoritePlace) -> Unit,
    onOpenStopDetails: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreenContent(
        state = state,
        onOpenSearch = onOpenSearch,
        onOpenAlerts = onOpenAlerts,
        onOpenSettings = onOpenSettings,
        onOpenFavoriteTrip = onOpenFavoriteTrip,
        onOpenStopDetails = onOpenStopDetails,
        onRecenter = viewModel::refreshLocation,
        onCycleLayer = viewModel::cycleMapLayer,
        onToggleNearbySheet = viewModel::setShowNearbySheet
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    state: HomeUiState,
    onOpenSearch: () -> Unit,
    onOpenAlerts: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenFavoriteTrip: (FavoritePlace) -> Unit,
    onOpenStopDetails: (String) -> Unit,
    onRecenter: () -> Unit,
    onCycleLayer: () -> Unit,
    onToggleNearbySheet: (Boolean) -> Unit
) {
    val locale = LocalConfiguration.current.locales[0].language
    val isHebrew = locale == "iw" || locale == "he"

    Box(modifier = Modifier.fillMaxSize()) {
        TransitMapCanvas(
            center = state.mapCenter,
            stops = state.nearbyStops,
            vehicles = state.liveVehicles,
            layerStyle = state.mapLayer,
            onStopClick = { stop -> onOpenStopDetails(stop.code) },
            modifier = Modifier.fillMaxSize()
        )

        // Floating Top Search Bar + Quick Chips
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(top = 12.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable(onClick = onOpenSearch),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 6.dp,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.search_placeholder),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    IconButton(onClick = onOpenAlerts) {
                        BadgedBox(
                            badge = {
                                if (state.alerts.isNotEmpty()) {
                                    Badge {
                                        Text(state.alerts.size.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.NotificationsActive,
                                contentDescription = stringResource(R.string.service_alerts),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = stringResource(R.string.settings),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.favorites, key = { it.id }) { fav ->
                    val label = when (fav.iconKey) {
                        "home" -> stringResource(R.string.chip_home)
                        "work" -> stringResource(R.string.chip_work)
                        else -> fav.label
                    }
                    AssistChip(
                        onClick = { onOpenFavoriteTrip(fav) },
                        label = { Text(label, fontWeight = FontWeight.Medium) },
                        leadingIcon = {
                            Icon(
                                imageVector = favoriteIconByKey(fav.iconKey),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        shape = CircleShape,
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        border = null,
                        elevation = AssistChipDefaults.assistChipElevation(elevation = 2.dp)
                    )
                }
            }
        }

        // Map Utility FABs
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 112.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SmallFloatingActionButton(
                onClick = onCycleLayer,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Icon(
                    imageVector = Icons.Rounded.Layers,
                    contentDescription = stringResource(R.string.map_layers)
                )
            }
            FloatingActionButton(
                onClick = onRecenter,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    imageVector = Icons.Rounded.MyLocation,
                    contentDescription = stringResource(R.string.recenter_map)
                )
            }
        }

        // Nearby Stops Bottom Sheet Peek Card
        val nearest = state.nearbyStops.firstOrNull()
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp)
                .clickable { onToggleNearbySheet(true) },
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 4.dp,
            shadowElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.DirectionsBus,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = nearest?.localizedName(isHebrew) ?: stringResource(R.string.nearby_stops),
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LivePulseDot()
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.live_vehicles_count, state.liveVehicles.size),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (nearest != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        nearest.upcomingArrivals.take(3).forEach { arr ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LineBadgePill(
                                    lineNumber = arr.lineNumber,
                                    operator = arr.operator,
                                    mode = arr.mode,
                                    isLive = arr.isLive
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(R.string.min_away, arr.etaMinutes),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        if (state.showNearbySheet) {
            ModalBottomSheet(
                onDismissRequest = { onToggleNearbySheet(false) },
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Text(
                    text = stringResource(R.string.nearby_stops),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.nearbyStops.take(6), key = { it.id }) { stop ->
                        NearbyStopRowCard(
                            stop = stop,
                            isHebrew = isHebrew,
                            onClick = {
                                onToggleNearbySheet(false)
                                onOpenStopDetails(stop.code)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NearbyStopRowCard(
    stop: TransitStop,
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
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stop.localizedName(isHebrew),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${stringResource(R.string.stop_code_prefix, stop.code)} • ${stringResource(R.string.min_walk, stop.walkMinutes)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = stringResource(R.string.meters_away, stop.distanceMeters),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (stop.upcomingArrivals.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    stop.upcomingArrivals.take(3).forEach { arr ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            LineBadgePill(
                                lineNumber = arr.lineNumber,
                                operator = arr.operator,
                                mode = arr.mode,
                                isLive = arr.isLive
                            )
                            EtaPill(etaMinutes = arr.etaMinutes, isLive = arr.isLive)
                        }
                    }
                }
            }
        }
    }
}
