package com.tafnit.transit.ui.planner

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tafnit.transit.R
import com.tafnit.transit.data.mock.IsraelTransitDataset
import com.tafnit.transit.domain.model.TransitMode
import com.tafnit.transit.domain.model.TransitOperator
import com.tafnit.transit.domain.model.TripItinerary
import com.tafnit.transit.domain.model.TripLeg
import com.tafnit.transit.ui.components.LineBadgePill
import com.tafnit.transit.ui.components.TransitMapCanvas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItineraryDetailScreen(
    viewModel: TripPlannerViewModel,
    onBack: () -> Unit,
    onStartLiveNavigation: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val itinerary = state.selectedItinerary ?: state.itineraries.firstOrNull() ?: return
    val locale = LocalConfiguration.current.locales[0].language
    val isHebrew = locale == "iw" || locale == "he"

    ItineraryDetailContent(
        itinerary = itinerary,
        isHebrew = isHebrew,
        onBack = onBack,
        onStartTrip = {
            viewModel.startNavigation(itinerary, simulate = true)
            onStartLiveNavigation()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItineraryDetailContent(
    itinerary: TripItinerary,
    isHebrew: Boolean,
    onBack: () -> Unit,
    onStartTrip: () -> Unit
) {
    val context = LocalContext.current
    val polylines = itinerary.legs.map { leg ->
        leg.polyline to (leg.mode == TransitMode.WALK)
    }
    val mapCenter = itinerary.legs.firstOrNull()?.polyline?.firstOrNull()
        ?: IsraelTransitDataset.DEFAULT_CENTRAL_ISRAEL

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.itinerary_details))
                        Text(
                            text = "${stringResource(R.string.min_away, itinerary.totalDurationMinutes)} • ${itinerary.departureTime} – ${itinerary.arrivalTime}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    IconButton(
                        onClick = {
                            val originPt = itinerary.legs.firstOrNull()?.polyline?.firstOrNull()
                            val destPt = itinerary.legs.lastOrNull()?.polyline?.lastOrNull()
                            val url = if (originPt != null && destPt != null) {
                                "https://www.google.com/maps/dir/?api=1&origin=${originPt.latitude},${originPt.longitude}&destination=${destPt.latitude},${destPt.longitude}&travelmode=transit"
                            } else {
                                "https://www.google.com/maps"
                            }
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Map,
                            contentDescription = stringResource(R.string.open_in_google_maps)
                        )
                    }
                    IconButton(
                        onClick = {
                            val summary = buildString {
                                appendLine("Tafnit (תפנית) • ${itinerary.totalDurationMinutes} min (${itinerary.departureTime} – ${itinerary.arrivalTime})")
                                itinerary.legs.forEach { leg ->
                                    if (leg.mode == TransitMode.WALK) {
                                        appendLine("• Walk ${leg.durationMinutes} min (${leg.distanceMeters} m)")
                                    } else {
                                        val stopName = leg.boardingStop?.localizedName(isHebrew) ?: ""
                                        appendLine("• Line ${leg.lineNumber} at $stopName (${leg.departureTime} → ${leg.arrivalTime})")
                                    }
                                }
                            }
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, summary)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_itinerary)))
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = stringResource(R.string.share_itinerary)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onStartTrip,
                icon = { Icon(Icons.Rounded.Navigation, contentDescription = null) },
                text = { Text(stringResource(R.string.start_trip)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
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
                polylines = polylines,
                zoomLevel = 13.0,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(175.dp)
                    .clip(RoundedCornerShape(20.dp))
            )

            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 92.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(itinerary.legs, key = { it.id }) { leg ->
                    ItineraryLegCard(leg = leg, isHebrew = isHebrew)
                }
            }
        }
    }
}

@Composable
private fun ItineraryLegCard(
    leg: TripLeg,
    isHebrew: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            if (leg.mode == TransitMode.WALK) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.DirectionsWalk,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        val stopName = leg.boardingStop?.localizedName(isHebrew)
                        Text(
                            text = if (stopName != null) stringResource(R.string.walk_to_stop, stopName)
                            else stringResource(R.string.walk_to_dest),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${stringResource(R.string.meters_away, leg.distanceMeters)} • ${stringResource(R.string.min_away, leg.durationMinutes)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = leg.departureTime,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LineBadgePill(
                        lineNumber = leg.lineNumber ?: "",
                        operator = leg.operator ?: TransitOperator.OTHER,
                        mode = leg.mode,
                        isLive = leg.isLive
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        val boardingName = leg.boardingStop?.localizedName(isHebrew) ?: ""
                        Text(
                            text = stringResource(R.string.board_line_at, leg.lineNumber ?: "", boardingName),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        val headsign = if (isHebrew) leg.headsignHe else leg.headsignEn
                        Text(
                            text = stringResource(R.string.towards_headsign, headsign ?: ""),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (leg.boardingStop != null) {
                            Text(
                                text = stringResource(R.string.stop_code_prefix, leg.boardingStop.code),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        text = leg.departureTime,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .clickable { expanded = !expanded }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val stopCount = leg.intermediateStops.size + 1
                    Text(
                        text = stringResource(R.string.intermediate_stops, stopCount, leg.durationMinutes),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (expanded) {
                    Column(
                        modifier = Modifier.padding(start = 12.dp, top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        leg.intermediateStops.forEach { mid ->
                            Text(
                                text = "• ${mid.localizedName(isHebrew)} (${mid.code})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (leg.alightingStop != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${stringResource(R.string.get_off_at, leg.alightingStop.localizedName(isHebrew))} (${leg.arrivalTime})",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}
