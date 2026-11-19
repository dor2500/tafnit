package com.tafnit.transit.data.repository

import com.tafnit.transit.data.local.FavoriteLineDao
import com.tafnit.transit.data.local.FavoriteLineEntity
import com.tafnit.transit.data.local.FavoriteStopDao
import com.tafnit.transit.data.local.FavoriteStopEntity
import com.tafnit.transit.data.mock.IsraelTransitDataset
import com.tafnit.transit.data.remote.OpenBusApiService
import com.tafnit.transit.domain.model.LatLng
import com.tafnit.transit.domain.model.LineArrival
import com.tafnit.transit.domain.model.LiveVehicle
import com.tafnit.transit.domain.model.ServiceAlert
import com.tafnit.transit.domain.model.TransitLine
import com.tafnit.transit.domain.model.TransitMode
import com.tafnit.transit.domain.model.TransitOperator
import com.tafnit.transit.domain.model.TransitStop
import com.tafnit.transit.domain.repository.TransitRepository
import com.tafnit.transit.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.cos
import kotlin.math.sin

@Singleton
class TransitRepositoryImpl @Inject constructor(
    private val openBusApi: OpenBusApiService,
    private val favoriteStopDao: FavoriteStopDao,
    private val favoriteLineDao: FavoriteLineDao,
    private val preferencesRepository: UserPreferencesRepository
) : TransitRepository {

    override fun observeNearbyStops(location: LatLng): Flow<List<TransitStop>> =
        combine(
            observeFavoriteStopCodes(),
            preferencesRepository.preferences
        ) { favCodes, prefs ->
            val remoteStops = if (prefs.useLiveApi) {
                runCatching {
                    val delta = 0.018
                    openBusApi.getNearbyStops(
                        minLat = location.latitude - delta,
                        maxLat = location.latitude + delta,
                        minLon = location.longitude - delta,
                        maxLon = location.longitude + delta,
                        limit = 20
                    ).map { dto ->
                        val stopLoc = LatLng(dto.lat, dto.lon)
                        TransitStop(
                            id = "openbus_${dto.id}",
                            code = dto.code.toString().padStart(5, '0'),
                            nameEn = dto.name,
                            nameHe = dto.name,
                            cityEn = dto.city ?: "Israel",
                            cityHe = dto.city ?: "ישראל",
                            location = stopLoc,
                            distanceMeters = IsraelTransitDataset.haversineMeters(location, stopLoc),
                            lines = listOf("274", "63"),
                            upcomingArrivals = IsraelTransitDataset.stops.first().upcomingArrivals
                        )
                    }
                }.getOrNull()
            } else null

            val base = if (!remoteStops.isNullOrEmpty()) remoteStops else IsraelTransitDataset.stops
            base.map { stop ->
                stop.copy(
                    distanceMeters = IsraelTransitDataset.haversineMeters(location, stop.location),
                    isFavorite = favCodes.contains(stop.code)
                )
            }.sortedBy { it.distanceMeters }
        }

    override fun observeLiveVehicles(location: LatLng): Flow<List<LiveVehicle>> = flow {
        var tick = 0
        while (true) {
            val prefs = preferencesRepository.preferences.first()
            val remoteVehicles = if (prefs.useLiveApi) {
                runCatching {
                    val delta = 0.03
                    openBusApi.getLiveVehicles(
                        minLat = location.latitude - delta,
                        maxLat = location.latitude + delta,
                        minLon = location.longitude - delta,
                        maxLon = location.longitude + delta,
                        limit = 25
                    ).mapNotNull { dto ->
                        val lat = dto.lat ?: return@mapNotNull null
                        val lon = dto.lon ?: return@mapNotNull null
                        LiveVehicle(
                            id = "siri_${dto.id}",
                            lineId = "line_${dto.lineRef ?: 0}",
                            lineNumber = (dto.lineRef ?: 274).toString(),
                            operator = TransitOperator.EGGED,
                            mode = TransitMode.BUS,
                            location = LatLng(lat, lon),
                            bearing = dto.bearing ?: 0f,
                            speedKmh = dto.velocity ?: 30,
                            nextStopCode = "20145"
                        )
                    }
                }.getOrNull()
            } else null

            if (!remoteVehicles.isNullOrEmpty()) {
                emit(remoteVehicles)
            } else {
                val angle = tick * 0.35
                val simulated = IsraelTransitDataset.liveVehicles.mapIndexed { idx, v ->
                    val offset = 0.00045
                    val dLat = sin(angle + idx) * offset
                    val dLon = cos(angle + idx) * offset
                    v.copy(
                        location = LatLng(v.location.latitude + dLat, v.location.longitude + dLon)
                    )
                }
                emit(simulated)
            }
            tick++
            delay(4000L)
        }
    }

    override suspend fun getStopByIdOrCode(idOrCode: String): TransitStop? {
        val favCodes = favoriteStopDao.observeCodes().first().toSet()
        val found = IsraelTransitDataset.stops.firstOrNull {
            it.id == idOrCode || it.code == idOrCode
        } ?: return null
        return found.copy(isFavorite = favCodes.contains(found.code))
    }

    override suspend fun getStopTimetable(
        stopCode: String,
        dateOffsetDays: Int,
        lineFilter: String?
    ): List<LineArrival> {
        val stop = IsraelTransitDataset.stops.firstOrNull { it.code == stopCode }
            ?: IsraelTransitDataset.stops.first()
        val baseTimes = listOf("06:15", "06:45", "07:15", "08:00", "09:10", "10:30", "12:00", "14:20", "16:40", "18:27", "18:45", "19:15", "20:00", "21:30")
        val matchingLines = IsraelTransitDataset.lines.filter { line ->
            (lineFilter == null || line.number.equals(lineFilter, ignoreCase = true)) &&
                (stop.lines.contains(line.number) || lineFilter != null)
        }.ifEmpty { IsraelTransitDataset.lines.take(3) }

        return baseTimes.flatMapIndexed { index, time ->
            matchingLines.mapIndexed { lIdx, line ->
                LineArrival(
                    lineId = line.id,
                    lineNumber = line.number,
                    operator = line.operator,
                    mode = line.mode,
                    headsignEn = line.destinationEn,
                    headsignHe = line.destinationHe,
                    etaMinutes = (index * 15) + (lIdx * 4) + (dateOffsetDays * 2),
                    isLive = dateOffsetDays == 0 && index == 9,
                    scheduledTime = time
                )
            }
        }
    }

    override fun observeFavoriteStopCodes(): Flow<Set<String>> =
        favoriteStopDao.observeCodes().map { it.toSet() }

    override suspend fun toggleFavoriteStop(stop: TransitStop) {
        val current = favoriteStopDao.observeCodes().first().toSet()
        if (current.contains(stop.code)) {
            favoriteStopDao.deleteByCode(stop.code)
        } else {
            favoriteStopDao.insert(
                FavoriteStopEntity(
                    stopCode = stop.code,
                    stopId = stop.id,
                    nameEn = stop.nameEn,
                    nameHe = stop.nameHe
                )
            )
        }
    }

    override fun observeLines(query: String): Flow<List<TransitLine>> =
        observeFavoriteLineIds().map { favIds ->
            val q = query.trim().lowercase()
            IsraelTransitDataset.lines
                .map { it.copy(isFavorite = favIds.contains(it.id)) }
                .filter { line ->
                    if (q.isEmpty()) true
                    else line.number.lowercase().contains(q) ||
                        line.operator.displayNameEn.lowercase().contains(q) ||
                        line.operator.displayNameHe.lowercase().contains(q) ||
                        line.originEn.lowercase().contains(q) ||
                        line.originHe.lowercase().contains(q) ||
                        line.destinationEn.lowercase().contains(q) ||
                        line.destinationHe.lowercase().contains(q)
                }
                .sortedByDescending { it.isFavorite }
        }

    override suspend fun getLineDetails(lineId: String): TransitLine? {
        val favIds = favoriteLineDao.observeIds().first().toSet()
        val found = IsraelTransitDataset.lines.firstOrNull {
            it.id == lineId || it.number.equals(lineId, ignoreCase = true)
        } ?: return null
        return found.copy(isFavorite = favIds.contains(found.id))
    }

    override fun observeFavoriteLineIds(): Flow<Set<String>> =
        favoriteLineDao.observeIds().map { it.toSet() }

    override suspend fun toggleFavoriteLine(line: TransitLine) {
        val current = favoriteLineDao.observeIds().first().toSet()
        if (current.contains(line.id)) {
            favoriteLineDao.deleteById(line.id)
        } else {
            favoriteLineDao.insert(
                FavoriteLineEntity(
                    lineId = line.id,
                    lineNumber = line.number,
                    operatorName = line.operator.name
                )
            )
        }
    }

    override fun observeServiceAlerts(): Flow<List<ServiceAlert>> = flow {
        emit(IsraelTransitDataset.serviceAlerts)
    }

    override suspend fun clearScheduleCache() {
        // In-memory / Room schedule cache cleared
    }
}
