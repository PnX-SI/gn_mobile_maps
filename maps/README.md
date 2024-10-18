# Maps module

## Dependencies

- [JTS Topology Suite](https://github.com/locationtech/jts)
- [osmdroid](https://github.com/osmdroid/osmdroid)

## Settings

Example:

```json
{
  "show_compass": true,
  "show_scale": true,
  "show_zoom": false,
  "max_bounds": [
    [
      47.253369,
      -1.605721
    ],
    [
      47.173845,
      -1.482811
    ]
  ],
  "center": [
    47.225827,
    -1.55447
  ],
  "start_zoom": 8.0,
  "min_zoom": 7.0,
  "max_zoom": 12.0,
  "min_zoom_editing": 10.0,
  "layers": [
    {
      "label": "Plan IGN v2",
      "source": "https://data.geopf.fr/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&STYLE=normal&TILEMATRIXSET=PM&FORMAT=image/png&LAYER=GEOGRAPHICALGRIDSYSTEMS.PLANIGNV2"
    },
    {
      "label": "Ortho 20cm",
      "source": "https://data.geopf.fr/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&STYLE=normal&TILEMATRIXSET=PM&FORMAT=image/jpeg&LAYER=ORTHOIMAGERY.ORTHOPHOTOS"
    },
    {
      "label": "Parcelles cadastrales",
      "source": "https://data.geopf.fr/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&STYLE=normal&TILEMATRIXSET=PM&FORMAT=image/png&LAYER=CADASTRALPARCELS.PARCELS"
    },
    {
      "label": "OpenStreetMap",
      "source": [
        "https://a.tile.openstreetmap.org",
        "https://b.tile.openstreetmap.org",
        "https://c.tile.openstreetmap.org"
      ]
    },
    {
      "label": "OpenTopoMap",
      "source": [
        "https://a.tile.opentopomap.org",
        "https://b.tile.opentopomap.org",
        "https://c.tile.opentopomap.org"
      ]
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

| Parameter                        | UI      | Description                                                              |
|----------------------------------|---------|--------------------------------------------------------------------------|
| `base_path`                      | &#9744; | Sets the default layers path (default: `null`).                          |
| `use_default_online_tile_source` | &#9745; | Whether to use online tiles source (default: `true`).                    |
| `show_compass`                   | &#9745; | Whether to show north compass during map rotation (default: `true`).     |
| `show_scale`                     | &#9745; | Whether to show the map scale (default: `true`).                         |
| `show_zoom`                      | &#9745; | Whether to show zoom control (default: `false`).                         |
| `rotate`                         | &#9745; | Whether to activate rotation gesture (default: `false`).                 |
| `max_bounds`                     | &#9744; | Set the map to limit it's scrollable view to the specified bounding box. |
| `center`                         | &#9744; | Center automatically the map at given position at startup.               |
| `start_zoom`                     | &#9744; | Set the default map zoom at startup.                                     |
| `min_zoom`                       | &#9744; | Set the minimum allowed zoom level.                                      |
| `max_zoom`                       | &#9744; | Set the maximum allowed zoom level.                                      |
| `min_zoom_editing`               | &#9744; | Set the minimum zoom level to allow editing feature on the map.          |
| `layers[]`                       | &#9744; | Define layers to display on the map.                                     |

#### Base path

- If `base_path` is not set, uses the external storage root path (if defined) or the internal
  storage root path as fallback to perform a deep scan to find all configured layers
- If `base_path` is an absolute path, tries to resolve the root path of the layers from this path
- If `base_path` is a relative path, tries to resolve the root path of the layers from external
  storage (if defined) matching this relative path or from internal storage matching this relative
  path as fallback
- If a configured layer was not found from `base_path`, tries to find it by performing a deep scan
  from external storage root path (if defined) or from internal storage root path as fallback

## Layer description

| Parameter    | Type               | Description                                                                                               |
|--------------|--------------------|-----------------------------------------------------------------------------------------------------------|
| `label`      | String             | A human friendly representation of this layer.                                                            |
| `source`     | String or String[] | Define the layer source name (e.g. URL of the tile source provider or the name of the local source file). |
| `properties` | Object             | Define additional layer properties (default: `null`).                                                     |

**Supported sources:**

- Online tiles source (URLs), supported online sources are:
  - [IGN Géoplateforme](https://geoservices.ign.fr)
  - [OpenStreetMap](https://www.openstreetmap.org)
  - [OpenTopoMap](https://www.opentopomap.org)
  - [Wikimedia Maps](https://maps.wikimedia.org)
- Local source (file), supported format are `.mbtiles` for tiles layer and `.geojson`, `.json`,
  `.wkt` for vector layer.

### Online tiles sources

Online tiles sources are active by default if at least one is registered in the configuration.
Online tiles sources can be disable through `use_default_online_tile_source` parameter to `false`.
In this case, the local source file take over the display of the tiles if at least one is registered
and active in the configuration.

If there is no Internet connection, the display of the tiles is based either on the tiles cache or
on the currently active local tiles source file.

#### Tiles resolution

Tiles are displayed in the following order of priority, depending on the zoom level and the x,y
position:
* If we have an active local tiles source and that tiles source can provide a tile at the requested
position (x, y) for the current zoom level, then the tile will be displayed from this local tiles
source.
* If we do not have an active local tiles source or that local tiles source cannot provide a tile at
the requested position (x, y) for the current zoom level, the system tries to request that tile from
the cached data or from an active online tiles source:
  * If we have an active online tiles source and that online tiles source can provide a tile at the
requested position (x, y) for the current zoom level, then the tile will be displayed from this
online tiles source.
  * If we have an active online tiles source but without Internet connection, the system tries to
request that tile from tiles cache. If the tiles cache can provide the requested tile, then the
cached tile will be displayed. If the tiles cache system does not have the correct tile requested,
an approximation of the tile will be made from the tiles at a lower zoom level and from the tiles
coming from the local tiles source if it is active. The resulting tile may be of lower quality
(pixelated) depending on the tiles used during the approximation process.

Local tiles sources can be registered without any online tiles sources. In this case, the tile cache
system is not very useful because the local tiles source will be the only tiles provider. The tiles
cache could potentially resolve tiles outside of the geographic range of the current active local
tiles source (for example, if an online tiles source has already been displayed on the requested
area).

#### IGN Géoplateforme

Through [WMTS](http://www.opengeospatial.org/standards/wmts) protocol.

Examples of available layer sources:

- **IGN plan v2**: https://data.geopf.fr/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&STYLE=normal&TILEMATRIXSET=PM&FORMAT=image/png&LAYER=GEOGRAPHICALGRIDSYSTEMS.PLANIGNV2
- **Ortho photo 20cm**: https://data.geopf.fr/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&STYLE=normal&TILEMATRIXSET=PM&FORMAT=image/jpeg&LAYER=ORTHOIMAGERY.ORTHOPHOTOS
- **Cadastral map**: https://data.geopf.fr/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&STYLE=normal&TILEMATRIXSET=PM&FORMAT=image/png&LAYER=CADASTRALPARCELS.PARCELS

See https://geoservices.ign.fr/documentation/services/services-geoplateforme/diffusion
for more information (in French).

#### OpenStreetMap

Base URLs:

- https://a.tile.openstreetmap.org
- https://b.tile.openstreetmap.org
- https://c.tile.openstreetmap.org

#### OpenTopoMap

Base URLs:

- https://a.tile.opentopomap.org
- https://b.tile.opentopomap.org
- https://c.tile.opentopomap.org

#### Wikimedia Maps

Base URLs:

- https://maps.wikimedia.org/osm-intl

### Layer properties

| Parameter          | Type    | Default value | Description                                                                                                                             |
|--------------------|---------|---------------|-----------------------------------------------------------------------------------------------------------------------------------------|
| `shown_by_default` | Boolean | true          | Whether to show this layer by default (default: `true`, only applicable to vector layers)                                               |
| `min_zoom`         | Number  | -1            | The minimum zoom level where the layer is visible                                                                                       |
| `max_zoom`         | Number  | -1            | The maximum zoom level where the layer is visible                                                                                       |
| `tile_size`        | Number  | 256           | The tile size in pixels (only applicable to tiles layers).                                                                              |
| `tile_mime_type`   | Number  | `image/png`   | The MIME type used for tiles (only applicable to tiles layers).                                                                         |
| `attribution`      | String  | `null`        | Describe the layer data and is often a legal obligation towards copyright holders and tile providers (only applicable to tiles layers). |
| `style`            | Object  | `null`        | Define the layer style (only applicable to vector layers).                                                                              |

If an online tile layer is active but no attribution is defined, it will automatically be set to
its default value according to this layer:

- **IGN Geoportail**: _© IGN Geoportail_
- **OpenStreetMap**: _© OpenStreetMap contributors, under ODbL licence_
- **OpenTopoMap**: _Map data: © OpenStreetMap contributors, SRTM | Map style: © OpenTopoMap (CC-BY-SA)_
- **Wikimedia Maps**: _Wikimedia maps | Map data © OpenStreetMap contributors_

### Layer style

Layer style is only available for vector layers (e.g. WKT or GeoJSON layers). Available parameters
are as follow:

| Parameter     | Type    | Default value | Description                                                                                                    |
|---------------|---------|---------------|----------------------------------------------------------------------------------------------------------------|
| `stroke`      | Boolean | `true`        | Whether to draw stroke along the path. Set it to false to disable borders on polygons or circles.              |
| `color`       | String  | `#444444`     | The stroke color. Supported formats are `#RRGGBB` and `#AARRGGBB`.                                             |
| `weight`      | Number  | 8             | The stroke width in pixels.                                                                                    |
| `opacity`     | Number  | 1.0           | The stroke opacity (value between 0 and 1, not applicable if an alpha channel is defined to the stroke color). |
| `fill`        | Boolean | `false`       | Whether to fill the path with color. Set it to false to disable filling on polygons or circles.                |
| `fillColor`   | String  | `#00000000`   | The fill color. Supported formats are `#RRGGBB` and `#AARRGGBB`.                                               |
| `fillOpacity` | Number  | 0.2           | The fill opacity (value between 0 and 1, not applicable if an alpha channel is defined to the fill color).     |
