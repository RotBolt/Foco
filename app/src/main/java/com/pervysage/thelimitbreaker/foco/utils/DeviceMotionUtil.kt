package com.pervysage.thelimitbreaker.foco.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class DeviceMotionUtil(context: Context) : SensorEventListener {

    private val TAG = "DeviceMotionUtil"

    private var isActionDone = false
    private var initialFaceDown = true

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val acceleroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private lateinit var action: () -> Unit

    fun setAction(a: () -> Unit) {
        action = a
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event!!.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            Log.d(TAG, """

                x: ${event.values[0]}
                y: ${event.values[1]}
                z: ${event.values[2]}
            """.trimIndent())
            if (event.values[2] > 5 && event.values[1] < 3 && event.values[1] > -3) {
                initialFaceDown = false
            }
            if (!initialFaceDown) {
                if (event.values[2] < -8 && event.values[1] < 3 && event.values[1] > -3) {
                    Log.d(TAG, "isActionDone $isActionDone")
                    if (!isActionDone) {
                        isActionDone = true
                        action()
                    }
                }
            }
        }
    }

    fun startFlipListener() {
        Log.d(TAG,"startFlipListener")

        sensorManager.registerListener(
                this,
                acceleroSensor,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
        )

    }
    fun stopFlipListener() {
        Log.d(TAG,"stopFlipListener")
        sensorManager.unregisterListener(this,acceleroSensor)
    }


}