package com.pervysage.thelimitbreaker.foco.services

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.support.v4.app.JobIntentService
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.pervysage.thelimitbreaker.foco.R
import com.pervysage.thelimitbreaker.foco.database.Repository
import com.pervysage.thelimitbreaker.foco.utils.sendNotification


class GeoActionsIntentService : JobIntentService() {

    private val TAG = "GeoActions"

    companion object {
        private val JOB_ID = 2505

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
        if (geofenceEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            val repo = Repository.getInstance(application)
            val triggeringGeofences = geofenceEvent.triggeringGeofences
            for (geofence in triggeringGeofences) {
                val geoID = geofence.requestId
                val (latStr, lngStr) = geoID.split(",")
                val lat = latStr.toDouble()
                val lng = lngStr.toDouble()
                val placePrefs = repo.getPlacePref(lat, lng)
                val notifyMsg = "Entered : ${placePrefs.name}"
                toggleService(true,placePrefs.contactGroup)
                sendNotification(notifyMsg, Geofence.GEOFENCE_TRANSITION_ENTER, baseContext)
                break
            }
        }else if(geofenceEvent.geofenceTransition==Geofence.GEOFENCE_TRANSITION_EXIT){
            toggleService(false,"")
            sendNotification("Exit", Geofence.GEOFENCE_TRANSITION_EXIT, baseContext)
        }
    }


    private fun toggleService(doStart: Boolean,activeContactGroup:String){
        val am = baseContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val sharedPref = baseContext.getSharedPreferences(getString(R.string.SHARED_PREF_KEY),Context.MODE_PRIVATE)
        if (doStart){
            am.ringerMode=AudioManager.RINGER_MODE_SILENT
            am.setStreamVolume(AudioManager.STREAM_RING,0,AudioManager.FLAG_PLAY_SOUND)
            val maxVolume = (am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)*0.90).toInt()
            am.setStreamVolume(AudioManager.STREAM_MUSIC,maxVolume,AudioManager.FLAG_PLAY_SOUND)
        }else{
            am.ringerMode=AudioManager.RINGER_MODE_NORMAL
            val maxVolume = (am.getStreamMaxVolume(AudioManager.STREAM_RING)*0.90).toInt()
            am.setStreamVolume(AudioManager.STREAM_RING,maxVolume,AudioManager.FLAG_PLAY_SOUND)
        }
        with(sharedPref.edit()){
            putBoolean(getString(R.string.SERVICE_STATUS),doStart)
            putString(getString(R.string.ACTIVE_CONTACT_GROUP),activeContactGroup)
        }.apply()
    }


}
