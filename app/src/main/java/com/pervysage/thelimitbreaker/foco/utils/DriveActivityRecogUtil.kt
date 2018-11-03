package com.pervysage.thelimitbreaker.foco.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.crashlytics.android.Crashlytics
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.DetectedActivity
import com.pervysage.thelimitbreaker.foco.broadcastReceivers.DriveModeRecogReceiver

class DriveActivityRecogUtil(private val context:Context){
    private val activityRecogClient = ActivityRecognition.getClient(context)

    fun startDriveModeRecog(){

        val transitions = ArrayList<ActivityTransition>()

        val activityTypes = IntArray(2)
        activityTypes[0]=DetectedActivity.IN_VEHICLE
        activityTypes[1]=DetectedActivity.ON_BICYCLE

        for (type in activityTypes){
            transitions.add(
                    ActivityTransition.Builder()
                            .setActivityType(type)
                            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                            .build()
            )
            transitions.add(
                    ActivityTransition.Builder()
                            .setActivityType(type)
                            .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                            .build()
            )
        }

        val activityTransitRequest = ActivityTransitionRequest(transitions)
        val pendingIntent = PendingIntent.getBroadcast(
                context,
                2505,
                Intent(context,DriveModeRecogReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        val task=activityRecogClient.requestActivityTransitionUpdates(activityTransitRequest,pendingIntent)
        task.addOnSuccessListener {
           Toast.makeText(context,"Drive Mode Activated",Toast.LENGTH_SHORT).show()
        }
        task.addOnFailureListener {
            Crashlytics.logException(it)
            Toast.makeText(context,"Oops, something went wrong. Please try again",Toast.LENGTH_SHORT).show()
        }
    }

    fun stopDriveModeRecog(){
        val pendingIntent = PendingIntent.getBroadcast(
                context,
                2505,
                Intent(context,DriveModeRecogReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        val task = activityRecogClient.removeActivityTransitionUpdates(pendingIntent)

        task.addOnSuccessListener {
            Toast.makeText(context,"Drive Mode Deactivated",Toast.LENGTH_SHORT).show()
        }
        task.addOnFailureListener {
            Crashlytics.logException(it)
            Toast.makeText(context,"Oops, something went wrong. Please try again",Toast.LENGTH_SHORT).show()
        }
    }
}