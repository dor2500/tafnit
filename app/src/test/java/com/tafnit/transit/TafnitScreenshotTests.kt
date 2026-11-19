package com.tafnit.transit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import com.tafnit.transit.data.mock.IsraelTransitDataset
import com.tafnit.transit.data.repository.TripPlannerRepositoryImpl
import com.tafnit.transit.domain.model.ActiveNavigationState
import com.tafnit.transit.domain.model.MapLayerStyle
import com.tafnit.transit.domain.model.NavigationPhase
import com.tafnit.transit.domain.model.PlaceCategory
import com.tafnit.transit.domain.model.PlaceResult
import com.tafnit.transit.domain.model.RouteFilter
import com.tafnit.transit.domain.model.ThemeMode
import com.tafnit.transit.domain.model.TransitOperator
import com.tafnit.transit.ui.components.EtaPill
import com.tafnit.transit.ui.components.LineBadgePill
import com.tafnit.transit.ui.home.HomeScreenContent
import com.tafnit.transit.ui.home.HomeUiState
import com.tafnit.transit.ui.lines.LineDetailContent
import com.tafnit.transit.ui.lines.LinesUiState
import com.tafnit.transit.ui.navigation.LiveNavigationContent
import com.tafnit.transit.ui.planner.TripPlannerContent
import com.tafnit.transit.ui.planner.TripPlannerUiState
import com.tafnit.transit.ui.search.SearchScreenContent
import com.tafnit.transit.ui.search.SearchUiState
import com.tafnit.transit.ui.theme.TafnitTheme
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class TafnitScreenshotTests {

    @get:Rule
    val composeRule = createComposeRule()

    private fun screenshotPath(name: String) = "../docs/screenshots/$name.png"

    private fun sampleHomeState(layer: MapLayerStyle) = HomeUiState(
        userLocation = IsraelTransitDataset.DEFAULT_CENTRAL_ISRAEL,
        mapCenter = IsraelTransitDataset.DEFAULT_CENTRAL_ISRAEL,
        nearbyStops = IsraelTransitDataset.stops,
        liveVehicles = IsraelTransitDataset.liveVehicles,
        favorites = IsraelTransitDataset.defaultFavorites,
        alerts = IsraelTransitDataset.serviceAlerts,
        mapLayer = layer
    )

    @Test
    @Config(qualifiers = "en-rUS-w412dp-h915dp-xxhdpi")
    fun capture01_homeMapDark() {
        composeRule.setContent {
            TafnitTheme(themeMode = ThemeMode.DARK, dynamicColor = false) {
                Scaffold(
                    bottomBar = {
                        PreviewBottomNav()
                    }
                ) { padding ->
                    Box(modifier = Modifier.padding(bottom = padding.calculateBottomPadding())) {
                        HomeScreenContent(
                            state = sampleHomeState(MapLayerStyle.DARK),
                            onOpenSearch = {},
                            onOpenAlerts = {},
                            onOpenSettings = {},
                            onOpenFavoriteTrip = {},
                            onOpenStopDetails = {},
                            onRecenter = {},
                            onCycleLayer = {},
                            onToggleNearbySheet = {}
                        )
                    }
                }
            }
        }
        composeRule.onRoot().captureRoboImage(screenshotPath("01_home_map_dark"))
    }

    @Test
    @Config(qualifiers = "en-rUS-w412dp-h915dp-xxhdpi")
    fun capture02_homeMapLight() {
        composeRule.setContent {
            TafnitTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                Scaffold(
                    bottomBar = {
                        PreviewBottomNav()
                    }
                ) { padding ->
                    Box(modifier = Modifier.padding(bottom = padding.calculateBottomPadding())) {
                        HomeScreenContent(
                            state = sampleHomeState(MapLayerStyle.STANDARD),
                            onOpenSearch = {},
                            onOpenAlerts = {},
                            onOpenSettings = {},
                            onOpenFavoriteTrip = {},
                            onOpenStopDetails = {},
                            onRecenter = {},
                            onCycleLayer = {},
                            onToggleNearbySheet = {}
                        )
                    }
                }
            }
        }
        composeRule.onRoot().captureRoboImage(screenshotPath("02_home_map_light"))
    }

    @Test
    @Config(qualifiers = "iw-rIL-w412dp-h915dp-xxhdpi")
    fun capture03_searchFavoritesHe() {
        composeRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                TafnitTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                    SearchScreenContent(
                        state = SearchUiState(
                            favorites = IsraelTransitDataset.defaultFavorites,
                            recentSearches = IsraelTransitDataset.landmarks.take(4)
                        ),
                        isHebrew = true,
                        onBack = {},
                        onQueryChange = {},
                        onSearchLineClick = {},
                        onSelectPlace = {},
                        onSelectFavorite = {},
                        onTogglePlaceFavorite = {},
                        onSaveFavorite = { _, _, _, _ -> },
                        onDeleteFavorite = {},
                        onPickOnMap = {}
                    )
                }
            }
        }
        composeRule.onRoot().captureRoboImage(screenshotPath("03_search_favorites_he"))
    }

    @Test
    @Config(qualifiers = "en-rUS-w412dp-h915dp-xxhdpi")
    fun capture04_searchFavoritesEn() {
        composeRule.setContent {
            TafnitTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                SearchScreenContent(
                    state = SearchUiState(
                        favorites = IsraelTransitDataset.defaultFavorites,
                        recentSearches = IsraelTransitDataset.landmarks.take(4)
                    ),
                    isHebrew = false,
                    onBack = {},
                    onQueryChange = {},
                    onSearchLineClick = {},
                    onSelectPlace = {},
                    onSelectFavorite = {},
                    onTogglePlaceFavorite = {},
                    onSaveFavorite = { _, _, _, _ -> },
                    onDeleteFavorite = {},
                    onPickOnMap = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage(screenshotPath("04_search_favorites_en"))
    }

    @Test
    @Config(qualifiers = "en-rUS-w412dp-h915dp-xxhdpi")
    fun capture05_tripPlannerRoutes() {
        val origin = PlaceResult(
            id = "o1",
            nameEn = "Current Location",
            nameHe = "המיקום הנוכחי",
            subtitleEn = "Rishon LeZion",
            subtitleHe = "ראשון לציון",
            location = IsraelTransitDataset.DEFAULT_CENTRAL_ISRAEL,
            category = PlaceCategory.ADDRESS
        )
        val dest = IsraelTransitDataset.landmarks.first()
        val itineraries = runBlocking {
            TripPlannerRepositoryImpl().planTrips(origin, dest, RouteFilter())
        }
        composeRule.setContent {
            TafnitTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                TripPlannerContent(
                    state = TripPlannerUiState(
                        origin = origin,
                        destination = dest,
                        itineraries = itineraries,
                        selectedItinerary = itineraries.firstOrNull()
                    ),
                    isHebrew = false,
                    onBack = {},
                    onSwap = {},
                    onEditDestination = {},
                    onApplyFilter = { _, _ -> },
                    onTimeOptionChange = {},
                    onSelectItinerary = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage(screenshotPath("05_trip_planner_routes"))
    }

    @Test
    @Config(qualifiers = "en-rUS-w412dp-h915dp-xxhdpi")
    fun capture06_liveNavigationNotification() {
        val origin = PlaceResult(
            id = "o1",
            nameEn = "Current Location",
            nameHe = "המיקום הנוכחי",
            subtitleEn = "Tel Aviv",
            subtitleHe = "תל אביב",
            location = IsraelTransitDataset.DEFAULT_CENTRAL_ISRAEL,
            category = PlaceCategory.ADDRESS
        )
        val dest = IsraelTransitDataset.landmarks.first()
        val itineraries = runBlocking {
            TripPlannerRepositoryImpl().planTrips(origin, dest, RouteFilter())
        }
        val navState = ActiveNavigationState(
            isActive = true,
            itinerary = itineraries.first(),
            currentLegIndex = 1,
            phase = NavigationPhase.ON_BOARD,
            stopsRemaining = 2,
            nextStopName = "Habima Square / Rothschild",
            alightingStopName = "Azrieli Mall / Menachem Begin",
            etaMinutes = 6,
            isSimulating = true,
            alertStageTriggered = 2
        )
        composeRule.setContent {
            TafnitTheme(themeMode = ThemeMode.DARK, dynamicColor = false) {
                Box(modifier = Modifier.fillMaxSize()) {
                    LiveNavigationContent(
                        state = navState,
                        onBack = {},
                        onAdvanceStep = {},
                        onToggleSim = {},
                        onStopNavigation = {}
                    )
                    // Lock-screen Ongoing Notification Preview Overlay at top
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 18.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        shadowElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Navigation,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Tafnit • Live Navigation",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "now",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "On board Line 274 • 2 stops remaining",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Next stop: Habima Square • 6 min",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            LinearProgressIndicator(
                                progress = { 0.72f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                        }
                    }
                }
            }
        }
        composeRule.onRoot().captureRoboImage(screenshotPath("06_live_navigation_notification"))
    }

    @Test
    @Config(qualifiers = "en-rUS-w412dp-h915dp-xxhdpi")
    fun capture07_lineDetailsTimeline() {
        val line = IsraelTransitDataset.lines.first().copy(isFavorite = true)
        composeRule.setContent {
            TafnitTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                LineDetailContent(
                    line = line,
                    state = LinesUiState(
                        allLines = IsraelTransitDataset.lines,
                        selectedLine = line,
                        liveVehicles = IsraelTransitDataset.liveVehicles
                    ),
                    isHebrew = false,
                    onBack = {},
                    onToggleFavorite = {},
                    onOpenStopDetails = {},
                    onOpenAlerts = {}
                )
            }
        }
        composeRule.onRoot().captureRoboImage(screenshotPath("07_line_details_timeline"))
    }

    @Test
    @Config(qualifiers = "en-rUS-w412dp-h915dp-xxhdpi")
    fun capture08_widgetsPreview() {
        composeRule.setContent {
            TafnitTheme(themeMode = ThemeMode.LIGHT, dynamicColor = false) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFFD3E3FD), Color(0xFF9EF2E4), Color(0xFFF3F4F8))
                            )
                        )
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        // Widget 1: TafnitNearbyWidget Preview
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(26.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 6.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Azrieli Mall / Menachem Begin",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = "#20145 • Updated 18:24",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Text(
                                            text = "Refresh",
                                            style = MaterialTheme.typography.labelMedium,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                                IsraelTransitDataset.stops.first().upcomingArrivals.take(3).forEach { arr ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        LineBadgePill(arr.lineNumber, arr.operator, arr.mode, arr.isLive)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = arr.headsignEn,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        EtaPill(arr.etaMinutes, arr.isLive)
                                    }
                                }
                            }
                        }

                        // Widget 2: TafnitCommuteWidget Preview
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(26.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 6.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Tafnit Quick Commute",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Rounded.Home, contentDescription = null)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Home", fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Rounded.Work, contentDescription = null)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Work", fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            LineBadgePill("274", TransitOperator.EGGED, isLive = true)
                                            EtaPill(3, true)
                                        }
                                    }
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            LineBadgePill("R1", TransitOperator.DANKAL, isLive = true)
                                            EtaPill(6, true)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        composeRule.onRoot().captureRoboImage(screenshotPath("08_widgets_preview"))
    }
}

@Composable
private fun PreviewBottomNav() {
    NavigationBar {
        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = { Icon(Icons.Rounded.Directions, contentDescription = null) },
            label = { Text(stringResource(R.string.tab_directions)) }
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Rounded.Place, contentDescription = null) },
            label = { Text(stringResource(R.string.tab_stations)) }
        )
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Rounded.DirectionsBus, contentDescription = null) },
            label = { Text(stringResource(R.string.tab_lines)) }
        )
    }
}
