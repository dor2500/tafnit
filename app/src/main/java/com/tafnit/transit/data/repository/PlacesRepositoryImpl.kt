package com.tafnit.transit.data.repository

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.tafnit.transit.data.local.FavoritePlaceDao
import com.tafnit.transit.data.local.FavoritePlaceEntity
import com.tafnit.transit.data.local.RecentSearchDao
import com.tafnit.transit.data.local.RecentSearchEntity
import com.tafnit.transit.data.mock.IsraelTransitDataset
import com.tafnit.transit.data.remote.NominatimApiService
import com.tafnit.transit.data.remote.PhotonApiService
import com.tafnit.transit.domain.model.FavoritePlace
import com.tafnit.transit.domain.model.LatLng
import com.tafnit.transit.domain.model.PlaceCategory
import com.tafnit.transit.domain.model.PlaceResult
import com.tafnit.transit.domain.repository.PlacesRepository
import com.tafnit.transit.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class PlacesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val favoritePlaceDao: FavoritePlaceDao,
    private val recentSearchDao: RecentSearchDao,
    private val photonApi: PhotonApiService,
    private val nominatimApi: NominatimApiService,
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

        // 1. Google / Android Platform Geocoder (uses Google Play Services on device, no API key needed)
        val googleGeocoderResults = searchWithPlatformGeocoder(q)

        // 2. OpenStreetMap Nominatim (strong Hebrew Israeli address coverage)
        val nominatimResults = runCatching {
            nominatimApi.search(query = q).mapNotNull { dto ->
                val lat = dto.lat.toDoubleOrNull() ?: return@mapNotNull null
                val lon = dto.lon.toDoubleOrNull() ?: return@mapNotNull null
                val parts = dto.displayName.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                val title = dto.name?.takeIf { it.isNotBlank() }
                    ?: parts.take(2).joinToString(", ").ifBlank { dto.displayName }
                val subtitle = parts.drop(1).take(3).joinToString(", ").ifBlank { "Israel" }
                PlaceResult(
                    id = "nom_${dto.placeId}",
                    nameEn = title,
                    nameHe = title,
                    subtitleEn = subtitle,
                    subtitleHe = subtitle,
                    location = LatLng(lat, lon),
                    category = PlaceCategory.ADDRESS
                )
            }
        }.getOrDefault(emptyList())

        // 3. OpenStreetMap Photon API fallback
        val photonResults = if (googleGeocoderResults.isEmpty() && nominatimResults.isEmpty()) {
            runCatching {
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
        } else emptyList()

        return (matchingStops + googleGeocoderResults + nominatimResults + photonResults + matchingLandmarks)
            .distinctBy { "${it.nameEn}_${"%.4f".format(it.location.latitude)}_${"%.4f".format(it.location.longitude)}" }
    }

    private suspend fun searchWithPlatformGeocoder(query: String): List<PlaceResult> =
        withContext(Dispatchers.IO) {
            if (!Geocoder.isPresent()) return@withContext emptyList()
            val geocoder = Geocoder(context, Locale("he", "IL"))
            val addresses: List<Address> = runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { cont ->
                        geocoder.getFromLocationName(
                            query,
                            8,
                            29.4,
                            34.2,
                            33.4,
                            35.9
                        ) { list ->
                            if (cont.isActive) cont.resume(list)
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocationName(query, 8, 29.4, 34.2, 33.4, 35.9).orEmpty()
                }
            }.getOrDefault(emptyList())

            addresses.mapIndexed { idx, addr ->
                val street = listOfNotNull(addr.thoroughfare, addr.subThoroughfare).joinToString(" ").trim()
                val title = street.ifBlank { addr.featureName ?: addr.getAddressLine(0) ?: query }
                val subtitle = listOfNotNull(addr.locality, addr.adminArea, addr.countryName)
                    .distinct()
                    .joinToString(", ")
                    .ifBlank { addr.getAddressLine(0) ?: "Israel" }
                PlaceResult(
                    id = "ggeo_${idx}_${addr.latitude}_${addr.longitude}",
                    nameEn = title,
                    nameHe = title,
                    subtitleEn = subtitle,
                    subtitleHe = subtitle,
                    location = LatLng(addr.latitude, addr.longitude),
                    category = PlaceCategory.ADDRESS
                )
            }
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
