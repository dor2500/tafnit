package com.tafnit.transit.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tafnit.transit.data.mock.IsraelTransitDataset
import com.tafnit.transit.domain.model.FavoritePlace
import com.tafnit.transit.domain.model.LatLng
import com.tafnit.transit.domain.model.PlaceResult
import com.tafnit.transit.domain.repository.LocationRepository
import com.tafnit.transit.domain.repository.PlacesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val favorites: List<FavoritePlace> = emptyList(),
    val recentSearches: List<PlaceResult> = emptyList(),
    val autocompleteResults: List<PlaceResult> = emptyList(),
    val isSearching: Boolean = false
)

@OptIn(FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val placesRepository: PlacesRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val query = MutableStateFlow("")

    private val resultsFlow = query
        .debounce(250L)
        .mapLatest { q ->
            if (q.isBlank()) emptyList()
            else {
                val bias = locationRepository.getLastKnownOrDefault()
                placesRepository.searchPlaces(q, bias)
            }
        }

    val uiState: StateFlow<SearchUiState> = combine(
        query,
        placesRepository.observeFavorites(),
        placesRepository.observeRecentSearches(),
        resultsFlow
    ) { q, favs, recents, results ->
        SearchUiState(
            query = q,
            favorites = favs,
            recentSearches = recents.ifEmpty { IsraelTransitDataset.landmarks.take(4) },
            autocompleteResults = results,
            isSearching = q.isNotBlank()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SearchUiState())

    fun onQueryChange(newQuery: String) {
        query.value = newQuery
    }

    fun recordRecent(place: PlaceResult) {
        viewModelScope.launch {
            placesRepository.addRecentSearch(place)
        }
    }

    fun togglePlaceFavorite(place: PlaceResult, isHebrew: Boolean) {
        viewModelScope.launch {
            val favId = "fav_${place.id}"
            val existing = uiState.value.favorites.firstOrNull { it.id == favId }
            if (existing != null) {
                placesRepository.deleteFavorite(favId)
            } else {
                placesRepository.upsertFavorite(
                    FavoritePlace(
                        id = favId,
                        label = place.localizedName(isHebrew),
                        iconKey = "star",
                        address = place.localizedSubtitle(isHebrew),
                        location = place.location,
                        isPinned = false
                    )
                )
            }
        }
    }

    fun saveFavorite(id: String?, label: String, iconKey: String, address: String, location: LatLng?) {
        viewModelScope.launch {
            val existing = id?.let { target -> uiState.value.favorites.firstOrNull { it.id == target } }
            placesRepository.upsertFavorite(
                FavoritePlace(
                    id = existing?.id ?: "fav_${UUID.randomUUID()}",
                    label = label.ifBlank { "Saved Place" },
                    iconKey = iconKey,
                    address = address.ifBlank { "Central Israel" },
                    location = location ?: existing?.location ?: IsraelTransitDataset.DEFAULT_CENTRAL_ISRAEL,
                    isPinned = existing?.isPinned ?: (iconKey == "home" || iconKey == "work")
                )
            )
        }
    }

    fun deleteFavorite(id: String) {
        viewModelScope.launch {
            placesRepository.deleteFavorite(id)
        }
    }

    suspend fun reverseGeocode(location: LatLng): PlaceResult =
        placesRepository.reverseGeocode(location)
}
