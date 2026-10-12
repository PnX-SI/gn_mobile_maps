package fr.geonature.maps.util

import android.Manifest
import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.test.core.app.ApplicationProvider
import fr.geonature.maps.CoroutineTestRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkClass
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows

@ExperimentalCoroutinesApi
@RunWith(RobolectricTestRunner::class)
class CurrentLocationLifecycleObserverTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var context: Application
    private lateinit var locationManager: LocationManager
    private lateinit var lifecycleOwner: ComponentActivity
    private lateinit var lifecycleRegistry: LifecycleRegistry

    @Before
    fun setup() {
        context = spyk(ApplicationProvider.getApplicationContext() as Application)

        locationManager = mockkClass(LocationManager::class)
        every { context.getSystemService(Context.LOCATION_SERVICE) } returns locationManager

        lifecycleOwner = mockk(relaxed = true)
        lifecycleRegistry = LifecycleRegistry(lifecycleOwner)
        every { lifecycleOwner.lifecycle } returns lifecycleRegistry

        // the Lifecycle must be in a CREATED state before adding the observer
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    @Test
    fun `should not retrieve the current position if no permissions were granted`() = runTest {
        // deny permissions
        Shadows.shadowOf(context)
            .denyPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )

        val testRegistry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(
                requestCode: Int,
                contract: ActivityResultContract<I, O>,
                input: I,
                options: ActivityOptionsCompat?
            ) {
                dispatchResult(
                    requestCode,
                    mapOf(
                        Manifest.permission.ACCESS_FINE_LOCATION to false,
                        Manifest.permission.ACCESS_COARSE_LOCATION to false
                    )
                )
            }
        }

        val currentLocationLifecycleObserver = CurrentLocationLifecycleObserver(
            context,
            lifecycleOwner,
            testRegistry
        )

        // the test needs to advance the lifecycle to call the observer
        lifecycleRegistry.currentState = Lifecycle.State.STARTED

        val currentLocation = currentLocationLifecycleObserver.getCurrentLocation()
        assertNull(currentLocation)
    }

    @Test
    fun `should retrieve the current position if at least one permission was granted`() = runTest {
        with(Shadows.shadowOf(context)) {
            denyPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
            grantPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        val testRegistry = object : ActivityResultRegistry() {
            override fun <I, O> onLaunch(
                requestCode: Int,
                contract: ActivityResultContract<I, O>,
                input: I,
                options: ActivityOptionsCompat?
            ) {
                dispatchResult(
                    requestCode,
                    mapOf(
                        Manifest.permission.ACCESS_FINE_LOCATION to false,
                        Manifest.permission.ACCESS_COARSE_LOCATION to true
                    )
                )
            }
        }
        val expectedLocation = Location(LocationManager.NETWORK_PROVIDER).apply {
            latitude = 47.2256258
            longitude = -1.5545135
            accuracy = 2.0F
            time = System.currentTimeMillis()
            elapsedRealtimeNanos = System.nanoTime()
        }

        every { locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) } returns false
        every { locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) } returns true
        every { locationManager.removeUpdates(any<LocationListener>()) } returns Unit
        every {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                any<Long>(),
                any<Float>(),
                any<LocationListener>()
            )
        } answers {
            lastArg<LocationListener>().onLocationChanged(expectedLocation)
        }

        val currentLocationLifecycleObserver = CurrentLocationLifecycleObserver(
            context,
            lifecycleOwner,
            testRegistry
        )

        // the test needs to advance the lifecycle to call the observer
        lifecycleRegistry.currentState = Lifecycle.State.STARTED

        val currentLocation = currentLocationLifecycleObserver.getCurrentLocation()

        assertEquals(
            expectedLocation,
            currentLocation
        )
    }
}