package com.tafnit.transit.ui.lines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tafnit.transit.data.mock.IsraelTransitDataset
import com.tafnit.transit.domain.model.LiveVehicle
import com.tafnit.transit.domain.model.TransitLine
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

data class LinesUiState(
    val query: String = "",
    val favoriteLines: List<TransitLine> = emptyList(),
    val allLines: List<TransitLine> = emptyList(),
    val selectedLine: TransitLine? = null,
    val liveVehicles: List<LiveVehicle> = emptyList()
)

@HiltViewModel
class LinesViewModel @Inject constructor(
    private val transitRepository: TransitRepository
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val selectedLineId = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val linesFlow = query.flatMapLatest { q ->
        transitRepository.observeLines(q)
    }

    val uiState: StateFlow<LinesUiState> = combine(
        query,
        linesFlow,
        selectedLineId,
        transitRepository.observeLiveVehicles(IsraelTransitDataset.DEFAULT_CENTRAL_ISRAEL)
    ) { q, lines, selectedId, vehicles ->
        LinesUiState(
            query = q,
            favoriteLines = lines.filter { it.isFavorite },
            allLines = lines,
            selectedLine = lines.firstOrNull { it.id == selectedId || it.number == selectedId },
            liveVehicles = vehicles
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LinesUiState())

    fun onQueryChange(newQuery: String) {
        query.value = newQuery
    }

    fun selectLine(lineId: String) {
        selectedLineId.value = lineId
    }

    fun toggleFavoriteLine(line: TransitLine) {
        viewModelScope.launch {
            transitRepository.toggleFavoriteLine(line)
        }
    }
}
