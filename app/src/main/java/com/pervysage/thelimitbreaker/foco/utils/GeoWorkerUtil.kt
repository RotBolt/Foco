package com.pervysage.thelimitbreaker.foco.utils

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.crashlytics.android.Crashlytics
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.pervysage.thelimitbreaker.foco.broadcastReceivers.GeoBroadcastReceiver
import com.pervysage.thelimitbreaker.foco.database.entities.PlacePrefs

class GeoWorkerUtil(private val context: Context){
    private val geofenceClient = LocationServices.getGeofencingClient(context)

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
                .addOnFailureListener {
                    Crashlytics.log("GeoWorkerUtil $it")
                    Toast.makeText(context,"Oops, something went wrong. Please try again", Toast.LENGTH_SHORT).show()
                }

    }

    fun removePlaceFromMonitoring(placePrefs: PlacePrefs):Task<Void>{
        val requestID=placePrefs.geoKey
        return geofenceClient.removeGeofences(getPendingIntent(requestID))
                .addOnFailureListener {
                    Crashlytics.log("GeoWorkerUtil $it")
                    Toast.makeText(context,"Oops, something went wrong. Please try again", Toast.LENGTH_SHORT).show()
                }

    }

    fun updatePlacePrefsForMonitoring(placePrefs: PlacePrefs){
        removePlaceFromMonitoring(placePrefs)
                .addOnSuccessListener {
                    addPlaceForMonitoring(placePrefs)
                }
                .addOnFailureListener {
                    Crashlytics.log("GeoWorkerUtil $it")
                    Toast.makeText(context,"Oops, something went wrong. Please try again", Toast.LENGTH_SHORT).show()
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