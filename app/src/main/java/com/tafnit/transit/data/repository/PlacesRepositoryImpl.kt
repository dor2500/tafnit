package com.tafnit.transit.data.repository

import com.tafnit.transit.data.local.FavoritePlaceDao
import com.tafnit.transit.data.local.FavoritePlaceEntity
import com.tafnit.transit.data.local.RecentSearchDao
import com.tafnit.transit.data.local.RecentSearchEntity
import com.tafnit.transit.data.mock.IsraelTransitDataset
import com.tafnit.transit.data.remote.PhotonApiService
import com.tafnit.transit.domain.model.FavoritePlace
import com.tafnit.transit.domain.model.LatLng
import com.tafnit.transit.domain.model.PlaceCategory
import com.tafnit.transit.domain.model.PlaceResult
import com.tafnit.transit.domain.repository.PlacesRepository
import com.tafnit.transit.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlacesRepositoryImpl @Inject constructor(
    private val favoritePlaceDao: FavoritePlaceDao,
    private val recentSearchDao: RecentSearchDao,
    private val photonApi: PhotonApiService,
    private val preferencesRepository: UserPreferencesRepository
) : PlacesRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch {
            if (favoritePlaceDao.count() == 0) {
                IsraelTransitDataset.defaultFavorites.forEach { fav ->
                    favoritePlaceDao.upsert(fav.toEntity())
                }
            }
        }
    }

    override fun observeFavorites(): Flow<List<FavoritePlace>> =
        favoritePlaceDao.observeAll().map { entities ->
            entities.map {
                FavoritePlace(
                    id = it.id,
                    label = it.label,
                    iconKey = it.iconKey,
                    address = it.address,
                    location = LatLng(it.latitude, it.longitude),
                    isPinned = it.isPinned
                )
            }
        }

    override suspend fun upsertFavorite(place: FavoritePlace) {
        favoritePlaceDao.upsert(place.toEntity())
    }

    override suspend fun deleteFavorite(id: String) {
        favoritePlaceDao.deleteById(id)
    }

    override fun observeRecentSearches(): Flow<List<PlaceResult>> =
        recentSearchDao.observeAll().map { list ->
            list.map {
                PlaceResult(
                    id = it.id,
                    nameEn = it.nameEn,
                    nameHe = it.nameHe,
                    subtitleEn = it.subtitleEn,
                    subtitleHe = it.subtitleHe,
                    location = LatLng(it.latitude, it.longitude),
                    category = runCatching { PlaceCategory.valueOf(it.category) }
                        .getOrDefault(PlaceCategory.ADDRESS),
                    stopCode = it.stopCode
                )
            }
        }

    override suspend fun addRecentSearch(place: PlaceResult) {
        recentSearchDao.upsert(
            RecentSearchEntity(
                id = place.id,
                nameEn = place.nameEn,
                nameHe = place.nameHe,
                subtitleEn = place.subtitleEn,
                subtitleHe = place.subtitleHe,
                latitude = place.location.latitude,
                longitude = place.location.longitude,
                category = place.category.name,
                stopCode = place.stopCode,
                searchedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun clearRecentSearches() {
        recentSearchDao.clearAll()
    }

    override suspend fun searchPlaces(query: String, biasLocation: LatLng): List<PlaceResult> {
        val q = query.trim()
        if (q.isEmpty()) return IsraelTransitDataset.landmarks

        val qLower = q.lowercase()
        val matchingStops = IsraelTransitDataset.stops.filter {
            it.code.contains(qLower) ||
                it.nameEn.lowercase().contains(qLower) ||
                it.nameHe.lowercase().contains(qLower) ||
                it.cityEn.lowercase().contains(qLower) ||
                it.cityHe.lowercase().contains(qLower)
        }.map { stop ->
            PlaceResult(
                id = "place_stop_${stop.code}",
                nameEn = "${stop.nameEn} (${stop.code})",
                nameHe = "${stop.nameHe} (${stop.code})",
                subtitleEn = "${stop.cityEn} • Lines ${stop.lines.joinToString(", ")}",
                subtitleHe = "${stop.cityHe} • קווים ${stop.lines.joinToString(", ")}",
                location = stop.location,
                category = PlaceCategory.STOP,
                stopCode = stop.code
            )
        }

        val matchingLandmarks = IsraelTransitDataset.landmarks.filter {
            it.nameEn.lowercase().contains(qLower) ||
                it.nameHe.lowercase().contains(qLower) ||
                it.subtitleEn.lowercase().contains(qLower) ||
                it.subtitleHe.lowercase().contains(qLower)
        }

        // Always query OpenStreetMap Photon API for real Israeli addresses & POIs,
        // falling back gracefully to local stops & landmarks when offline.
        val remotePlaces = runCatching {
            photonApi.search(
                query = q,
                lat = biasLocation.latitude,
                lon = biasLocation.longitude
            ).features.mapIndexedNotNull { idx, feature ->
                val coords = feature.geometry.coordinates
                if (coords.size < 2) return@mapIndexedNotNull null
                val props = feature.properties
                val streetAndNum = listOfNotNull(props.street, props.housenumber)
                    .joinToString(" ")
                    .trim()
                val title = props.name?.takeIf { it.isNotBlank() }
                    ?: streetAndNum.ifBlank { return@mapIndexedNotNull null }
                val subtitle = listOfNotNull(
                    streetAndNum.takeIf { it.isNotBlank() && it != title },
                    props.city ?: props.state,
                    props.country
                ).distinct().joinToString(", ")
                PlaceResult(
                    id = "photon_${idx}_${coords[1]}_${coords[0]}",
                    nameEn = title,
                    nameHe = title,
                    subtitleEn = subtitle.ifBlank { "Israel" },
                    subtitleHe = subtitle.ifBlank { "ישראל" },
                    location = LatLng(coords[1], coords[0]),
                    category = PlaceCategory.ADDRESS
                )
            }
        }.getOrDefault(emptyList())

        return (matchingStops + remotePlaces + matchingLandmarks).distinctBy { it.id }
    }

    override suspend fun reverseGeocode(location: LatLng): PlaceResult {
        val remote = runCatching {
            photonApi.reverse(location.latitude, location.longitude).features.firstOrNull()
        }.getOrNull()
        if (remote != null) {
            val props = remote.properties
            val title = props.name ?: listOfNotNull(props.street, props.housenumber).joinToString(" ")
            if (title.isNotBlank()) {
                val city = props.city ?: "Israel"
                return PlaceResult(
                    id = "picked_${location.latitude}_${location.longitude}",
                    nameEn = title,
                    nameHe = title,
                    subtitleEn = city,
                    subtitleHe = city,
                    location = location,
                    category = PlaceCategory.ADDRESS
                )
            }
        }
        val nearestStop = IsraelTransitDataset.stops.minByOrNull {
            IsraelTransitDataset.haversineMeters(location, it.location)
        }
        val areaEn = nearestStop?.cityEn ?: "Central Israel"
        val areaHe = nearestStop?.cityHe ?: "מרכז ישראל"
        val formattedCoords = "%.4f, %.4f".format(location.latitude, location.longitude)
        return PlaceResult(
            id = "picked_${location.latitude}_${location.longitude}",
            nameEn = "Pinned Location ($areaEn)",
            nameHe = "מיקום שנבחר ($areaHe)",
            subtitleEn = formattedCoords,
            subtitleHe = formattedCoords,
            location = location,
            category = PlaceCategory.ADDRESS
        )
    }

    private fun FavoritePlace.toEntity() = FavoritePlaceEntity(
        id = id,
        label = label,
        iconKey = iconKey,
        address = address,
        latitude = location.latitude,
        longitude = location.longitude,
        isPinned = isPinned
    )
}
