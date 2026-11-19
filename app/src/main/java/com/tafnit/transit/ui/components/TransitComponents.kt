package com.tafnit.transit.ui.components

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Build
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Park
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Train
import androidx.compose.material.icons.rounded.Tram
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.tafnit.transit.R
import com.tafnit.transit.domain.model.LatLng
import com.tafnit.transit.domain.model.LiveVehicle
import com.tafnit.transit.domain.model.MapLayerStyle
import com.tafnit.transit.domain.model.PlaceCategory
import com.tafnit.transit.domain.model.TransitMode
import com.tafnit.transit.domain.model.TransitOperator
import com.tafnit.transit.domain.model.TransitStop
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@Composable
fun LivePulseDot(
    color: Color = MaterialTheme.colorScheme.tertiary,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "live_pulse")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    Box(
        modifier = modifier
            .size(8.dp)
            .alpha(alpha)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
fun LineBadgePill(
    lineNumber: String,
    operator: TransitOperator,
    mode: TransitMode = TransitMode.BUS,
    isLive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val badgeColor = Color(operator.colorHex)
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = badgeColor.copy(alpha = 0.16f),
        contentColor = badgeColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = modeIcon(mode),
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = lineNumber,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            if (isLive) {
                LivePulseDot(color = MaterialTheme.colorScheme.tertiary)
            }
        }
    }
}

@Composable
fun EtaPill(
    etaMinutes: Int,
    isLive: Boolean,
    modifier: Modifier = Modifier
) {
    val container = if (isLive) {
        MaterialTheme.colorScheme.tertiaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val content = if (isLive) {
        MaterialTheme.colorScheme.onTertiaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = container,
        contentColor = content
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLive) {
                LivePulseDot(color = content)
                Spacer(modifier = Modifier.width(5.dp))
            }
            Text(
                text = stringResource(R.string.min_away, etaMinutes),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

fun modeIcon(mode: TransitMode): ImageVector = when (mode) {
    TransitMode.BUS -> Icons.Rounded.DirectionsBus
    TransitMode.RAIL -> Icons.Rounded.Train
    TransitMode.LIGHT_RAIL -> Icons.Rounded.Tram
    TransitMode.SHUTTLE -> Icons.Rounded.DirectionsBus
    TransitMode.WALK -> Icons.Rounded.DirectionsWalk
}

fun placeCategoryIcon(category: PlaceCategory): ImageVector = when (category) {
    PlaceCategory.HOME -> Icons.Rounded.Home
    PlaceCategory.WORK -> Icons.Rounded.Work
    PlaceCategory.FAVORITE -> Icons.Rounded.Star
    PlaceCategory.STOP -> Icons.Rounded.DirectionsBus
    PlaceCategory.LANDMARK -> Icons.Rounded.PhotoCamera
    PlaceCategory.PARK -> Icons.Rounded.Park
    PlaceCategory.MALL -> Icons.Rounded.ShoppingBag
    PlaceCategory.ADDRESS -> Icons.Rounded.LocationOn
    PlaceCategory.TRANSIT_HUB -> Icons.Rounded.Train
}

fun favoriteIconByKey(key: String): ImageVector = when (key) {
    "home" -> Icons.Rounded.Home
    "work" -> Icons.Rounded.Work
    "school" -> Icons.Rounded.School
    "gym" -> Icons.Rounded.FitnessCenter
    "heart" -> Icons.Rounded.Favorite
    else -> Icons.Rounded.Star
}

@Composable
fun TransitMapCanvas(
    center: LatLng,
    stops: List<TransitStop> = emptyList(),
    vehicles: List<LiveVehicle> = emptyList(),
    polylines: List<Pair<List<LatLng>, Boolean>> = emptyList(), // (points, isDashedWalk)
    layerStyle: MapLayerStyle = MapLayerStyle.STANDARD,
    zoomLevel: Double = 14.5,
    onStopClick: ((TransitStop) -> Unit)? = null,
    onMapCenterChanged: ((LatLng) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme() || layerStyle == MapLayerStyle.DARK
    val isPreviewOrRobolectric = LocalInspectionMode.current ||
        Build.FINGERPRINT.equals("robolectric", ignoreCase = true)

    if (isPreviewOrRobolectric) {
        SimulatedVectorMapCanvas(
            center = center,
            stops = stops,
            vehicles = vehicles,
            polylines = polylines,
            isDark = isDark,
            modifier = modifier
        )
        return
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val walkColor = MaterialTheme.colorScheme.secondary

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(zoomLevel)
                controller.setCenter(GeoPoint(center.latitude, center.longitude))
            }
        },
        update = { mapView ->
            mapView.controller.setCenter(GeoPoint(center.latitude, center.longitude))
            if (isDark) {
                val invertMatrix = ColorMatrix(
                    floatArrayOf(
                        -1f, 0f, 0f, 0f, 255f,
                        0f, -1f, 0f, 0f, 255f,
                        0f, 0f, -1f, 0f, 255f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                mapView.overlayManager.tilesOverlay.setColorFilter(ColorMatrixColorFilter(invertMatrix))
            } else {
                mapView.overlayManager.tilesOverlay.setColorFilter(null)
            }

            mapView.overlays.clear()

            polylines.forEach { (points, isWalk) ->
                if (points.size >= 2) {
                    val line = Polyline().apply {
                        setPoints(points.map { GeoPoint(it.latitude, it.longitude) })
                        outlinePaint.color = if (isWalk) {
                            android.graphics.Color.rgb(
                                (walkColor.red * 255).toInt(),
                                (walkColor.green * 255).toInt(),
                                (walkColor.blue * 255).toInt()
                            )
                        } else {
                            android.graphics.Color.rgb(
                                (primaryColor.red * 255).toInt(),
                                (primaryColor.green * 255).toInt(),
                                (primaryColor.blue * 255).toInt()
                            )
                        }
                        outlinePaint.strokeWidth = if (isWalk) 7f else 11f
                    }
                    mapView.overlays.add(line)
                }
            }

            stops.forEach { stop ->
                val marker = Marker(mapView).apply {
                    position = GeoPoint(stop.location.latitude, stop.location.longitude)
                    title = "${stop.nameEn} (${stop.code})"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    setOnMarkerClickListener { _, _ ->
                        onStopClick?.invoke(stop)
                        true
                    }
                }
                mapView.overlays.add(marker)
            }

            vehicles.forEach { vehicle ->
                val marker = Marker(mapView).apply {
                    position = GeoPoint(vehicle.location.latitude, vehicle.location.longitude)
                    title = "Line ${vehicle.lineNumber}"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                }
                mapView.overlays.add(marker)
            }

            onMapCenterChanged?.let { callback ->
                val mapCenter = mapView.mapCenter
                callback(LatLng(mapCenter.latitude, mapCenter.longitude))
            }

            mapView.invalidate()
        }
    )
}

@Composable
private fun SimulatedVectorMapCanvas(
    center: LatLng,
    stops: List<TransitStop>,
    vehicles: List<LiveVehicle>,
    polylines: List<Pair<List<LatLng>, Boolean>>,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val bg = if (isDark) Color(0xFF151921) else Color(0xFFEAF0F6)
    val roadMajor = if (isDark) Color(0xFF28303E) else Color(0xFFFFFFFF)
    val roadMinor = if (isDark) Color(0xFF1F2530) else Color(0xFFF4F6F9)
    val parkColor = if (isDark) Color(0xFF1B3126) else Color(0xFFCDE9D4)
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary

    Box(modifier = modifier.background(bg)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Park blocks
            drawRect(
                color = parkColor,
                topLeft = Offset(w * 0.08f, h * 0.14f),
                size = androidx.compose.ui.geometry.Size(w * 0.24f, h * 0.18f)
            )
            drawRect(
                color = parkColor,
                topLeft = Offset(w * 0.68f, h * 0.56f),
                size = androidx.compose.ui.geometry.Size(w * 0.22f, h * 0.16f)
            )

            // Minor street grid
            for (i in 1..7) {
                val y = h * (i / 8f)
                drawLine(roadMinor, Offset(0f, y), Offset(w, y), strokeWidth = 4f)
            }
            for (j in 1..5) {
                val x = w * (j / 6f)
                drawLine(roadMinor, Offset(x, 0f), Offset(x, h), strokeWidth = 4f)
            }

            // Major arteries (Ayalon / Begin / Herzl style)
            drawLine(roadMajor, Offset(0f, h * 0.38f), Offset(w, h * 0.34f), strokeWidth = 14f)
            drawLine(roadMajor, Offset(w * 0.48f, 0f), Offset(w * 0.56f, h), strokeWidth = 16f)
            drawLine(roadMajor, Offset(w * 0.12f, h * 0.85f), Offset(w * 0.88f, h * 0.18f), strokeWidth = 10f)

            fun toScreen(point: LatLng): Offset {
                val dx = ((point.longitude - center.longitude) * 4200f).toFloat()
                val dy = ((center.latitude - point.latitude) * 4200f).toFloat()
                return Offset(
                    x = (w / 2f + dx).coerceIn(24f, w - 24f),
                    y = (h / 2f + dy).coerceIn(24f, h - 24f)
                )
            }

            // Polylines
            polylines.forEach { (pts, isWalk) ->
                if (pts.size >= 2) {
                    val pathEffect = if (isWalk) PathEffect.dashPathEffect(floatArrayOf(14f, 10f)) else null
                    for (idx in 0 until pts.lastIndex) {
                        drawLine(
                            color = if (isWalk) secondary else primary,
                            start = toScreen(pts[idx]),
                            end = toScreen(pts[idx + 1]),
                            strokeWidth = if (isWalk) 7f else 11f,
                            cap = StrokeCap.Round,
                            pathEffect = pathEffect
                        )
                    }
                }
            }

            // Stop pins
            stops.take(6).forEach { stop ->
                val pt = toScreen(stop.location)
                drawCircle(color = Color.White, radius = 14f, center = pt)
                drawCircle(color = primary, radius = 10f, center = pt)
            }

            // Live vehicles
            vehicles.take(5).forEach { v ->
                val pt = toScreen(v.location)
                drawCircle(color = Color.White, radius = 16f, center = pt)
                drawCircle(color = tertiary, radius = 12f, center = pt)
            }

            // User location puck
            val userPt = Offset(w / 2f, h / 2f)
            drawCircle(color = primary.copy(alpha = 0.22f), radius = 34f, center = userPt)
            drawCircle(color = Color.White, radius = 14f, center = userPt)
            drawCircle(color = primary, radius = 10f, center = userPt)
        }
    }
}
