# Maps module

## Dependencies

- [JTS Topology Suite](https://github.com/locationtech/jts)
- [osmdroid](https://github.com/osmdroid/osmdroid)

## Settings

Example:

```json
{
  "show_attribution": true,
  "show_compass": true,
  "show_scale": true,
  "show_zoom": false,
  "max_bounds": [
    [47.253369, -1.605721],
    [47.173845, -1.482811]
  ],
  "center": [47.225827, -1.55447],
  "start_zoom": 8.0,
  "min_zoom": 7.0,
  "max_zoom": 12.0,
  "min_zoom_editing": 10.0,
  "layers": [
    {
      "label": "OSM",
      "source": "https://a.tile.openstreetmap.org"
    },
    {
      "label": "Nantes (Base)",
      "source": "nantes.mbtiles"
    },
    {
      "label": "Nantes (Data)",
      "source": "nantes.wkt",
      "properties": {
        "style": {
          "stroke": true,
          "color": "#FF0000",
          "weight": 8,
          "opacity": 0.9,
          "fill": true,
          "fillColor": "#FF8000",
          "fillOpacity": 0.2
        }
      }
    }
  ]
}
```

### Parameters description

| Parameter                        | UI      | Description                                                                                  |
| -------------------------------- | ------- | -------------------------------------------------------------------------------------------- |
| `base_path`                      | &#9744; | Sets the default layers path (default: `null`).                                              |
| `use_default_online_tile_source` | &#9745; | Whether to use the default online tiles source (default: `true`, default tiles source: OSM). |
| `show_attribution`               | &#9744; | Whether to show the layer attribution control (default: `true`).                             |
| `show_compass`                   | &#9745; | Whether to show north compass during map rotation (default: `true`).                         |
| `show_scale`                     | &#9745; | Whether to show the map scale (default: `true`).                                             |
| `show_zoom`                      | &#9745; | Whether to show zoom control (default: `false`).                                             |
| `rotate`                         | &#9745; | Whether to activate rotation gesture (default: `false`).                                     |
| `max_bounds`                     | &#9744; | Set the map to limit it's scrollable view to the specified bounding box.                     |
| `center`                         | &#9744; | Center automatically the map at given position at startup.                                   |
| `start_zoom`                     | &#9744; | Set the default map zoom at startup.                                                         |
| `min_zoom`                       | &#9744; | Set the minimum allowed zoom level.                                                          |
| `max_zoom`                       | &#9744; | Set the maximum allowed zoom level.                                                          |
| `min_zoom_editing`               | &#9744; | Set the minimum zoom level to allow editing feature on the map.                              |
| `layers[]`                       | &#9744; | Define layers to display on the map.                                                         |

#### Base path

- If `base_path` is not set, uses the external storage root path (if defined) or the internal
  storage root path as fallback to perform a deep scan to find all configured layers
- If `base_path` is an absolute path, tries to resolve the root path of the layers from this path
- If `base_path` is a relative path, tries to resolve the root path of the layers from external
  storage (if defined) matching this relative path or from internal storage matching this relative
  path as fallback
- If a configured layer was not found from `base_path`, tries to find it by performing a deep scan
  from external storage root path (if defined) or from internal storage root path as fallback

#### Layer description

| Parameter    | Type   | Description                                                                                               |
| ------------ | ------ | --------------------------------------------------------------------------------------------------------- |
| `label`      | String | A human friendly representation of this layer.                                                            |
| `source`     | String | Define the layer source name (e.g. URL of the tile source provider or the name of the local source file). |
| `properties` | Object | Define additional layer properties (default: `null`).                                                     |

**Supported sources:**

- Online tiles source (URLs)
- Local source (file), supported format are `.mbtiles` for tiles layer and `.geojson`, `.json`,
  `.wkt` for vector layer

##### Layer properties

| Parameter        | Type   | Default value | Description                                                                                           |
| ---------------- | ------ | ------------- | ----------------------------------------------------------------------------------------------------- |
| `min_zoom`       | Number | -1            | The minimum zoom level                                                                                |
| `max_zoom`       | Number | -1            | The maximum zoom level                                                                                |
| `tile_size`      | Number | 256           | The tile size in pixels (Only applicable to tiles layers).                                            |
| `tile_mime_type` | Number | `image/png`   | The MIME type used for tiles (Only applicable to tiles layers).                                       |
| `attribution`    | String | `null`        | Describe the layer data and is often a legal obligation towards copyright holders and tile providers. |
| `style`          | Object | `null`        | Define the layer style (only applicable to vector layers).                                            |

##### Layer style

Layer style is only available to vector layers (e.g. WKT or GeoJSON layers). Available parameters
are as follow:

| Parameter     | Type    | Default value | Description                                                                                                    |
| ------------- | ------- | ------------- | -------------------------------------------------------------------------------------------------------------- |
| `stroke`      | Boolean | `true`        | Whether to draw stroke along the path. Set it to false to disable borders on polygons or circles.              |
| `color`       | String  | `#444444`     | The stroke color. Supported formats are `#RRGGBB` and `#AARRGGBB`.                                             |
| `weight`      | Number  | 8             | The stroke width in pixels.                                                                                    |
| `opacity`     | Number  | 1.0           | The stroke opacity (value between 0 and 1, not applicable if an alpha channel is defined to the stroke color). |
| `fill`        | Boolean | `false`       | Whether to fill the path with color. Set it to false to disable filling on polygons or circles.                |
| `fillColor`   | String  | `#00000000`   | The fill color. Supported formats are `#RRGGBB` and `#AARRGGBB`.                                               |
| `fillOpacity` | Number  | 0.2           | The fill opacity (value between 0 and 1, not applicable if an alpha channel is defined to the fill color).     |
