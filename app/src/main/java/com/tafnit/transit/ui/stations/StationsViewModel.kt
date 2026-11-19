package com.tafnit.transit.ui.stations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tafnit.transit.data.mock.IsraelTransitDataset
import com.tafnit.transit.domain.model.LatLng
import com.tafnit.transit.domain.model.LineArrival
import com.tafnit.transit.domain.model.LiveVehicle
import com.tafnit.transit.domain.model.TransitStop
import com.tafnit.transit.domain.repository.LocationRepository
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

data class StationsUiState(
    val userLocation: LatLng = IsraelTransitDataset.DEFAULT_CENTRAL_ISRAEL,
    val stops: List<TransitStop> = emptyList(),
    val liveVehicles: List<LiveVehicle> = emptyList(),
    val selectedStop: TransitStop? = null,
    val timetable: List<LineArrival> = emptyList(),
    val dateOffsetDays: Int = 0,
    val lineFilter: String? = null
)

@HiltViewModel
class StationsViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val transitRepository: TransitRepository
) : ViewModel() {

    private val userLocation = MutableStateFlow(IsraelTransitDataset.DEFAULT_CENTRAL_ISRAEL)
    private val selectedStopCode = MutableStateFlow<String?>(null)
    private val dateOffsetDays = MutableStateFlow(0)
    private val lineFilter = MutableStateFlow<String?>(null)
    private val timetable = MutableStateFlow<List<LineArrival>>(emptyList())

    init {
        viewModelScope.launch {
            userLocation.value = locationRepository.getLastKnownOrDefault()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val stopsFlow = userLocation.flatMapLatest {
        transitRepository.observeNearbyStops(it)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val vehiclesFlow = userLocation.flatMapLatest {
        transitRepository.observeLiveVehicles(it)
    }

    val uiState: StateFlow<StationsUiState> = combine(
        userLocation,
        stopsFlow,
        vehiclesFlow,
        selectedStopCode,
        timetable,
        dateOffsetDays,
        lineFilter
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val stops = values[1] as List<TransitStop>
        val code = values[3] as String?
        StationsUiState(
            userLocation = values[0] as LatLng,
            stops = stops,
            liveVehicles = values[2] as List<LiveVehicle>,
            selectedStop = stops.firstOrNull { it.code == code },
            timetable = values[4] as List<LineArrival>,
            dateOffsetDays = values[5] as Int,
            lineFilter = values[6] as String?
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StationsUiState())

    fun loadStop(stopCode: String) {
        selectedStopCode.value = stopCode
        refreshTimetable()
    }

    fun setDateOffset(days: Int) {
        dateOffsetDays.value = days
        refreshTimetable()
    }

    fun setLineFilter(line: String?) {
        lineFilter.value = line
        refreshTimetable()
    }

    fun toggleFavoriteStop(stop: TransitStop) {
        viewModelScope.launch {
            transitRepository.toggleFavoriteStop(stop)
        }
    }

    private fun refreshTimetable() {
        val code = selectedStopCode.value ?: return
        viewModelScope.launch {
            timetable.value = transitRepository.getStopTimetable(
                stopCode = code,
                dateOffsetDays = dateOffsetDays.value,
                lineFilter = lineFilter.value
            )
        }
    }
}
