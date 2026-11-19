package com.tafnit.transit.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.tafnit.transit.domain.model.ActiveNavigationState
import com.tafnit.transit.domain.model.NavigationPhase
import com.tafnit.transit.domain.model.TransitMode
import com.tafnit.transit.domain.model.TripItinerary
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LiveNavigationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _state = MutableStateFlow(ActiveNavigationState())
    val state: StateFlow<ActiveNavigationState> = _state.asStateFlow()

    fun startNavigation(itinerary: TripItinerary, simulate: Boolean = false) {
        val firstLeg = itinerary.legs.firstOrNull()
        val firstTransitLeg = itinerary.legs.firstOrNull { it.mode != TransitMode.WALK }
        val initialPhase = when (firstLeg?.mode) {
            TransitMode.WALK -> NavigationPhase.WALKING_TO_STOP
            else -> NavigationPhase.WAITING_AT_STOP
        }
        val stopsCount = (firstTransitLeg?.intermediateStops?.size ?: 1) + 2
        _state.value = ActiveNavigationState(
            isActive = true,
            itinerary = itinerary,
            currentLegIndex = 0,
            phase = initialPhase,
            stopsRemaining = stopsCount,
            nextStopName = firstTransitLeg?.boardingStop?.nameEn ?: "Next Stop",
            alightingStopName = firstTransitLeg?.alightingStop?.nameEn ?: "Destination",
            etaMinutes = itinerary.totalDurationMinutes,
            isSimulating = simulate,
            alertStageTriggered = 0
        )
        val intent = Intent(context, LiveNavigationService::class.java).apply {
            action = LiveNavigationService.ACTION_START
        }
        runCatching { ContextCompat.startForegroundService(context, intent) }
    }

    fun advanceSimulationStep() {
        val current = _state.value
        val itinerary = current.itinerary ?: return
        if (!current.isActive) return

        val transitLeg = itinerary.legs.getOrNull(current.currentLegIndex)
            ?.takeIf { it.mode != TransitMode.WALK }
            ?: itinerary.legs.firstOrNull { it.mode != TransitMode.WALK }

        when (current.phase) {
            NavigationPhase.WALKING_TO_STOP -> {
                _state.value = current.copy(
                    phase = NavigationPhase.WAITING_AT_STOP,
                    nextStopName = transitLeg?.boardingStop?.nameEn ?: current.nextStopName,
                    etaMinutes = (current.etaMinutes - 2).coerceAtLeast(1)
                )
            }
            NavigationPhase.WAITING_AT_STOP -> {
                val transitIndex = itinerary.legs.indexOfFirst { it.mode != TransitMode.WALK }
                    .coerceAtLeast(0)
                _state.value = current.copy(
                    currentLegIndex = transitIndex,
                    phase = NavigationPhase.ON_BOARD,
                    stopsRemaining = 3,
                    nextStopName = transitLeg?.intermediateStops?.firstOrNull()?.nameEn
                        ?: transitLeg?.alightingStop?.nameEn ?: "Next Stop",
                    alightingStopName = transitLeg?.alightingStop?.nameEn ?: "Destination",
                    etaMinutes = (current.etaMinutes - 3).coerceAtLeast(1),
                    alertStageTriggered = 0
                )
            }
            NavigationPhase.ON_BOARD -> {
                val nextStops = current.stopsRemaining - 1
                if (nextStops <= 0) {
                    _state.value = current.copy(
                        phase = NavigationPhase.ARRIVED,
                        stopsRemaining = 0,
                        etaMinutes = 0,
                        alertStageTriggered = -1
                    )
                } else {
                    val alertStage = when (nextStops) {
                        2 -> 2
                        1 -> 1
                        else -> 0
                    }
                    _state.value = current.copy(
                        stopsRemaining = nextStops,
                        nextStopName = if (nextStops == 1) current.alightingStopName else current.nextStopName,
                        etaMinutes = (nextStops * 3).coerceAtLeast(1),
                        alertStageTriggered = alertStage
                    )
                }
            }
            NavigationPhase.ARRIVED -> {
                stopNavigation()
            }
        }
    }

    fun toggleSimulation(enabled: Boolean) {
        _state.value = _state.value.copy(isSimulating = enabled)
    }

    fun stopNavigation() {
        _state.value = ActiveNavigationState()
        val intent = Intent(context, LiveNavigationService::class.java).apply {
            action = LiveNavigationService.ACTION_STOP
        }
        runCatching { context.startService(intent) }
    }
}
