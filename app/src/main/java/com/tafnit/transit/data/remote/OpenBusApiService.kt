package com.tafnit.transit.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

@Serializable
data class OpenBusStopDto(
    val id: Long = 0,
    val code: Int = 0,
    val name: String = "",
    val city: String? = null,
    val lat: Double = 0.0,
    val lon: Double = 0.0
)

@Serializable
data class OpenBusRouteDto(
    val id: Long = 0,
    @SerialName("line_ref") val lineRef: Int = 0,
    @SerialName("operator_ref") val operatorRef: Int = 0,
    @SerialName("route_short_name") val routeShortName: String? = null,
    @SerialName("route_long_name") val routeLongName: String? = null,
    @SerialName("route_type") val routeType: Int = 3
)

@Serializable
data class OpenBusVehicleLocationDto(
    val id: Long = 0,
    @SerialName("siri_route__line_ref") val lineRef: Int? = null,
    @SerialName("siri_route__operator_ref") val operatorRef: Int? = null,
    val lat: Double? = null,
    val lon: Double? = null,
    val bearing: Float? = null,
    val velocity: Int? = null
)

interface OpenBusApiService {
    @GET("gtfs_stops/list")
    suspend fun getNearbyStops(
        @Query("lat__greater_or_equal") minLat: Double,
        @Query("lat__lower_or_equal") maxLat: Double,
        @Query("lon__greater_or_equal") minLon: Double,
        @Query("lon__lower_or_equal") maxLon: Double,
        @Query("limit") limit: Int = 25
    ): List<OpenBusStopDto>

    @GET("gtfs_routes/list")
    suspend fun searchRoutes(
        @Query("route_short_name") shortName: String? = null,
        @Query("limit") limit: Int = 30
    ): List<OpenBusRouteDto>

    @GET("siri_vehicle_locations/list")
    suspend fun getLiveVehicles(
        @Query("lat__greater_or_equal") minLat: Double,
        @Query("lat__lower_or_equal") maxLat: Double,
        @Query("lon__greater_or_equal") minLon: Double,
        @Query("lon__lower_or_equal") maxLon: Double,
        @Query("limit") limit: Int = 30
    ): List<OpenBusVehicleLocationDto>
}
