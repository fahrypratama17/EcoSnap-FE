
package com.example.ecosnap.features.map.components

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ecosnap.features.map.model.WastePartner
import com.google.gson.JsonObject
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point

private const val PARTNER_SOURCE = "ecosnap-partners"
private const val SELECTED_SOURCE = "ecosnap-selected"
private const val PARTNER_LAYER = "ecosnap-partner-dots"
private const val SELECTED_LAYER = "ecosnap-selected-dot"

private const val OSM_STYLE = """
{
  "version": 8,
  "name": "EcoSnap OpenStreetMap",
  "sources": {
    "osm": {
      "type": "raster",
      "tiles": [
        "https://tile.openstreetmap.org/{z}/{x}/{y}.png"
      ],
      "tileSize": 256,
      "minzoom": 0,
      "maxzoom": 19,
      "attribution": "© OpenStreetMap contributors"
    }
  },
  "layers": [
    {
      "id": "osm-base",
      "type": "raster",
      "source": "osm"
    }
  ]
}
"""

private fun partnersToGeoJson(
  partners: List<WastePartner>
): FeatureCollection {
  val features = partners.map { partner ->
    val properties = JsonObject().apply {
      addProperty("id", partner.id)
    }

    Feature.fromGeometry(
      Point.fromLngLat(
        partner.location.longitude,
        partner.location.latitude
      ),
      properties
    )
  }

  return FeatureCollection.fromFeatures(
    features.toTypedArray()
  )
}

@Composable
fun MapLibrePartnerMap(
  partners: List<WastePartner>,
  selectedPartnerId: String?,
  onPartnerSelected: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current

  val latestOnSelected by rememberUpdatedState(
    onPartnerSelected
  )

  var readyMap by remember {
    mutableStateOf<MapLibreMap?>(null)
  }

  val mapView = remember(context) {
    MapLibre.getInstance(context.applicationContext)

    MapView(context).apply {
      onCreate(null)
    }
  }

  DisposableEffect(mapView, lifecycleOwner) {
    var started = false
    var resumed = false

    fun startMap() {
      if (!started) {
        mapView.onStart()
        started = true
      }
    }

    fun resumeMap() {
      startMap()
      if (!resumed) {
        mapView.onResume()
        resumed = true
      }
    }

    fun pauseMap() {
      if (resumed) {
        mapView.onPause()
        resumed = false
      }
    }

    fun stopMap() {
      pauseMap()
      if (started) {
        mapView.onStop()
        started = false
      }
    }

    val observer = LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_START -> startMap()
        Lifecycle.Event.ON_RESUME -> resumeMap()
        Lifecycle.Event.ON_PAUSE -> pauseMap()
        Lifecycle.Event.ON_STOP -> stopMap()
        else -> Unit
      }
    }

    lifecycleOwner.lifecycle.addObserver(observer)

    if (lifecycleOwner.lifecycle.currentState
        .isAtLeast(Lifecycle.State.STARTED)) {
      startMap()
    }

    if (lifecycleOwner.lifecycle.currentState
        .isAtLeast(Lifecycle.State.RESUMED)) {
      resumeMap()
    }

    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
      stopMap()
      mapView.onDestroy()
    }
  }

  DisposableEffect(mapView) {
    var mapInstance: MapLibreMap? = null
    var clickListener: MapLibreMap.OnMapClickListener? = null
    var disposed = false

    mapView.getMapAsync { map ->
      if (!disposed) {
        mapInstance = map

        map.moveCamera(
          CameraUpdateFactory.newLatLngZoom(
            LatLng(-7.9800, 112.6300),
            14.0
          )
        )

        map.setStyle(
          Style.Builder().fromJson(OSM_STYLE)
        ) { style ->
          if (!disposed) {

            style.addSource(
              GeoJsonSource(PARTNER_SOURCE)
            )

            style.addSource(
              GeoJsonSource(SELECTED_SOURCE)
            )

            style.addLayer(
              CircleLayer(
                "partner-ring",
                PARTNER_SOURCE
              ).withProperties(
                PropertyFactory.circleRadius(16f),
                PropertyFactory.circleColor(
                  Color.WHITE
                ),
                PropertyFactory.circleStrokeColor(
                  Color.rgb(64, 145, 108)
                ),
                PropertyFactory.circleStrokeWidth(2f)
              )
            )

            style.addLayer(
              CircleLayer(
                PARTNER_LAYER,
                PARTNER_SOURCE
              ).withProperties(
                PropertyFactory.circleRadius(10f),
                PropertyFactory.circleColor(
                  Color.rgb(64, 145, 108)
                )
              )
            )

            style.addLayer(
              CircleLayer(
                "selected-ring",
                SELECTED_SOURCE
              ).withProperties(
                PropertyFactory.circleRadius(21f),
                PropertyFactory.circleColor(
                  Color.WHITE
                ),
                PropertyFactory.circleStrokeColor(
                  Color.rgb(27, 67, 50)
                ),
                PropertyFactory.circleStrokeWidth(3f)
              )
            )

            style.addLayer(
              CircleLayer(
                SELECTED_LAYER,
                SELECTED_SOURCE
              ).withProperties(
                PropertyFactory.circleRadius(12f),
                PropertyFactory.circleColor(
                  Color.rgb(27, 67, 50)
                )
              )
            )

            val listener =
              MapLibreMap.OnMapClickListener { location ->

                val screenPoint =
                  map.projection.toScreenLocation(
                    location
                  )

                val features =
                  map.queryRenderedFeatures(
                    screenPoint,
                    SELECTED_LAYER,
                    PARTNER_LAYER
                  )

                val partnerId =
                  features.firstOrNull()
                    ?.getStringProperty("id")

                if (partnerId != null) {
                  latestOnSelected(partnerId)
                  true
                } else {
                  false
                }
              }

            map.addOnMapClickListener(listener)
            clickListener = listener
            readyMap = map
          }
        }
      }
    }

    onDispose {
      disposed = true
      readyMap = null

      clickListener?.let { listener ->
        mapInstance?.removeOnMapClickListener(
          listener
        )
      }
    }
  }

  LaunchedEffect(readyMap, partners, selectedPartnerId) {
    val map = readyMap ?: return@LaunchedEffect
    val style = map.style ?: return@LaunchedEffect

    style.getSourceAs<GeoJsonSource>(
      PARTNER_SOURCE
    )?.setGeoJson(
      partnersToGeoJson(partners)
    )

    val selected = partners.filter {
      it.id == selectedPartnerId
    }

    style.getSourceAs<GeoJsonSource>(
      SELECTED_SOURCE
    )?.setGeoJson(
      partnersToGeoJson(selected)
    )
  }

  LaunchedEffect(readyMap, selectedPartnerId) {
    val map = readyMap ?: return@LaunchedEffect

    val partner = partners.firstOrNull {
      it.id == selectedPartnerId
    } ?: return@LaunchedEffect

    map.animateCamera(
      CameraUpdateFactory.newLatLngZoom(
        LatLng(
          partner.location.latitude,
          partner.location.longitude
        ),
        14.5
      ),
      450
    )
  }

  AndroidView(
    factory = { mapView },
    modifier = modifier.fillMaxSize()
  )
}
