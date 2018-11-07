package com.pervysage.thelimitbreaker.foco.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class DeviceMotionUtil(context: Context) : SensorEventListener {

    private var isActionDone = false
    private var initialFaceDown = true

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val acceleroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val FORCE_THRESHOLD = 350
    private val TIME_THRESHOLD = 100
    private val SHAKE_TIMEOUT = 500
    private val SHAKE_DURATION = 1000
    private val SHAKE_COUNT = 3

    private var mLastX = -1.0f
    private var mLastY = -1.0f
    private var mLastZ = -1.0f
    private var mLastTime: Long = 0
    private var mShakeCount = 0
    private var mLastShake: Long = 0
    private var mLastForce: Long = 0

    private lateinit var action: () -> Unit

    private lateinit var shakeAction: () -> Unit


    fun setShakeAction(l: () -> Unit) {
        shakeAction = l
    }

    fun setAction(a: () -> Unit) {
        action = a
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event!!.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            if (event.values[2] > 5 && event.values[1] < 3 && event.values[1] > -3) {
                initialFaceDown = false
            }
            if (!initialFaceDown) {
                if (event.values[2] < -8 && event.values[1] < 3 && event.values[1] > -3) {

                    if (!isActionDone) {
                        isActionDone = true
                        Thread.sleep(500)
                        action()
                    }
                }
            }

            val now = System.currentTimeMillis()
            if (now - mLastTime > TIME_THRESHOLD) {
                val diff = now - mLastTime
                val speed = Math.abs(event.values[0] + event.values[1] + event.values[2] - mLastX - mLastY - mLastZ) / diff * 10000
                if (speed > FORCE_THRESHOLD) {
                    if (++mShakeCount >= SHAKE_COUNT && now - mLastShake > SHAKE_DURATION) {
                        mLastShake = now
                        mShakeCount = 0
                        shakeAction()
                    }
                    mLastForce = now
                }
                mLastTime = now
                mLastX = event.values[0]
                mLastY = event.values[1]
                mLastZ = event.values[2]
            }
        }
    }

    fun startMotionListener() {
        sensorManager.registerListener(
                this,
                acceleroSensor,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
        )

    }

    fun stopMotionListener() {
        sensorManager.unregisterListener(this, acceleroSensor)
    }


}