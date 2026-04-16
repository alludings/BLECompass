// SPDX-License-Identifier: GPL-3.0-or-later

package com.mubarak.mbcompass.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.mubarak.mbcompass.BLECompassClient
import com.mubarak.mbcompass.R
import com.mubarak.mbcompass.utils.Azimuth
import kotlin.math.sqrt

private const val TAG = "SensorListener"

class AndroidSensorEventListener(
    private val context: Context,
    private val sensorViewModel: SensorViewModel,
    private val onAccuracyUpdate: (accuracy: Int) -> Unit
) : SensorEventListener {

    interface AzimuthValueListener {
        fun onAzimuthValueChange(degree: Azimuth)
        fun onMagneticStrengthChange(strengthInUt: Float)
    }

    private var azimuthValueListener: AzimuthValueListener? = null

    private val bleClient = BLECompassClient(context) { heading ->
        azimuthValueListener?.onAzimuthValueChange(Azimuth(heading))
    }

    init {
        bleClient.startScan()
    }

    private val magnetometerReading = FloatArray(3)

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
            findMagneticStrength()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        if (sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            onAccuracyUpdate(accuracy)
        }
    }

    private fun findMagneticStrength() {
        val magneticStrength = sqrt(
            (magnetometerReading[0] * magnetometerReading[0]) +
                    (magnetometerReading[1] * magnetometerReading[1]) +
                    (magnetometerReading[2] * magnetometerReading[2])
        )

        azimuthValueListener?.onMagneticStrengthChange(magneticStrength)
    }

    fun registerSensor() {
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.also { magneticFieldSensor ->
            registerMagneticFieldSensor(sensorManager, magneticFieldSensor)
        } ?: run {
            Toast.makeText(context, R.string.magnetometer_not_available, Toast.LENGTH_LONG).show()
        }
    }

    private fun registerMagneticFieldSensor(
        sensorManager: SensorManager,
        magneticFieldSensor: Sensor
    ) {
        val result = sensorManager.registerListener(
            this,
            magneticFieldSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        if (result) {
            Log.d(TAG, "Magnetometer is registered")
        } else {
            Log.w(TAG, "Unable to register Magnetometer")
        }
    }

    fun unregisterSensorListener() {
        sensorManager.unregisterListener(this)
    }

    fun setAzimuthListener(listener: AzimuthValueListener) {
        azimuthValueListener = listener
    }
}