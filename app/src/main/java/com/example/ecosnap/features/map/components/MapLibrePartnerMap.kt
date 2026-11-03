package com.example.ecosnap.features.map.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.ecosnap.features.map.model.WastePartner
import com.google.gson.JsonObject
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
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
        partner.location.latitude,
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

    if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
      startMap()
    }

    if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
      resumeMap()
    }


  }
}