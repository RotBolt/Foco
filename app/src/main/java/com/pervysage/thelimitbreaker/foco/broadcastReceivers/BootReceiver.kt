package com.pervysage.thelimitbreaker.foco.broadcastReceivers

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pervysage.thelimitbreaker.foco.database.Repository
import com.pervysage.thelimitbreaker.foco.utils.GeoWorkerUtil

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val repo= Repository.getInstance((context.applicationContext) as Application)
        val placePrefs = repo.getAllPlacePrefsBackground()
        val geoWorker = GeoWorkerUtil(context)
        for (pref in placePrefs){
            geoWorker.addPlaceForMonitoring(pref)
        }
    }
}
