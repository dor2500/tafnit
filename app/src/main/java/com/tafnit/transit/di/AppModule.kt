package com.tafnit.transit.di

import android.content.Context
import androidx.room.Room
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.tafnit.transit.BuildConfig
import com.tafnit.transit.data.local.FavoriteLineDao
import com.tafnit.transit.data.local.FavoritePlaceDao
import com.tafnit.transit.data.local.FavoriteStopDao
import com.tafnit.transit.data.local.RecentSearchDao
import com.tafnit.transit.data.local.TafnitDatabase
import com.tafnit.transit.data.local.UserPreferencesRepositoryImpl
import com.tafnit.transit.data.remote.GitHubReleaseApiService
import com.tafnit.transit.data.remote.OpenBusApiService
import com.tafnit.transit.data.remote.PhotonApiService
import com.tafnit.transit.data.repository.LocationRepositoryImpl
import com.tafnit.transit.data.repository.PlacesRepositoryImpl
import com.tafnit.transit.data.repository.TransitRepositoryImpl
import com.tafnit.transit.data.repository.TripPlannerRepositoryImpl
import com.tafnit.transit.domain.repository.LocationRepository
import com.tafnit.transit.domain.repository.PlacesRepository
import com.tafnit.transit.domain.repository.TransitRepository
import com.tafnit.transit.domain.repository.TripPlannerRepository
import com.tafnit.transit.domain.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkAndDatabaseModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(12, TimeUnit.SECONDS)
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                }
            )
        }
        return builder.build()
    }

    @Provides
    @Singleton
    @Named("photon")
    fun providePhotonRetrofit(client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://photon.komoot.io/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    @Named("openbus")
    fun provideOpenBusRetrofit(client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://open-bus-stride-api.hasadna.org.il/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun providePhotonApi(@Named("photon") retrofit: Retrofit): PhotonApiService =
        retrofit.create(PhotonApiService::class.java)

    @Provides
    @Singleton
    fun provideOpenBusApi(@Named("openbus") retrofit: Retrofit): OpenBusApiService =
        retrofit.create(OpenBusApiService::class.java)

    @Provides
    @Singleton
    @Named("github")
    fun provideGitHubRetrofit(client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideGitHubReleaseApi(@Named("github") retrofit: Retrofit): GitHubReleaseApiService =
        retrofit.create(GitHubReleaseApiService::class.java)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TafnitDatabase =
        Room.databaseBuilder(context, TafnitDatabase::class.java, "tafnit_db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideFavoritePlaceDao(db: TafnitDatabase): FavoritePlaceDao = db.favoritePlaceDao()

    @Provides
    fun provideFavoriteStopDao(db: TafnitDatabase): FavoriteStopDao = db.favoriteStopDao()

    @Provides
    fun provideFavoriteLineDao(db: TafnitDatabase): FavoriteLineDao = db.favoriteLineDao()

    @Provides
    fun provideRecentSearchDao(db: TafnitDatabase): RecentSearchDao = db.recentSearchDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryBindingsModule {

    @Binds
    @Singleton
    abstract fun bindTransitRepository(impl: TransitRepositoryImpl): TransitRepository

    @Binds
    @Singleton
    abstract fun bindPlacesRepository(impl: PlacesRepositoryImpl): PlacesRepository

    @Binds
    @Singleton
    abstract fun bindTripPlannerRepository(impl: TripPlannerRepositoryImpl): TripPlannerRepository

    @Binds
    @Singleton
    abstract fun bindLocationRepository(impl: LocationRepositoryImpl): LocationRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(impl: UserPreferencesRepositoryImpl): UserPreferencesRepository
}
