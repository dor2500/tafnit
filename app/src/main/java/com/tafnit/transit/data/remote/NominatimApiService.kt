package com.tafnit.transit.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

@Serializable
data class NominatimPlaceDto(
    @SerialName("place_id") val placeId: Long = 0,
    val lat: String = "0",
    val lon: String = "0",
    @SerialName("display_name") val displayName: String = "",
    val name: String? = null
)

interface NominatimApiService {
    @Headers("User-Agent: Tafnit-Android/1.2 (public-transit-israel)")
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("format") format: String = "jsonv2",
        @Query("countrycodes") countryCodes: String = "il",
        @Query("accept-language") acceptLanguage: String = "he,en",
        @Query("limit") limit: Int = 8
    ): List<NominatimPlaceDto>
}
