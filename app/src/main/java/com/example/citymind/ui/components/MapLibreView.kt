package com.example.citymind.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapView
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import kotlin.math.abs

@Composable
fun MapLibreView(
    modifier: Modifier = Modifier,
    latitude: Double,
    longitude: Double,
    onLocationChanged: (Double, Double) -> Unit,
    zoom: Double = 15.0
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }

    // Use a high-quality, high-contrast OSM style
    val styleUrl = "https://tiles.basemaps.cartocdn.com/gl/voyager-gl-style/style.json"

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        AndroidView(
            factory = {
                mapView.apply {
                    onCreate(null)
                    getMapAsync { map ->
                        mapLibreMap = map
                        map.setStyle(styleUrl) { style ->
                            val position = CameraPosition.Builder()
                                .target(LatLng(latitude, longitude))
                                .zoom(zoom)
                                .build()
                            map.moveCamera(CameraUpdateFactory.newCameraPosition(position))

                            // Update location when user finishes moving the map
                            map.addOnCameraIdleListener {
                                map.cameraPosition.target?.let { target ->
                                    onLocationChanged(target.latitude, target.longitude)
                                }
                            }
                        }
                    }
                }
            },
            modifier = Modifier.matchParentSize(),
            update = {
                // If coordinates change externally (e.g. Current GPS button), move the camera
                mapLibreMap?.let { map ->
                    val currentTarget = map.cameraPosition.target
                    if (currentTarget != null && (abs(currentTarget.latitude - latitude) > 0.0001 || 
                        abs(currentTarget.longitude - longitude) > 0.0001)) {
                        map.animateCamera(CameraUpdateFactory.newLatLng(LatLng(latitude, longitude)))
                    }
                }
            }
        )

        // Fixed central marker for precise targeting ("mark the pointer own")
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Center Marker",
            tint = Color.Red,
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.Center)
                .graphicsLayer {
                    // Offset to make the tip of the pin point to the center
                    translationY = -60f // Adjusting to visually center the tip
                }
        )
    }

    // Handle lifecycle
    DisposableEffect(mapView) {
        onDispose {
            mapView.onStop()
            mapView.onDestroy()
        }
    }
}
