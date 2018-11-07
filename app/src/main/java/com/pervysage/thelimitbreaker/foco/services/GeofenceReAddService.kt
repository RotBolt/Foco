package com.pervysage.thelimitbreaker.foco.services

import android.app.Application
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.IBinder
import com.pervysage.thelimitbreaker.foco.database.Repository
import com.pervysage.thelimitbreaker.foco.utils.GeoWorkerUtil

class GeofenceReAddService : Service() {


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val gpsEnabled = (baseContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager).isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (gpsEnabled){
            val repo = Repository.getInstance((applicationContext) as Application)
            val placePrefs = repo.getAllPlacePrefs()
            val geoWorker = GeoWorkerUtil(baseContext)
            for (pref in placePrefs) {
                if (pref.active == 1){
                    geoWorker.addPlaceForMonitoring(pref)
                            .addOnFailureListener {
                                stopSelf()
                                baseContext.startService(intent)
                            }
                }
            }
            stopSelf()
        }else{
            stopSelf()
            baseContext.startService(intent)
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
