package com.tafnit.transit.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LatLng(
    val latitude: Double,
    val longitude: Double
)

enum class TransitMode {
    BUS,
    RAIL,
    LIGHT_RAIL,
    SHUTTLE,
    WALK
}

enum class TransitOperator(
    val displayNameEn: String,
    val displayNameHe: String,
    val colorHex: Long
) {
    EGGED("Egged", "אגד", 0xFF007A3D),
    DAN("Dan", "דן", 0xFF0057B8),
    KAVIM("Kavim", "קווים", 0xFFE65100),
    METROPOLINE("Metropoline", "מטרופולין", 0xFF6A1B9A),
    ISRAEL_RAILWAYS("Israel Railways", "רכבת ישראל", 0xFF0D47A1),
    DANKAL("Dankal Light Rail", "דנקל - רכבת קלה", 0xFFC62828),
    KFIR("Kfir Light Rail", "כפיר - רכבת קלה", 0xFFAD1457),
    OTHER("Transit", "תחבורה ציבורית", 0xFF455A64)
}

enum class PlaceCategory {
    HOME,
    WORK,
    FAVORITE,
    STOP,
    LANDMARK,
    PARK,
    MALL,
    ADDRESS,
    TRANSIT_HUB
}

enum class AlertSeverity {
    INFO,
    WARNING,
    SEVERE
}

enum class RouteOptimization {
    FASTEST,
    LEAST_WALKING,
    FEWEST_TRANSFERS,
    ACCESSIBLE
}

enum class MapLayerStyle {
    STANDARD,
    TRANSIT,
    DARK
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class AppLanguage(val tag: String) {
    SYSTEM(""),
    HEBREW("iw"),
    ENGLISH("en")
}

sealed class TimeOption {
    data object LeaveNow : TimeOption()
    data class DepartAt(val epochMillis: Long) : TimeOption()
    data class ArriveBy(val epochMillis: Long) : TimeOption()
}

data class RouteFilter(
    val modes: Set<TransitMode> = setOf(
        TransitMode.BUS,
        TransitMode.RAIL,
        TransitMode.LIGHT_RAIL,
        TransitMode.SHUTTLE
    ),
    val optimization: RouteOptimization = RouteOptimization.FASTEST,
    val timeOption: TimeOption = TimeOption.LeaveNow,
    val maxWalkMeters: Int = 1200
)

data class LineArrival(
    val lineId: String,
    val lineNumber: String,
    val operator: TransitOperator,
    val mode: TransitMode,
    val headsignEn: String,
    val headsignHe: String,
    val etaMinutes: Int,
    val isLive: Boolean,
    val scheduledTime: String,
    val vehicleId: String? = null,
    val platform: String? = null
)

data class TransitStop(
    val id: String,
    val code: String, // 5-digit Israeli station code (מק"ט)
    val nameEn: String,
    val nameHe: String,
    val cityEn: String,
    val cityHe: String,
    val location: LatLng,
    val distanceMeters: Int = 0,
    val lines: List<String> = emptyList(),
    val upcomingArrivals: List<LineArrival> = emptyList(),
    val isFavorite: Boolean = false
) {
    fun localizedName(isHebrew: Boolean): String = if (isHebrew) nameHe else nameEn
    fun localizedCity(isHebrew: Boolean): String = if (isHebrew) cityHe else cityEn
    val walkMinutes: Int get() = (distanceMeters / 75).coerceAtLeast(1)
}

data class LineStopEntry(
    val stop: TransitStop,
    val sequence: Int,
    val etaMinutes: Int,
    val isLiveEta: Boolean,
    val isBusApproaching: Boolean = false
)

data class TransitLine(
    val id: String,
    val number: String,
    val operator: TransitOperator,
    val mode: TransitMode,
    val originEn: String,
    val originHe: String,
    val destinationEn: String,
    val destinationHe: String,
    val outboundStops: List<LineStopEntry> = emptyList(),
    val returnStops: List<LineStopEntry> = emptyList(),
    val outboundPolyline: List<LatLng> = emptyList(),
    val returnPolyline: List<LatLng> = emptyList(),
    val scheduleTimesOutbound: List<String> = emptyList(),
    val scheduleTimesReturn: List<String> = emptyList(),
    val hasActiveAlert: Boolean = false,
    val isFavorite: Boolean = false
) {
    fun localizedRouteTitle(isHebrew: Boolean): String =
        if (isHebrew) "$originHe ◄ $destinationHe" else "$originEn → $destinationEn"
}

data class LiveVehicle(
    val id: String,
    val lineId: String,
    val lineNumber: String,
    val operator: TransitOperator,
    val mode: TransitMode,
    val location: LatLng,
    val bearing: Float,
    val speedKmh: Int,
    val nextStopCode: String
)

data class PlaceResult(
    val id: String,
    val nameEn: String,
    val nameHe: String,
    val subtitleEn: String,
    val subtitleHe: String,
    val location: LatLng,
    val category: PlaceCategory,
    val stopCode: String? = null,
    val isFavorite: Boolean = false
) {
    fun localizedName(isHebrew: Boolean): String = if (isHebrew) nameHe else nameEn
    fun localizedSubtitle(isHebrew: Boolean): String = if (isHebrew) subtitleHe else subtitleEn
}

data class FavoritePlace(
    val id: String,
    val label: String,
    val iconKey: String, // "home", "work", "star", "school", "gym", "heart"
    val address: String,
    val location: LatLng,
    val isPinned: Boolean = false
)

data class ServiceAlert(
    val id: String,
    val titleEn: String,
    val titleHe: String,
    val descriptionEn: String,
    val descriptionHe: String,
    val severity: AlertSeverity,
    val affectedLineNumbers: List<String>,
    val affectedStopCodes: List<String>,
    val operator: TransitOperator,
    val updatedAt: String
) {
    fun localizedTitle(isHebrew: Boolean): String = if (isHebrew) titleHe else titleEn
    fun localizedDescription(isHebrew: Boolean): String = if (isHebrew) descriptionHe else descriptionEn
}

data class TripLeg(
    val id: String,
    val mode: TransitMode,
    val lineNumber: String? = null,
    val operator: TransitOperator? = null,
    val headsignEn: String? = null,
    val headsignHe: String? = null,
    val boardingStop: TransitStop? = null,
    val alightingStop: TransitStop? = null,
    val intermediateStops: List<TransitStop> = emptyList(),
    val durationMinutes: Int,
    val distanceMeters: Int = 0,
    val departureTime: String,
    val arrivalTime: String,
    val isLive: Boolean = false,
    val polyline: List<LatLng> = emptyList()
)

data class TripItinerary(
    val id: String,
    val totalDurationMinutes: Int,
    val departureTime: String,
    val arrivalTime: String,
    val leaveInMinutes: Int,
    val totalWalkMinutes: Int,
    val transfersCount: Int,
    val isWheelchairAccessible: Boolean,
    val legs: List<TripLeg>
)

enum class NavigationPhase {
    WALKING_TO_STOP,
    WAITING_AT_STOP,
    ON_BOARD,
    ARRIVED
}

data class ActiveNavigationState(
    val isActive: Boolean = false,
    val itinerary: TripItinerary? = null,
    val currentLegIndex: Int = 0,
    val phase: NavigationPhase = NavigationPhase.WALKING_TO_STOP,
    val stopsRemaining: Int = 0,
    val nextStopName: String = "",
    val alightingStopName: String = "",
    val etaMinutes: Int = 0,
    val isSimulating: Boolean = false,
    val alertStageTriggered: Int = 0 // 0=none, 2=2-stops, 1=1-stop, -1=arrived
)

data class UserPreferences(
    val language: AppLanguage = AppLanguage.SYSTEM,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val getOffAlertStopsBefore: Int = 2,
    val alertSound: Boolean = true,
    val alertVibration: Boolean = true,
    val lockScreenNotification: Boolean = true,
    val defaultModes: Set<TransitMode> = setOf(
        TransitMode.BUS,
        TransitMode.RAIL,
        TransitMode.LIGHT_RAIL,
        TransitMode.SHUTTLE
    ),
    val maxWalkMeters: Int = 1200,
    val preferAccessible: Boolean = false,
    val useLiveApi: Boolean = false // false = fast offline Israeli demo mode by default, toggleable to Live OpenBus + Photon
)
