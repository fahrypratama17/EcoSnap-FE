package com.example.ecosnap.features.map.components

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

