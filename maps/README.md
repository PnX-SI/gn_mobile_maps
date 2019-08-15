# Maps module

## Settings

Example:

```json
{
  "show_scale": false,
  "show_compass": false,
  "max_bounds": [[47.253369, -1.605721], [47.173845, -1.482811]],
  "center": [47.225827, -1.55447],
  "start_zoom": 8.0,
  "min_zoom": 7.0,
  "max_zoom": 12.0,
  "min_zoom_editing": 10.0,
  "layers": [
    {
      "label": "Nantes (Base)",
      "name": "nantes.mbtiles"
    },
    {
      "label": "Nantes (Data)",
      "name": "nantes.wkt",
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
  ]
}
```

### Parameters description

| Parameter          | UI      | Description                                                                                                                        |
| ------------------ | ------- | ---------------------------------------------------------------------------------------------------------------------------------- |
| `base_path`        | &#9744; | Sets the default layers path (default: `null`).                                                                                    |
| `show_scale`       | &#9745; | Whether to show the map scale (default: `true`).                                                                                   |
| `show_compass`     | &#9745; | Whether to show north compass during map rotation (default: `true`).                                                               |
| `max_bounds`       | &#9744; | Set the map to limit it's scrollable view to the specified bounding box.                                                           |
| `center`           | &#9744; | Center automatically the map at given position at startup.                                                                         |
| `start_zoom`       | &#9744; | Set the default map zoom at startup.                                                                                               |
| `min_zoom`         | &#9744; | Set the minimum allowed zoom level.                                                                                                |
| `max_zoom`         | &#9744; | Set the maximum allowed zoom level.                                                                                                |
| `min_zoom_editing` | &#9744; | Set the minimum zoom level to allow editing feature on the map.                                                                    |
| `layers`           | &#9744; | Define layers to display on the map.                                                                                               |
| `layers[]/label`   | &#9744; | A human friendly representation of this layer.                                                                                     |
| `layers[]/source`  | &#9744; | Define the layer source name (e.g. the name of the source file). Supported formats are `.mbtiles`, `.geojson`, `.json` and `.wkt`. |
| `layers[]/style`   | &#9744; | Define the layer style (only applicable to vector layers).                                                                         |

### Layer style

Layer style is only available to vector layers (e.g. WKT or GeoJSON layers). Available parameters are as follow:

| Parameter     | Type    | Default value | Description                                                                                                                    |
| ------------- | ------- | ------------- | ------------------------------------------------------------------------------------------------------------------------------ |
| `stroke`      | Boolean | `true`        | Whether to draw stroke along the path. Set it to false to disable borders on polygons or circles.                              |
| `color`       | String  | `#444444`     | The stroke color. Supported formats are `#RRGGBB` and `#AARRGGBB` (Alpha channel is only available for Android Oreo or above). |
| `weight`      | Number  | 8             | The stroke width in pixels.                                                                                                    |
| `opacity`     | Number  | 1.0           | The stroke opacity (value between 0 and 1, not applicable if an alpha channel is defined to the stroke color).                 |
| `fill`        | Boolean | `false`       | Whether to fill the path with color. Set it to false to disable filling on polygons or circles.                                |
| `fillColor`   | String  | `#00000000`   | The fill color. Supported formats are `#RRGGBB` and `#AARRGGBB` (Alpha channel is only available for Android Oreo or above).   |
| `fillOpacity` | Number  | 0.2           | The fill opacity (value between 0 and 1, not applicable if an alpha channel is defined to the fill color).                     |
