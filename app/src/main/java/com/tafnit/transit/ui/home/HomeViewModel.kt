package com.tafnit.transit.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tafnit.transit.data.mock.IsraelTransitDataset
import com.tafnit.transit.domain.model.FavoritePlace
import com.tafnit.transit.domain.model.LatLng
import com.tafnit.transit.domain.model.LiveVehicle
import com.tafnit.transit.domain.model.MapLayerStyle
import com.tafnit.transit.domain.model.ServiceAlert
import com.tafnit.transit.domain.model.TransitStop
import com.tafnit.transit.domain.repository.LocationRepository
import com.tafnit.transit.domain.repository.PlacesRepository
import com.tafnit.transit.domain.repository.TransitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val userLocation: LatLng = IsraelTransitDataset.DEFAULT_CENTRAL_ISRAEL,
    val mapCenter: LatLng = IsraelTransitDataset.DEFAULT_CENTRAL_ISRAEL,
    val nearbyStops: List<TransitStop> = emptyList(),
    val liveVehicles: List<LiveVehicle> = emptyList(),
    val favorites: List<FavoritePlace> = emptyList(),
    val alerts: List<ServiceAlert> = emptyList(),
    val mapLayer: MapLayerStyle = MapLayerStyle.STANDARD,
    val showNearbySheet: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val transitRepository: TransitRepository,
    placesRepository: PlacesRepository
) : ViewModel() {

    private val userLocation = MutableStateFlow(IsraelTransitDataset.DEFAULT_CENTRAL_ISRAEL)
    private val mapCenter = MutableStateFlow(IsraelTransitDataset.DEFAULT_CENTRAL_ISRAEL)
    private val mapLayer = MutableStateFlow(MapLayerStyle.STANDARD)
    private val showNearbySheet = MutableStateFlow(false)

    init {
        refreshLocation()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val nearbyStopsFlow = userLocation.flatMapLatest {
        transitRepository.observeNearbyStops(it)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val liveVehiclesFlow = userLocation.flatMapLatest {
        transitRepository.observeLiveVehicles(it)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        userLocation,
        mapCenter,
        nearbyStopsFlow,
        liveVehiclesFlow,
        placesRepository.observeFavorites(),
        transitRepository.observeServiceAlerts(),
        mapLayer,
        showNearbySheet
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        HomeUiState(
            userLocation = values[0] as LatLng,
            mapCenter = values[1] as LatLng,
            nearbyStops = values[2] as List<TransitStop>,
            liveVehicles = values[3] as List<LiveVehicle>,
            favorites = values[4] as List<FavoritePlace>,
            alerts = values[5] as List<ServiceAlert>,
            mapLayer = values[6] as MapLayerStyle,
            showNearbySheet = values[7] as Boolean
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    fun refreshLocation() {
        viewModelScope.launch {
            val loc = locationRepository.getLastKnownOrDefault()
            userLocation.value = loc
            mapCenter.value = loc
        }
    }

    fun cycleMapLayer() {
        mapLayer.value = when (mapLayer.value) {
            MapLayerStyle.STANDARD -> MapLayerStyle.TRANSIT
            MapLayerStyle.TRANSIT -> MapLayerStyle.DARK
            MapLayerStyle.DARK -> MapLayerStyle.STANDARD
        }
    }

    fun setShowNearbySheet(show: Boolean) {
        showNearbySheet.value = show
    }
}
