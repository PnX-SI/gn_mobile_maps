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
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class CompassOrientationProvider(context: Context) : SensorEventListener, IOrientationProvider {

    private var sensorManager: SensorManager? =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var orientationConsumer: IOrientationConsumer? = null
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private var orientation: Float = 0f

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // nothing to do...
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(
                event.values,
                0,
                accelerometerReading,
                0,
                accelerometerReading.size
            )
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(
                event.values,
                0,
                magnetometerReading,
                0,
                magnetometerReading.size
            )
        }

        // update rotation matrix, which is needed to update orientation angles
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )

        SensorManager.getOrientation(
            rotationMatrix,
            orientationAngles
        )

        val newOrientation = Math.toDegrees(orientationAngles[0].toDouble())
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

        val accelerometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val accelerometerSensorListenerRegistered = sensorManager?.registerListener(
            this,
            accelerometerSensor,
            SensorManager.SENSOR_DELAY_NORMAL,
            SensorManager.SENSOR_DELAY_NORMAL
        ) ?: false

        val magneticFieldSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        val magneticFieldSensorRegistered = sensorManager?.registerListener(
            this,
            magneticFieldSensor,
            SensorManager.SENSOR_DELAY_NORMAL,
            SensorManager.SENSOR_DELAY_NORMAL
        ) ?: false

        Logger.debug { "accelerometer sensor listener registered: $accelerometerSensorListenerRegistered, magnetic field sensor registered: $magneticFieldSensorRegistered" }

        return accelerometerSensorListenerRegistered && magneticFieldSensorRegistered
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
