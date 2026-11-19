package com.tafnit.transit.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.tafnit.transit.data.mock.IsraelTransitDataset
import com.tafnit.transit.domain.model.LatLng
import com.tafnit.transit.domain.repository.LocationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : LocationRepository {

    private val fusedClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    override fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    override fun observeUserLocation(): Flow<LatLng> = flow {
        emit(getLastKnownOrDefault())
    }

    @SuppressLint("MissingPermission")
    override suspend fun getLastKnownOrDefault(): LatLng {
        if (!hasLocationPermission()) {
            return IsraelTransitDataset.DEFAULT_CENTRAL_ISRAEL
        }
        return runCatching {
            val loc = fusedClient.lastLocation.await()
                ?: fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null).await()
            if (loc != null) LatLng(loc.latitude, loc.longitude)
            else IsraelTransitDataset.DEFAULT_CENTRAL_ISRAEL
        }.getOrDefault(IsraelTransitDataset.DEFAULT_CENTRAL_ISRAEL)
    }
}
