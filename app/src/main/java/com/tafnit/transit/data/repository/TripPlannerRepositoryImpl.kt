package com.tafnit.transit.data.repository

import com.tafnit.transit.data.mock.IsraelTransitDataset
import com.tafnit.transit.domain.model.PlaceResult
import com.tafnit.transit.domain.model.RouteFilter
import com.tafnit.transit.domain.model.RouteOptimization
import com.tafnit.transit.domain.model.TransitMode
import com.tafnit.transit.domain.model.TransitOperator
import com.tafnit.transit.domain.model.TripItinerary
import com.tafnit.transit.domain.model.TripLeg
import com.tafnit.transit.domain.repository.TripPlannerRepository
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripPlannerRepositoryImpl @Inject constructor() : TripPlannerRepository {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    override suspend fun planTrips(
        origin: PlaceResult,
        destination: PlaceResult,
        filter: RouteFilter
    ): List<TripItinerary> {
        val originStop = IsraelTransitDataset.stops.minByOrNull {
            IsraelTransitDataset.haversineMeters(origin.location, it.location)
        } ?: IsraelTransitDataset.stops[0]

        val destStop = IsraelTransitDataset.stops.minByOrNull {
            IsraelTransitDataset.haversineMeters(destination.location, it.location)
        } ?: IsraelTransitDataset.stops[1]

        val midStop = IsraelTransitDataset.stops.firstOrNull {
            it.code != originStop.code && it.code != destStop.code
        } ?: IsraelTransitDataset.stops[2]

        val totalMeters = IsraelTransitDataset.haversineMeters(origin.location, destination.location)
            .coerceAtLeast(800)
        val estimatedTransitMin = (totalMeters / 420).coerceIn(8, 95)

        val now = LocalTime.now()
        val candidates = mutableListOf<TripItinerary>()

        // Route 1: Direct Egged Bus 274 (fastest direct bus)
        if (TransitMode.BUS in filter.modes) {
            val walkStart = (IsraelTransitDataset.haversineMeters(origin.location, originStop.location) / 75).coerceIn(2, 8)
            val busDur = estimatedTransitMin
            val walkEnd = (IsraelTransitDataset.haversineMeters(destStop.location, destination.location) / 75).coerceIn(2, 8)
            val total = walkStart + busDur + walkEnd
            candidates += TripItinerary(
                id = "route_274_direct",
                totalDurationMinutes = total,
                departureTime = now.plusMinutes(2).format(timeFormatter),
                arrivalTime = now.plusMinutes(2L + total).format(timeFormatter),
                leaveInMinutes = 2,
                totalWalkMinutes = walkStart + walkEnd,
                transfersCount = 0,
                isWheelchairAccessible = true,
                legs = listOf(
                    TripLeg(
                        id = "leg_walk_1",
                        mode = TransitMode.WALK,
                        boardingStop = originStop,
                        durationMinutes = walkStart,
                        distanceMeters = walkStart * 75,
                        departureTime = now.plusMinutes(2).format(timeFormatter),
                        arrivalTime = now.plusMinutes(2L + walkStart).format(timeFormatter),
                        polyline = listOf(origin.location, originStop.location)
                    ),
                    TripLeg(
                        id = "leg_bus_274",
                        mode = TransitMode.BUS,
                        lineNumber = "274",
                        operator = TransitOperator.EGGED,
                        headsignEn = destination.nameEn,
                        headsignHe = destination.nameHe,
                        boardingStop = originStop,
                        alightingStop = destStop,
                        intermediateStops = listOf(midStop),
                        durationMinutes = busDur,
                        distanceMeters = totalMeters,
                        departureTime = now.plusMinutes(2L + walkStart).format(timeFormatter),
                        arrivalTime = now.plusMinutes(2L + walkStart + busDur).format(timeFormatter),
                        isLive = true,
                        polyline = listOf(originStop.location, midStop.location, destStop.location)
                    ),
                    TripLeg(
                        id = "leg_walk_2",
                        mode = TransitMode.WALK,
                        alightingStop = destStop,
                        durationMinutes = walkEnd,
                        distanceMeters = walkEnd * 75,
                        departureTime = now.plusMinutes(2L + walkStart + busDur).format(timeFormatter),
                        arrivalTime = now.plusMinutes(2L + total).format(timeFormatter),
                        polyline = listOf(destStop.location, destination.location)
                    )
                )
            )
        }

        // Route 2: Israel Railways + Dankal Light Rail R1 (multi-modal)
        if (TransitMode.RAIL in filter.modes || TransitMode.LIGHT_RAIL in filter.modes) {
            val walkStart = 4
            val railDur = 14
            val lrtDur = 7
            val walkEnd = 2
            val total = walkStart + railDur + lrtDur + walkEnd
            candidates += TripItinerary(
                id = "route_rail_lrt",
                totalDurationMinutes = total,
                departureTime = now.plusMinutes(4).format(timeFormatter),
                arrivalTime = now.plusMinutes(4L + total).format(timeFormatter),
                leaveInMinutes = 4,
                totalWalkMinutes = walkStart + walkEnd,
                transfersCount = 1,
                isWheelchairAccessible = true,
                legs = listOf(
                    TripLeg(
                        id = "leg_walk_rail",
                        mode = TransitMode.WALK,
                        boardingStop = IsraelTransitDataset.stops[1],
                        durationMinutes = walkStart,
                        distanceMeters = 310,
                        departureTime = now.plusMinutes(4).format(timeFormatter),
                        arrivalTime = now.plusMinutes(8).format(timeFormatter),
                        polyline = listOf(origin.location, IsraelTransitDataset.stops[1].location)
                    ),
                    TripLeg(
                        id = "leg_rail_ic",
                        mode = TransitMode.RAIL,
                        lineNumber = "IC",
                        operator = TransitOperator.ISRAEL_RAILWAYS,
                        headsignEn = "Tel Aviv HaShalom",
                        headsignHe = "תל אביב השלום",
                        boardingStop = IsraelTransitDataset.stops[1],
                        alightingStop = midStop,
                        intermediateStops = emptyList(),
                        durationMinutes = railDur,
                        distanceMeters = 11200,
                        departureTime = now.plusMinutes(8).format(timeFormatter),
                        arrivalTime = now.plusMinutes(22).format(timeFormatter),
                        isLive = true,
                        polyline = listOf(IsraelTransitDataset.stops[1].location, midStop.location)
                    ),
                    TripLeg(
                        id = "leg_lrt_r1",
                        mode = TransitMode.LIGHT_RAIL,
                        lineNumber = "R1",
                        operator = TransitOperator.DANKAL,
                        headsignEn = destination.nameEn,
                        headsignHe = destination.nameHe,
                        boardingStop = midStop,
                        alightingStop = destStop,
                        intermediateStops = emptyList(),
                        durationMinutes = lrtDur,
                        distanceMeters = 2400,
                        departureTime = now.plusMinutes(22).format(timeFormatter),
                        arrivalTime = now.plusMinutes(29).format(timeFormatter),
                        isLive = true,
                        polyline = listOf(midStop.location, destStop.location)
                    ),
                    TripLeg(
                        id = "leg_walk_end",
                        mode = TransitMode.WALK,
                        alightingStop = destStop,
                        durationMinutes = walkEnd,
                        distanceMeters = 140,
                        departureTime = now.plusMinutes(29).format(timeFormatter),
                        arrivalTime = now.plusMinutes(31).format(timeFormatter),
                        polyline = listOf(destStop.location, destination.location)
                    )
                )
            )
        }

        // Route 3: Dan Bus 63 + Egged 164 (Least walking)
        if (TransitMode.BUS in filter.modes) {
            val walkStart = 2
            val leg1Dur = 12
            val leg2Dur = 14
            val walkEnd = 2
            val total = walkStart + leg1Dur + leg2Dur + walkEnd
            candidates += TripItinerary(
                id = "route_63_164",
                totalDurationMinutes = total,
                departureTime = now.plusMinutes(5).format(timeFormatter),
                arrivalTime = now.plusMinutes(5L + total).format(timeFormatter),
                leaveInMinutes = 5,
                totalWalkMinutes = walkStart + walkEnd,
                transfersCount = 1,
                isWheelchairAccessible = false,
                legs = listOf(
                    TripLeg(
                        id = "leg_w1",
                        mode = TransitMode.WALK,
                        boardingStop = originStop,
                        durationMinutes = walkStart,
                        distanceMeters = 150,
                        departureTime = now.plusMinutes(5).format(timeFormatter),
                        arrivalTime = now.plusMinutes(7).format(timeFormatter),
                        polyline = listOf(origin.location, originStop.location)
                    ),
                    TripLeg(
                        id = "leg_63",
                        mode = TransitMode.BUS,
                        lineNumber = "63",
                        operator = TransitOperator.DAN,
                        headsignEn = "Azrieli Mall",
                        headsignHe = "קניון עזריאלי",
                        boardingStop = originStop,
                        alightingStop = midStop,
                        intermediateStops = emptyList(),
                        durationMinutes = leg1Dur,
                        distanceMeters = 3100,
                        departureTime = now.plusMinutes(7).format(timeFormatter),
                        arrivalTime = now.plusMinutes(19).format(timeFormatter),
                        isLive = true,
                        polyline = listOf(originStop.location, midStop.location)
                    ),
                    TripLeg(
                        id = "leg_164",
                        mode = TransitMode.BUS,
                        lineNumber = "164",
                        operator = TransitOperator.EGGED,
                        headsignEn = destination.nameEn,
                        headsignHe = destination.nameHe,
                        boardingStop = midStop,
                        alightingStop = destStop,
                        intermediateStops = emptyList(),
                        durationMinutes = leg2Dur,
                        distanceMeters = 4500,
                        departureTime = now.plusMinutes(19).format(timeFormatter),
                        arrivalTime = now.plusMinutes(33).format(timeFormatter),
                        isLive = false,
                        polyline = listOf(midStop.location, destStop.location)
                    ),
                    TripLeg(
                        id = "leg_w2",
                        mode = TransitMode.WALK,
                        alightingStop = destStop,
                        durationMinutes = walkEnd,
                        distanceMeters = 150,
                        departureTime = now.plusMinutes(33).format(timeFormatter),
                        arrivalTime = now.plusMinutes(35).format(timeFormatter),
                        polyline = listOf(destStop.location, destination.location)
                    )
                )
            )
        }

        val filtered = if (filter.optimization == RouteOptimization.ACCESSIBLE) {
            candidates.filter { it.isWheelchairAccessible }.ifEmpty { candidates }
        } else {
            candidates
        }

        return when (filter.optimization) {
            RouteOptimization.FASTEST -> filtered.sortedBy { it.totalDurationMinutes }
            RouteOptimization.LEAST_WALKING -> filtered.sortedBy { it.totalWalkMinutes }
            RouteOptimization.FEWEST_TRANSFERS -> filtered.sortedBy { it.transfersCount }
            RouteOptimization.ACCESSIBLE -> filtered.sortedBy { it.totalDurationMinutes }
        }
    }
}
