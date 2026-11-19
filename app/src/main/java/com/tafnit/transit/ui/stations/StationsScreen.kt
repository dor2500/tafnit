package com.tafnit.transit.ui.stations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tafnit.transit.R
import com.tafnit.transit.ui.components.TransitMapCanvas
import com.tafnit.transit.ui.home.NearbyStopRowCard

@Composable
fun StationsScreen(
    onOpenStopDetails: (String) -> Unit,
    viewModel: StationsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val locale = LocalConfiguration.current.locales[0].language
    val isHebrew = locale == "iw" || locale == "he"

    Column(modifier = Modifier.fillMaxSize()) {
        // Top half: Interactive map with nearby stops & live vehicles
        TransitMapCanvas(
            center = state.userLocation,
            stops = state.stops,
            vehicles = state.liveVehicles,
            onStopClick = { onOpenStopDetails(it.code) },
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        )

        // Bottom half: Material 3 sheet surface listing nearby stops
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = stringResource(R.string.nearby_stops),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
                )
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.stops, key = { it.id }) { stop ->
                        NearbyStopRowCard(
                            stop = stop,
                            isHebrew = isHebrew,
                            onClick = { onOpenStopDetails(stop.code) }
                        )
                    }
                }
            }
        }
    }
}
