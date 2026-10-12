package fr.geonature.maps.ui.overlay

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import fr.geonature.maps.util.LowPassSensorValuesFilter.lowPass
import org.osmdroid.views.overlay.compass.IOrientationConsumer
import org.osmdroid.views.overlay.compass.IOrientationProvider
import org.tinylog.kotlin.Logger

/**
 * Compute compass orientation from hardware sensors.
 *
 * @author S. Grimault
 */
class CompassOrientationProvider(context: Context) : SensorEventListener, IOrientationProvider {

    private var sensorManager: SensorManager? =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val sensorLatency = 500 // in ms
    private var orientationConsumer: IOrientationConsumer? = null
    private var gravityOrAccelerometerReading = FloatArray(3)
    private var magnetometerReading = FloatArray(3)
    private var orientation: Float = 0f

    override fun onAccuracyChanged(
        sensor: Sensor?,
        accuracy: Int
    ) {
        // nothing to do...
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        val rotationMatrix = FloatArray(9)
        val orientationAngles = FloatArray(3)

        when (event.sensor.type) {
            Sensor.TYPE_GRAVITY -> {
                gravityOrAccelerometerReading = event.values.clone()
            }

            Sensor.TYPE_ACCELEROMETER -> {
                gravityOrAccelerometerReading = event.values.clone()
            }

            Sensor.TYPE_MAGNETIC_FIELD -> {
                magnetometerReading = event.values.clone()
            }
        }

        // update rotation matrix, which is needed to update orientation angles
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            gravityOrAccelerometerReading,
            magnetometerReading
        )

        SensorManager.getOrientation(
            rotationMatrix,
            orientationAngles
        )

        val newOrientation = -Math.toDegrees(orientationAngles[0].toDouble())
            .toFloat()

        orientation = lowPass(
            arrayOf(newOrientation).toFloatArray(),
            arrayOf(orientation).toFloatArray()
        )[0]

        orientationConsumer?.onOrientationChanged(
            orientation,
            this
        )
    }

    override fun startOrientationProvider(orientationConsumer: IOrientationConsumer?): Boolean {
        this.orientationConsumer = orientationConsumer

        val rotationSensorListenerRegistered = false

        if (rotationSensorListenerRegistered) {
            Logger.debug { "rotation vector sensor listener registered: true" }

            return true
        }

        val gravitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
        val gravitySensorSensorListenerRegistered = sensorManager?.registerListener(
            this,
            gravitySensor,
            sensorLatency * 1000, // in µs
            sensorLatency * 1000, // in µs
        ) ?: false

        // if there is a gravity sensor we do not need the accelerometer
        val accelerometerSensorListenerRegistered = if (!gravitySensorSensorListenerRegistered) {
            val accelerometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            sensorManager?.registerListener(
                this,
                accelerometerSensor,
                sensorLatency * 1000, // in µs
                sensorLatency * 1000, // in µs
            ) ?: false
        } else false

        val magneticFieldSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        val magneticFieldSensorRegistered = sensorManager?.registerListener(
            this,
            magneticFieldSensor,
            sensorLatency * 1000, // in µs
            sensorLatency * 1000, // in µs
        ) ?: false

        Logger.debug { "gravity sensor listener registered: $gravitySensorSensorListenerRegistered, accelerometer sensor listener registered: $accelerometerSensorListenerRegistered, magnetic field sensor registered: $magneticFieldSensorRegistered" }

        return (gravitySensorSensorListenerRegistered || accelerometerSensorListenerRegistered) && magneticFieldSensorRegistered
    }

    override fun stopOrientationProvider() {
        sensorManager?.unregisterListener(this)
    }

    override fun getLastKnownOrientation(): Float {
        return orientation
    }

    override fun destroy() {
        stopOrientationProvider()
        sensorManager = null
    }
}
