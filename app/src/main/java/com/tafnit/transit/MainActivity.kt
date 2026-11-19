package com.tafnit.transit

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.tafnit.transit.domain.model.PlaceCategory
import com.tafnit.transit.domain.model.PlaceResult
import com.tafnit.transit.domain.model.UserPreferences
import com.tafnit.transit.domain.repository.UserPreferencesRepository
import com.tafnit.transit.ui.alerts.ServiceAlertsScreen
import com.tafnit.transit.ui.home.HomeScreen
import com.tafnit.transit.ui.lines.LineDetailScreen
import com.tafnit.transit.ui.lines.LinesScreen
import com.tafnit.transit.ui.navigation.LiveNavigationScreen
import com.tafnit.transit.ui.planner.ItineraryDetailScreen
import com.tafnit.transit.ui.planner.TripPlannerScreen
import com.tafnit.transit.ui.planner.TripPlannerViewModel
import com.tafnit.transit.ui.search.MapPickerScreen
import com.tafnit.transit.ui.search.SearchScreen
import com.tafnit.transit.ui.settings.SettingsScreen
import com.tafnit.transit.ui.stations.StationDetailScreen
import com.tafnit.transit.ui.stations.StationsScreen
import com.tafnit.transit.ui.theme.TafnitTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable object DirectionsRoute
@Serializable object StationsRoute
@Serializable object LinesRoute
@Serializable object SearchRoute
@Serializable object MapPickerRoute
@Serializable object TripPlannerRoute
@Serializable object ItineraryDetailRoute
@Serializable object LiveNavigationRoute
@Serializable object ServiceAlertsRoute
@Serializable object SettingsRoute
@Serializable data class StationDetailRoute(val stopCode: String)
@Serializable data class LineDetailRoute(val lineId: String)

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var preferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val prefs by preferencesRepository.preferences
                .collectAsStateWithLifecycle(initialValue = UserPreferences())

            TafnitTheme(
                themeMode = prefs.themeMode,
                dynamicColor = prefs.dynamicColor
            ) {
                TafnitAppNavHost()
            }
        }
    }
}

@Composable
fun TafnitAppNavHost() {
    val navController = rememberNavController()
    val plannerViewModel: TripPlannerViewModel = hiltViewModel()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val topLevelRoutes = setOf(
        DirectionsRoute::class.qualifiedName,
        StationsRoute::class.qualifiedName,
        LinesRoute::class.qualifiedName
    )
    val showBottomBar = currentRoute in topLevelRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == DirectionsRoute::class.qualifiedName,
                        onClick = {
                            navController.navigate(DirectionsRoute) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Rounded.Directions, contentDescription = null) },
                        label = { Text(stringResource(R.string.tab_directions)) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == StationsRoute::class.qualifiedName,
                        onClick = {
                            navController.navigate(StationsRoute) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Rounded.Place, contentDescription = null) },
                        label = { Text(stringResource(R.string.tab_stations)) }
                    )
                    NavigationBarItem(
                        selected = currentRoute == LinesRoute::class.qualifiedName,
                        onClick = {
                            navController.navigate(LinesRoute) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(Icons.Rounded.DirectionsBus, contentDescription = null) },
                        label = { Text(stringResource(R.string.tab_lines)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            NavHost(
                navController = navController,
                startDestination = DirectionsRoute,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() }
            ) {
                composable<DirectionsRoute> {
                    HomeScreen(
                        onOpenSearch = { navController.navigate(SearchRoute) },
                        onOpenAlerts = { navController.navigate(ServiceAlertsRoute) },
                        onOpenSettings = { navController.navigate(SettingsRoute) },
                        onOpenFavoriteTrip = { fav ->
                            plannerViewModel.setDestination(
                                PlaceResult(
                                    id = fav.id,
                                    nameEn = fav.label,
                                    nameHe = fav.label,
                                    subtitleEn = fav.address,
                                    subtitleHe = fav.address,
                                    location = fav.location,
                                    category = PlaceCategory.FAVORITE
                                )
                            )
                            navController.navigate(TripPlannerRoute)
                        },
                        onOpenStopDetails = { stopCode ->
                            navController.navigate(StationDetailRoute(stopCode))
                        }
                    )
                }

                composable<StationsRoute> {
                    StationsScreen(
                        onOpenStopDetails = { stopCode ->
                            navController.navigate(StationDetailRoute(stopCode))
                        }
                    )
                }

                composable<LinesRoute> {
                    LinesScreen(
                        onOpenLineDetails = { lineId ->
                            navController.navigate(LineDetailRoute(lineId))
                        }
                    )
                }

                composable<SearchRoute> {
                    SearchScreen(
                        onBack = { navController.popBackStack() },
                        onSearchLineClick = {
                            navController.navigate(LinesRoute) {
                                popUpTo(navController.graph.findStartDestination().id)
                            }
                        },
                        onSelectDestination = { place ->
                            plannerViewModel.setDestination(place)
                            navController.navigate(TripPlannerRoute)
                        },
                        onPickOnMap = { navController.navigate(MapPickerRoute) }
                    )
                }

                composable<MapPickerRoute> {
                    MapPickerScreen(
                        onBack = { navController.popBackStack() },
                        onConfirmPlace = { place ->
                            plannerViewModel.setDestination(place)
                            navController.navigate(TripPlannerRoute)
                        }
                    )
                }

                composable<TripPlannerRoute> {
                    TripPlannerScreen(
                        viewModel = plannerViewModel,
                        onBack = { navController.popBackStack() },
                        onOpenSearchForDest = { navController.navigate(SearchRoute) },
                        onOpenItinerary = { navController.navigate(ItineraryDetailRoute) }
                    )
                }

                composable<ItineraryDetailRoute> {
                    ItineraryDetailScreen(
                        viewModel = plannerViewModel,
                        onBack = { navController.popBackStack() },
                        onStartLiveNavigation = { navController.navigate(LiveNavigationRoute) }
                    )
                }

                composable<LiveNavigationRoute> {
                    LiveNavigationScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                composable<StationDetailRoute> { entry ->
                    val route = entry.toRoute<StationDetailRoute>()
                    StationDetailScreen(
                        stopCode = route.stopCode,
                        onBack = { navController.popBackStack() },
                        onNavigateToStop = { stop ->
                            plannerViewModel.setDestination(
                                PlaceResult(
                                    id = stop.id,
                                    nameEn = stop.nameEn,
                                    nameHe = stop.nameHe,
                                    subtitleEn = stop.cityEn,
                                    subtitleHe = stop.cityHe,
                                    location = stop.location,
                                    category = PlaceCategory.STOP,
                                    stopCode = stop.code
                                )
                            )
                            navController.navigate(TripPlannerRoute)
                        },
                        onOpenLineDetails = { lineId ->
                            navController.navigate(LineDetailRoute(lineId))
                        }
                    )
                }

                composable<LineDetailRoute> { entry ->
                    val route = entry.toRoute<LineDetailRoute>()
                    LineDetailScreen(
                        lineId = route.lineId,
                        onBack = { navController.popBackStack() },
                        onOpenStopDetails = { stopCode ->
                            navController.navigate(StationDetailRoute(stopCode))
                        },
                        onOpenAlerts = { navController.navigate(ServiceAlertsRoute) }
                    )
                }

                composable<ServiceAlertsRoute> {
                    ServiceAlertsScreen(onBack = { navController.popBackStack() })
                }

                composable<SettingsRoute> {
                    SettingsScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}
