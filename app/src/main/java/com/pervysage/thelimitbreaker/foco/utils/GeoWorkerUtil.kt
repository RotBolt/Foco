package com.pervysage.thelimitbreaker.foco.utils

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.pervysage.thelimitbreaker.foco.broadcastReceivers.GeoBroadcastReceiver
import com.pervysage.thelimitbreaker.foco.database.entities.PlacePrefs

class GeoWorkerUtil(private val context: Context){
    private val geofenceClient = LocationServices.getGeofencingClient(context)

    private val TAG="GeoWorkerUtil"

    @SuppressLint("MissingPermission")
    fun addPlaceForMonitoring(placePrefs: PlacePrefs){
        val geoID = "${placePrefs.latitude},${placePrefs.longitude}"
        val thisGeofence = Geofence.Builder().apply {
            setRequestId(geoID)
            setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            setExpirationDuration(Geofence.NEVER_EXPIRE)
            setCircularRegion(placePrefs.latitude,placePrefs.longitude,placePrefs.radius.toFloat())
        }.build()

        geofenceClient.addGeofences(getGeofenceRequest(thisGeofence),getPendingIntent(placePrefs.geoKey))
                .addOnSuccessListener {
                    Log.d(TAG,"Geofence Added")
                }
                .addOnFailureListener {
                    Log.d(TAG,"Geofence Add Failed $it")
                }

    }

    fun removePlaceFromMonitoring(placePrefs: PlacePrefs):Task<Void>{
        val requestID=placePrefs.geoKey
        return geofenceClient.removeGeofences(getPendingIntent(requestID))
                .addOnSuccessListener {
                    Log.d(TAG,"Removed Successfully")
                }
                .addOnFailureListener {
                    Log.d(TAG,"Successfully Failed $it")
                }

    }

    fun updatePlacePrefsForMonitoring(placePrefs: PlacePrefs){
        removePlaceFromMonitoring(placePrefs)
                .addOnSuccessListener {
                    addPlaceForMonitoring(placePrefs)
                }
                .addOnFailureListener {
                    Log.d(TAG,"Successfully Failed during Updating $it")
                }
    }

    private fun getGeofenceRequest(geofence: Geofence):GeofencingRequest{
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofence(geofence)
        }.build()
    }

    private fun getPendingIntent(requestID:Int):PendingIntent{
        val intent = Intent(context, GeoBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(context,requestID,intent,PendingIntent.FLAG_UPDATE_CURRENT)
    }
}