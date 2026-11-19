package com.tafnit.transit.ui.planner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tafnit.transit.data.mock.IsraelTransitDataset
import com.tafnit.transit.domain.model.PlaceCategory
import com.tafnit.transit.domain.model.PlaceResult
import com.tafnit.transit.domain.model.RouteFilter
import com.tafnit.transit.domain.model.RouteOptimization
import com.tafnit.transit.domain.model.TimeOption
import com.tafnit.transit.domain.model.TransitMode
import com.tafnit.transit.domain.model.TripItinerary
import com.tafnit.transit.domain.repository.LocationRepository
import com.tafnit.transit.domain.repository.TripPlannerRepository
import com.tafnit.transit.domain.repository.UserPreferencesRepository
import com.tafnit.transit.service.LiveNavigationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TripPlannerUiState(
    val origin: PlaceResult = PlaceResult(
        id = "current_loc",
        nameEn = "Current Location",
        nameHe = "המיקום הנוכחי",
        subtitleEn = "Central Israel",
        subtitleHe = "מרכז ישראל",
        location = IsraelTransitDataset.DEFAULT_CENTRAL_ISRAEL,
        category = PlaceCategory.ADDRESS
    ),
    val destination: PlaceResult = IsraelTransitDataset.landmarks.first(),
    val filter: RouteFilter = RouteFilter(),
    val itineraries: List<TripItinerary> = emptyList(),
    val selectedItinerary: TripItinerary? = null,
    val isLoading: Boolean = false
)

enum class SearchTarget {
    ORIGIN,
    DESTINATION
}

@HiltViewModel
class TripPlannerViewModel @Inject constructor(
    private val tripPlannerRepository: TripPlannerRepository,
    private val locationRepository: LocationRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val liveNavigationManager: LiveNavigationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TripPlannerUiState())
    val uiState: StateFlow<TripPlannerUiState> = _uiState.asStateFlow()

    var pendingSearchTarget: SearchTarget = SearchTarget.DESTINATION

    init {
        viewModelScope.launch {
            val prefs = preferencesRepository.preferences.first()
            val loc = locationRepository.getLastKnownOrDefault()
            val defaultOrigin = _uiState.value.origin.copy(location = loc)
            val defaultFilter = RouteFilter(
                modes = prefs.defaultModes,
                optimization = if (prefs.preferAccessible) RouteOptimization.ACCESSIBLE else RouteOptimization.FASTEST,
                maxWalkMeters = prefs.maxWalkMeters
            )
            _uiState.value = _uiState.value.copy(
                origin = defaultOrigin,
                filter = defaultFilter
            )
            recalculateRoutes()
        }
    }

    fun setOrigin(place: PlaceResult) {
        _uiState.value = _uiState.value.copy(origin = place)
        recalculateRoutes()
    }

    fun setDestination(place: PlaceResult) {
        _uiState.value = _uiState.value.copy(destination = place)
        recalculateRoutes()
    }

    fun setPlaceForTarget(place: PlaceResult) {
        if (pendingSearchTarget == SearchTarget.ORIGIN) {
            setOrigin(place)
        } else {
            setDestination(place)
        }
        pendingSearchTarget = SearchTarget.DESTINATION
    }

    fun swapOriginDestination() {
        val current = _uiState.value
        _uiState.value = current.copy(
            origin = current.destination,
            destination = current.origin
        )
        recalculateRoutes()
    }

    fun updateFilter(modes: Set<TransitMode>, optimization: RouteOptimization) {
        val safeModes = modes.ifEmpty { setOf(TransitMode.BUS) }
        _uiState.value = _uiState.value.copy(
            filter = _uiState.value.filter.copy(
                modes = safeModes,
                optimization = optimization
            )
        )
        recalculateRoutes()
    }

    fun updateTimeOption(timeOption: TimeOption) {
        _uiState.value = _uiState.value.copy(
            filter = _uiState.value.filter.copy(timeOption = timeOption)
        )
        recalculateRoutes()
    }

    fun selectItinerary(itinerary: TripItinerary) {
        _uiState.value = _uiState.value.copy(selectedItinerary = itinerary)
    }

    fun startNavigation(itinerary: TripItinerary, simulate: Boolean = true) {
        liveNavigationManager.startNavigation(itinerary, simulate = simulate)
    }

    private fun recalculateRoutes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val state = _uiState.value
            val routes = tripPlannerRepository.planTrips(
                origin = state.origin,
                destination = state.destination,
                filter = state.filter
            )
            _uiState.value = _uiState.value.copy(
                itineraries = routes,
                selectedItinerary = routes.firstOrNull(),
                isLoading = false
            )
        }
    }
}
