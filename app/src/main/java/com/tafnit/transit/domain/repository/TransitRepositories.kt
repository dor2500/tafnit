package com.tafnit.transit.domain.repository

import com.tafnit.transit.domain.model.FavoritePlace
import com.tafnit.transit.domain.model.LatLng
import com.tafnit.transit.domain.model.LineArrival
import com.tafnit.transit.domain.model.LiveVehicle
import com.tafnit.transit.domain.model.PlaceResult
import com.tafnit.transit.domain.model.RouteFilter
import com.tafnit.transit.domain.model.ServiceAlert
import com.tafnit.transit.domain.model.ThemeMode
import com.tafnit.transit.domain.model.AppLanguage
import com.tafnit.transit.domain.model.TransitLine
import com.tafnit.transit.domain.model.TransitMode
import com.tafnit.transit.domain.model.TransitStop
import com.tafnit.transit.domain.model.TripItinerary
import com.tafnit.transit.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface TransitRepository {
    fun observeNearbyStops(location: LatLng): Flow<List<TransitStop>>
    fun observeLiveVehicles(location: LatLng): Flow<List<LiveVehicle>>
    suspend fun getStopByIdOrCode(idOrCode: String): TransitStop?
    suspend fun getStopTimetable(stopCode: String, dateOffsetDays: Int, lineFilter: String?): List<LineArrival>
    fun observeFavoriteStopCodes(): Flow<Set<String>>
    suspend fun toggleFavoriteStop(stop: TransitStop)

    fun observeLines(query: String): Flow<List<TransitLine>>
    suspend fun getLineDetails(lineId: String): TransitLine?
    fun observeFavoriteLineIds(): Flow<Set<String>>
    suspend fun toggleFavoriteLine(line: TransitLine)

    fun observeServiceAlerts(): Flow<List<ServiceAlert>>
    suspend fun clearScheduleCache()
}

interface PlacesRepository {
    fun observeFavorites(): Flow<List<FavoritePlace>>
    suspend fun upsertFavorite(place: FavoritePlace)
    suspend fun deleteFavorite(id: String)

    fun observeRecentSearches(): Flow<List<PlaceResult>>
    suspend fun addRecentSearch(place: PlaceResult)
    suspend fun clearRecentSearches()

    suspend fun searchPlaces(query: String, biasLocation: LatLng): List<PlaceResult>
    suspend fun reverseGeocode(location: LatLng): PlaceResult
}

interface TripPlannerRepository {
    suspend fun planTrips(
        origin: PlaceResult,
        destination: PlaceResult,
        filter: RouteFilter
    ): List<TripItinerary>
}

interface LocationRepository {
    fun observeUserLocation(): Flow<LatLng>
    suspend fun getLastKnownOrDefault(): LatLng
    fun hasLocationPermission(): Boolean
}

interface UserPreferencesRepository {
    val preferences: Flow<UserPreferences>
    suspend fun setLanguage(language: AppLanguage)
    suspend fun setThemeMode(themeMode: ThemeMode)
    suspend fun setDynamicColor(enabled: Boolean)
    suspend fun setGetOffAlertStopsBefore(stops: Int)
    suspend fun setAlertSound(enabled: Boolean)
    suspend fun setAlertVibration(enabled: Boolean)
    suspend fun setLockScreenNotification(enabled: Boolean)
    suspend fun setDefaultModes(modes: Set<TransitMode>)
    suspend fun setMaxWalkMeters(meters: Int)
    suspend fun setPreferAccessible(enabled: Boolean)
    suspend fun setUseLiveApi(enabled: Boolean)
}
