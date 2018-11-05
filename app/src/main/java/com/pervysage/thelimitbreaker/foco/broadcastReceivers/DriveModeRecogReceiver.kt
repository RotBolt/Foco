package com.pervysage.thelimitbreaker.foco.broadcastReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity
import com.pervysage.thelimitbreaker.foco.R
import com.pervysage.thelimitbreaker.foco.utils.sendDriveModeNotification

class DriveModeRecogReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {


        if (ActivityTransitionResult.hasResult(intent)) {
            val result = ActivityTransitionResult.extractResult(intent)
            if (result != null) {
                for (event in result.transitionEvents) {
                    if (event.activityType == DetectedActivity.ON_BICYCLE || event.activityType == DetectedActivity.IN_VEHICLE) {

                        val sharedPrefs = context?.getSharedPreferences(
                                context.getString(R.string.SHARED_PREF_KEY),
                                Context.MODE_PRIVATE
                        )
                        val am = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager

                        val dmStatus = sharedPrefs?.getBoolean(context.getString(R.string.DM_STATUS), false)
                                ?: false
                        if (event.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                            if (!dmStatus) {
                                sharedPrefs?.edit()?.putBoolean(
                                        context.getString(R.string.DM_STATUS),
                                        true
                                )?.commit()

                                am.setStreamVolume(AudioManager.STREAM_RING, 0, AudioManager.FLAG_PLAY_SOUND)

                                val notifyMsg = "DriveMode Started"
                                val contentText = "Blocking Unwanted Calls"
                                sendDriveModeNotification(notifyMsg, contentText, true, context)
                            }
                        } else if (event.transitionType == ActivityTransition.ACTIVITY_TRANSITION_EXIT) {
                            if (dmStatus) {
                                sharedPrefs?.edit()?.putBoolean(
                                        context.getString(R.string.DM_STATUS),
                                        false
                                )?.commit()


                                val geoStatus = sharedPrefs?.getBoolean(context.getString(R.string.GEO_STATUS), false)
                                        ?: false
                                if (!geoStatus) {
                                    am.setStreamVolume(AudioManager.STREAM_RING, am.getStreamMaxVolume(AudioManager.STREAM_RING), AudioManager.FLAG_PLAY_SOUND)
                                }
                                val notifyMsg = "DriveMode Stopped"
                                sendDriveModeNotification(notifyMsg, "Service Stopped", false, context)
                            }
                        }
                    }
                }
            }
        }
    }
}