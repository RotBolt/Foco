package com.pervysage.thelimitbreaker.foco.broadcastReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.util.Log
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity
import com.pervysage.thelimitbreaker.foco.R
import com.pervysage.thelimitbreaker.foco.utils.sendDriveModeNotification
import com.pervysage.thelimitbreaker.foco.utils.sendGeofenceNotification

class DriveModeRecogReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        Log.d("DriveModeReceiver","intent came")
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

                        if (event.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER) {
                            sharedPrefs?.edit()?.putBoolean(
                                    context.getString(R.string.DM_STATUS),
                                    true
                            )?.apply()

                            am.setStreamVolume(AudioManager.STREAM_RING, 0, AudioManager.FLAG_PLAY_SOUND)
                            val maxVolume = (am.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * 0.90).toInt()
                            am.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, AudioManager.FLAG_PLAY_SOUND)

                            val notifyMsg = "DriveMode Started"
                            val contentText="Blocking Unwanted Calls"
                            sendDriveModeNotification(notifyMsg,contentText,true,context)
                        } else if (event.transitionType == ActivityTransition.ACTIVITY_TRANSITION_EXIT) {
                            sharedPrefs?.edit()?.putBoolean(
                                    context.getString(R.string.DM_STATUS),
                                    false
                            )?.apply()

                            am.ringerMode=AudioManager.RINGER_MODE_NORMAL
                            val maxVolume = (am.getStreamMaxVolume(AudioManager.STREAM_RING)*0.90).toInt()
                            am.setStreamVolume(AudioManager.STREAM_RING,maxVolume,AudioManager.FLAG_PLAY_SOUND)

                            val notifyMsg = "DriveMode Stopped"
                            sendDriveModeNotification(notifyMsg,"Service Stopped",false,context)
                        }
                    }
                }
            }
        }
    }
}