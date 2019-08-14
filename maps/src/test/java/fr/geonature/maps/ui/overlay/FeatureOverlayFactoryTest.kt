package fr.geonature.maps.ui.overlay

import fr.geonature.maps.FixtureHelper.getFixture
import fr.geonature.maps.jts.geojson.GeometryUtils
import fr.geonature.maps.jts.geojson.JTSTestHelper.createCoordinate
import fr.geonature.maps.jts.geojson.JTSTestHelper.createGeometryCollection
import fr.geonature.maps.jts.geojson.JTSTestHelper.createLineString
import fr.geonature.maps.jts.geojson.JTSTestHelper.createLinearRing
import fr.geonature.maps.jts.geojson.JTSTestHelper.createMultiLineString
import fr.geonature.maps.jts.geojson.JTSTestHelper.createMultiPoint
import fr.geonature.maps.jts.geojson.JTSTestHelper.createMultiPolygon
import fr.geonature.maps.jts.geojson.JTSTestHelper.createPoint
import fr.geonature.maps.jts.geojson.JTSTestHelper.createPolygon
import fr.geonature.maps.jts.geojson.io.GeoJsonReader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.overlay.FolderOverlay
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.Polyline
import org.robolectric.RobolectricTestRunner
import java.io.StringReader

/**
 * Unit tests about [FeatureOverlayFactory].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class FeatureOverlayFactoryTest {

    private lateinit var gf: GeometryFactory
    private lateinit var geoJsonReader: GeoJsonReader

    @Before
    fun setUp() {
        gf = GeometryFactory()
        geoJsonReader = GeoJsonReader()
    }

    @Test
    fun testCreateOverlayFromPoint() {
        // given Point
        val point = createPoint(gf,
                                47.2256258,
                                -1.5545135)

        // when create assertEquals(2, (overlay.items[1] as FolderOverlay).items.size) from Point
        val overlayAsCirclePolygon = FeatureOverlayFactory.createOverlay(point)

        // then
        assertTrue(overlayAsCirclePolygon is Polygon)
        assertEquals(GeometryUtils.fromPoint(point).longitude,
                     BoundingBox.fromGeoPoints((overlayAsCirclePolygon as Polygon).points).centerLongitude,
                     0.00001)
        assertEquals(GeometryUtils.fromPoint(point).latitude,
                     BoundingBox.fromGeoPoints(overlayAsCirclePolygon.points).centerLatitude,
                     0.00001)

        // when create Overlay from Geometry
        val overlayFromGeometry = FeatureOverlayFactory.createOverlay(point as Geometry)

        // then
        assertEquals(overlayAsCirclePolygon.points,
                     (overlayFromGeometry as Polygon).points)
    }

    @Test
    fun testCreateOverlayFromMultiPoint() {
        // given MultiPoint
        val multiPoints = createMultiPoint(gf,
                                           createPoint(gf,
                                                       47.2256258,
                                                       -1.5545135),
                                           createPoint(gf,
                                                       47.225136,
                                                       -1.553913))

        // when create Overlay from MultiPoint
        val overlay = FeatureOverlayFactory.createOverlay(multiPoints)

        // then
        assertTrue(overlay is FolderOverlay)
        assertEquals(2,
                     (overlay as FolderOverlay).items.size)
        overlay.items.forEach {
            assertTrue(it is Polygon)
        }

        // when create Overlay from Geometry
        val overlayFromGeometry = FeatureOverlayFactory.createOverlay(multiPoints as Geometry)

        // then
        assertTrue(overlayFromGeometry is FolderOverlay)
        assertEquals(2,
                     (overlayFromGeometry as FolderOverlay).items.size)
        overlayFromGeometry.items.forEach {
            assertTrue(it is Polygon)
        }
    }

    @Test
    fun testCreateOverlayFromLineString() {
        // given LineString
        val lineString = createLineString(gf,
                                          createCoordinate(47.2256258,
                                                           -1.5545135),
                                          createCoordinate(47.225136,
                                                           -1.553913))

        // when create Overlay from LineString
        val overlayAsPolyline = FeatureOverlayFactory.createOverlay(lineString)

        // then
        assertTrue(overlayAsPolyline is Polyline)
        assertEquals(2,
                     (overlayAsPolyline as Polyline).points.size)

        // when create Overlay from Geometry
        val overlayFromGeometry = FeatureOverlayFactory.createOverlay(lineString as Geometry)

        // then
        assertEquals(overlayAsPolyline.points,
                     (overlayFromGeometry as Polyline).points)
    }

    @Test
    fun testCreateOverlayFromMultiLineString() {
        // given MultiLineString
        val multiLineString = createMultiLineString(gf,
                                                    createLineString(gf,
                                                                     createCoordinate(47.2256258,
                                                                                      -1.5545135),
                                                                     createCoordinate(47.225136,
                                                                                      -1.553913)))

        // when create Overlay from MultiLineString
        val overlay = FeatureOverlayFactory.createOverlay(multiLineString)

        // then
        assertTrue(overlay is FolderOverlay)
        assertEquals(1,
                     (overlay as FolderOverlay).items.size)
        overlay.items.forEach {
            assertTrue(it is Polyline)
        }

        // when create Overlay from Geometry
        val overlayFromGeometry = FeatureOverlayFactory.createOverlay(multiLineString as Geometry)

        // then
        assertTrue(overlayFromGeometry is FolderOverlay)
        assertEquals(1,
                     (overlayFromGeometry as FolderOverlay).items.size)
        overlayFromGeometry.items.forEach {
            assertTrue(it is Polyline)
        }
    }

    @Test
    fun testCreateOverlayFromSimplePolygon() {
        // given Polygon
        val polygon = createPolygon(gf,
                                    createCoordinate(47.226219,
                                                     -1.554430),
                                    createCoordinate(47.226237,
                                                     -1.554261),
                                    createCoordinate(47.226122,
                                                     -1.554245),
                                    createCoordinate(47.226106,
                                                     -1.554411),
                                    createCoordinate(47.226219,
                                                     -1.554430))

        // when create Overlay from Polygon
        val overlayAsPolygon = FeatureOverlayFactory.createOverlay(polygon)

        // then
        assertTrue(overlayAsPolygon is Polygon)
        assertEquals(5,
                     (overlayAsPolygon as Polygon).points.size)
        assertTrue(overlayAsPolygon.holes.isEmpty())

        // when create Overlay from Geometry
        val overlayFromGeometry = FeatureOverlayFactory.createOverlay(polygon as Geometry)

        // then
        assertEquals(overlayAsPolygon.points,
                     (overlayFromGeometry as Polygon).points)
    }

    @Test
    fun testCreateOverlayFromPolygonWithHoles() {
        // given Polygon with holes
        val polygonWithHoles = createPolygon(gf,
                                             createLinearRing(gf,
                                                              createCoordinate(47.226257,
                                                                               -1.554564),
                                                              createCoordinate(47.226295,
                                                                               -1.554202),
                                                              createCoordinate(47.226075,
                                                                               -1.554169),
                                                              createCoordinate(47.226049,
                                                                               -1.554496),
                                                              createCoordinate(47.226257,
                                                                               -1.554564)),
                                             createLinearRing(gf,
                                                              createCoordinate(47.226219,
                                                                               -1.554430),
                                                              createCoordinate(47.226237,
                                                                               -1.554261),
                                                              createCoordinate(47.226122,
                                                                               -1.554245),
                                                              createCoordinate(47.226106,
                                                                               -1.554411),
                                                              createCoordinate(47.226219,
                                                                               -1.554430)))

        // when create Overlay from Polygon
        val overlayAsPolygon = FeatureOverlayFactory.createOverlay(polygonWithHoles)

        // then
        assertTrue(overlayAsPolygon is Polygon)
        assertEquals(5,
                     (overlayAsPolygon as Polygon).points.size)
        assertEquals(1,
                     overlayAsPolygon.holes.size)
        assertEquals(5,
                     (overlayAsPolygon).holes[0].size)

        // when create Overlay from Geometry
        val overlayFromGeometry = FeatureOverlayFactory.createOverlay(polygonWithHoles as Geometry)

        // then
        assertEquals(overlayAsPolygon.points,
                     (overlayFromGeometry as Polygon).points)
        assertEquals(overlayAsPolygon.holes,
                     overlayFromGeometry.holes)
    }

    @Test
    fun testCreateOverlayFromMultiPolygon() {
        // given MultiPolygon
        val multiPolygon = createMultiPolygon(gf,
                                              createPolygon(gf,
                                                            createCoordinate(47.226116,
                                                                             -1.554169),
                                                            createCoordinate(47.226126,
                                                                             -1.554097),
                                                            createCoordinate(47.225527,
                                                                             -1.553986),
                                                            createCoordinate(47.225519,
                                                                             -1.554061),
                                                            createCoordinate(47.226116,
                                                                             -1.554169)),
                                              createPolygon(gf,
                                                            createLinearRing(gf,
                                                                             createCoordinate(47.226257,
                                                                                              -1.554564),
                                                                             createCoordinate(47.226295,
                                                                                              -1.554202),
                                                                             createCoordinate(47.226075,
                                                                                              -1.554169),
                                                                             createCoordinate(47.226049,
                                                                                              -1.554496),
                                                                             createCoordinate(47.226257,
                                                                                              -1.554564)),
                                                            createLinearRing(gf,
                                                                             createCoordinate(47.226219,
                                                                                              -1.554430),
                                                                             createCoordinate(47.226237,
                                                                                              -1.554261),
                                                                             createCoordinate(47.226122,
                                                                                              -1.554245),
                                                                             createCoordinate(47.226106,
                                                                                              -1.554411),
                                                                             createCoordinate(47.226219,
                                                                                              -1.554430))))

        // when create Overlay from MultiPolygon
        val overlay = FeatureOverlayFactory.createOverlay(multiPolygon)

        // then
        assertTrue(overlay is FolderOverlay)
        assertEquals(2,
                     (overlay as FolderOverlay).items.size)
        overlay.items.forEach {
            assertTrue(it is Polygon)
        }

        // when create Overlay from Geometry
        val overlayFromGeometry = FeatureOverlayFactory.createOverlay(multiPolygon as Geometry)

        // then
        assertTrue(overlayFromGeometry is FolderOverlay)
        assertEquals(2,
                     (overlayFromGeometry as FolderOverlay).items.size)
        overlayFromGeometry.items.forEach {
            assertTrue(it is Polygon)
        }
    }

    @Test
    fun testCreateOverlayFromEmptyGeometryCollection() {
        // given an empty GeometryCollection
        val multiPoints = createMultiPoint(gf)

        // when create Overlay from MultiPoint
        val overlay = FeatureOverlayFactory.createOverlay(multiPoints)

        // then
        assertTrue(overlay is FolderOverlay)
        assertTrue((overlay as FolderOverlay).items.isEmpty())
    }

    @Test
    fun testCreateOverlayFromGeometryCollection() {
        // given a GeometryCollection
        val geometryCollection = createGeometryCollection(gf,
                                                          createPoint(gf,
                                                                      47.2256258,
                                                                      -1.5545135),
                                                          createMultiPoint(gf,
                                                                           createPoint(gf,
                                                                                       47.2256258,
                                                                                       -1.5545135),
                                                                           createPoint(gf,
                                                                                       47.225136,
                                                                                       -1.553913)),
                                                          createLineString(gf,
                                                                           createCoordinate(47.2256258,
                                                                                            -1.5545135),
                                                                           createCoordinate(47.225136,
                                                                                            -1.553913)),
                                                          createMultiLineString(gf,
                                                                                createLineString(gf,
                                                                                                 createCoordinate(47.2256258,
                                                                                                                  -1.5545135),
                                                                                                 createCoordinate(47.225136,
                                                                                                                  -1.553913))),
                                                          createPolygon(gf,
                                                                        createCoordinate(47.226219,
                                                                                         -1.554430),
                                                                        createCoordinate(47.226237,
                                                                                         -1.554261),
                                                                        createCoordinate(47.226122,
                                                                                         -1.554245),
                                                                        createCoordinate(47.226106,
                                                                                         -1.554411),
                                                                        createCoordinate(47.226219,
                                                                                         -1.554430)))

        // when create Overlay from GeometryCollection
        val overlay = FeatureOverlayFactory.createOverlay(geometryCollection)

        // then
        assertTrue(overlay is FolderOverlay)
        assertEquals(5,
                     (overlay as FolderOverlay).items.size)
        assertTrue(overlay.items[0] is Polygon)
        assertTrue(overlay.items[1] is FolderOverlay)
        assertEquals(2,
                     (overlay.items[1] as FolderOverlay).items.size)
        assertTrue(overlay.items[2] is Polyline)
        assertTrue(overlay.items[3] is FolderOverlay)
        assertEquals(1,
                     (overlay.items[3] as FolderOverlay).items.size)
        assertTrue(overlay.items[4] is Polygon)
    }

    @Test
    fun testCreateOverlayFromFeature() {
        // given a JSON Feature as GeometryCollection
        val reader = StringReader(getFixture("feature_geometrycollection.json"))

        // when read the JSON as Feature
        val feature = geoJsonReader.readFeature(reader)

        // then
        assertNotNull(feature)

        // when create Overlay from Feature
        val overlay = FeatureOverlayFactory.createOverlay(feature)

        // then
        assertTrue(overlay is FolderOverlay)
        assertEquals(5,
                     (overlay as FolderOverlay).items.size)
        assertTrue(overlay.items[0] is Polygon)
        assertTrue(overlay.items[1] is FolderOverlay)
        assertEquals(2,
                     (overlay.items[1] as FolderOverlay).items.size)
        assertTrue(overlay.items[2] is Polyline)
        assertTrue(overlay.items[3] is FolderOverlay)
        assertEquals(1,
                     (overlay.items[3] as FolderOverlay).items.size)
        assertTrue(overlay.items[4] is Polygon)
    }

    @Test
    fun testCreateOverlayFromFeatureCollection() {
        // given a JSON FeatureCollection
        val json = StringReader(getFixture("featurecollection.json"))

        // when read the JSON as FeatureCollection
        val featureCollection = geoJsonReader.readFeatureCollection(json)

        // then
        assertNotNull(featureCollection)

        // when create Overlay from FeatureCollection
        val overlay = FeatureOverlayFactory.createOverlay(featureCollection)

        // then
        assertTrue(overlay is FolderOverlay)
        assertEquals(5,
                     (overlay as FolderOverlay).items.size)
    }

    @Test
    fun testCreateOverlayFromEmptyFeatureCollection() {
        // given a JSON empty FeatureCollection
        val reader = StringReader(getFixture("featurecollection_empty.json"))

        // when read the JSON as FeatureCollection
        val featureCollection = geoJsonReader.readFeatureCollection(reader)

        // then
        assertNotNull(featureCollection)

        // when create Overlay from FeatureCollection
        val overlay = FeatureOverlayFactory.createOverlay(featureCollection)

        // then
        assertTrue(overlay is FolderOverlay)
        assertTrue((overlay as FolderOverlay).items.isEmpty())
    }
}