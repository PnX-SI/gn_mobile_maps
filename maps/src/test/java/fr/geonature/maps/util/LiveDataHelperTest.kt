package fr.geonature.maps.util

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.MutableLiveData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit test for `LiveDataHelper`.
 *
 * @author S. Grimault
 */
class LiveDataHelperTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var lifecycleOwner: LifecycleOwner
    private lateinit var lifecycleRegistry: LifecycleRegistry

    @Before
    fun setup() {
        lifecycleOwner = object : LifecycleOwner {
            override val lifecycle: Lifecycle
                get() = lifecycleRegistry
        }
        lifecycleRegistry = LifecycleRegistry(lifecycleOwner)
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
    }

    @Test
    fun `observer should be called only once`() {
        val liveData = MutableLiveData<Int>()
        val observedValues = mutableListOf<Int?>()

        liveData.observeOnce(lifecycleOwner) {
            observedValues.add(it)
        }

        liveData.value = 1
        liveData.value = 2
        liveData.value = 3

        assertEquals(listOf(1), observedValues)
    }

    @Test
    fun `observer should not be called if lifecycle is not started`() {
        lifecycleRegistry.currentState = Lifecycle.State.CREATED

        val liveData = MutableLiveData<Int>()
        val observedValues = mutableListOf<Int?>()

        liveData.observeOnce(lifecycleOwner) {
            observedValues.add(it)
        }

        liveData.value = 1

        assertTrue(observedValues.isEmpty())
    }

    @Test
    fun `observer should receive null if null value is emitted`() {
        val liveData = MutableLiveData<String?>()
        val observedValues = mutableListOf<String?>()

        liveData.observeOnce(lifecycleOwner) {
            observedValues.add(it)
        }

        liveData.value = null
        liveData.value = "some value" // should not be observed

        assertEquals(listOf(null), observedValues)
    }

    @Test
    fun `observer should be called until condition is met`() {
        val liveData = MutableLiveData<Int>()
        val observedValues = mutableListOf<Int?>()

        liveData.observeUntil(
            owner = lifecycleOwner,
            until = { it == 3 },
            observer = { observedValues.add(it) })

        liveData.value = 1
        liveData.value = 2
        liveData.value = 3
        liveData.value = 4 // should not be observed

        assertEquals(
            listOf(
                1,
                2,
                3
            ),
            observedValues
        )
    }

    @Test
    fun `observer should stop immediately if condition met on first emission`() {
        val liveData = MutableLiveData<Int>()
        val observedValues = mutableListOf<Int?>()

        liveData.observeUntil(
            owner = lifecycleOwner,
            until = { it == 1 },
            observer = { observedValues.add(it) })

        liveData.value = 1
        liveData.value = 2 // should not be observed

        assertEquals(
            listOf(1),
            observedValues
        )
    }

    @Test
    fun `observer should not be triggered if lifecycle is not started`() {
        lifecycleRegistry.currentState = Lifecycle.State.CREATED

        val liveData = MutableLiveData<Int>()
        val observedValues = mutableListOf<Int?>()

        liveData.observeUntil(
            owner = lifecycleOwner,
            until = { it == 1 },
            observer = { observedValues.add(it) })

        liveData.value = 1
        liveData.value = 2

        assertTrue(observedValues.isEmpty())
    }
}