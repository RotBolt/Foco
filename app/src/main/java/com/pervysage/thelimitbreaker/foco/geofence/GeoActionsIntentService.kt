package com.pervysage.thelimitbreaker.foco.geofence

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.AudioManager
import android.os.Build
import android.support.v4.app.JobIntentService
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.pervysage.thelimitbreaker.foco.R
import com.pervysage.thelimitbreaker.foco.actvities.MainActivity
import com.pervysage.thelimitbreaker.foco.database.Repository


class GeoActionsIntentService : JobIntentService() {

    private val TAG = "GeoActions"

    companion object {
        private val JOB_ID = 2505
        var isMonitorOn=false
        var contactGroup="All Contacts"
        fun enqueueWork(context: Context, intent: Intent) {
            enqueueWork(context, GeoActionsIntentService::class.java, JOB_ID, intent)
        }
    }


    override fun onHandleWork(intent: Intent) {
        Log.d(TAG, "intent came ")
        val geofenceEvent = GeofencingEvent.fromIntent(intent)
        if (geofenceEvent.hasError()) {
            Log.d(TAG, "Error")
            val errorCode = geofenceEvent.errorCode
            when (errorCode) {
                GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> {
                    Log.e(TAG, "Geofence Not available")
                }
                GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> {
                    Log.e(TAG, "Too Many Geofences")
                }
                GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> {
                    Log.e(TAG, "Geofence too many pending intents")
                }
            }
            return
        }
        if (geofenceEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            val repo = Repository.getInstance(application)
            var notificationDetails =
                    if (geofenceEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER)
                        "Entered :"
                    else
                        "Exit :"

            val triggeringGeofences = geofenceEvent.triggeringGeofences
            for (geofence in triggeringGeofences) {
                val geoID = geofence.requestId
                val (latStr, lngStr) = geoID.split(",")
                val lat = latStr.toDouble()
                val lng = lngStr.toDouble()
                val placePrefs = repo.getPlacePref(lat, lng)
                Log.d(TAG, "placepref : ${placePrefs.name}")
                contactGroup=placePrefs.contactGroup
                val am = baseContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                if (geofenceEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                    val minVal = am.getStreamMinVolume(AudioManager.STREAM_RING)
                    Log.d(TAG,"minVol: $minVal")
                    am.adjustStreamVolume(AudioManager.STREAM_RING,minVal,AudioManager.FLAG_SHOW_UI)
                    isMonitorOn=true
                }
                else
                    am.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION,AudioManager.ADJUST_RAISE,AudioManager.FLAG_SHOW_UI)

                notificationDetails += placePrefs.name
                break
            }
            sendNotification(notificationDetails, geofenceEvent.geofenceTransition)

        }
    }

    private fun sendNotification(notificationDetails: String, geofenceTransition: Int) {

        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            // Create the channel for the notification
            val mChannel = NotificationChannel("channel_id", name, NotificationManager.IMPORTANCE_DEFAULT)
            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel)
        }

        // Create an explicit content Intent that starts the main Activity.
        val notificationIntent = Intent(applicationContext, MainActivity::class.java)

        // Construct a task stack.
        val stackBuilder = TaskStackBuilder.create(this)

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MainActivity::class.java)

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent)

        // Get a PendingIntent containing the entire back stack.
        val notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        // Get a notification builder that's compatible with platform versions >= 4
        val builder = NotificationCompat.Builder(this)

        // Define the notification settings.
        builder.setSmallIcon(R.drawable.ic_launcher_foreground)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(resources,
                        R.drawable.ic_launcher_foreground))
                .setColor(Color.BLUE)
                .setContentTitle(notificationDetails)
                .setContentText("Monitoring Calls")
                .setContentIntent(notificationPendingIntent)

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId("channel_id") // Channel ID
        }
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER)
            builder.setAutoCancel(true)
        else
            builder.setAutoCancel(true)

        // Issue the notification
        mNotificationManager.notify(0, builder.build())
    }


}
