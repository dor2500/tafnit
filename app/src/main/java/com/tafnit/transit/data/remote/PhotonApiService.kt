package com.tafnit.transit.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

@Serializable
data class PhotonResponse(
    val features: List<PhotonFeature> = emptyList()
)

@Serializable
data class PhotonFeature(
    val geometry: PhotonGeometry,
    val properties: PhotonProperties
)

@Serializable
data class PhotonGeometry(
    val coordinates: List<Double> // [lon, lat]
)

@Serializable
data class PhotonProperties(
    val name: String? = null,
    val street: String? = null,
    val housenumber: String? = null,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null,
    @SerialName("osm_value") val osmValue: String? = null,
    @SerialName("osm_key") val osmKey: String? = null
)

interface PhotonApiService {
    @GET("api/")
    suspend fun search(
        @Query("q") query: String,
        @Query("lat") lat: Double = 32.0853,
        @Query("lon") lon: Double = 34.7818,
        @Query("bbox") bbox: String = "34.2,29.4,35.9,33.4",
        @Query("limit") limit: Int = 10
    ): PhotonResponse

    @GET("reverse")
    suspend fun reverse(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): PhotonResponse
}
