package com.pervysage.thelimitbreaker.foco.broadcastReceivers

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.pervysage.thelimitbreaker.foco.database.Repository
import com.pervysage.thelimitbreaker.foco.utils.GeoWorkerUtil

class BootReceiver : BroadcastReceiver() {


    private val TAG = "MyBootReceiver"
    override fun onReceive(context: Context, intent: Intent) {

        Log.d(TAG,"onReceive PUI")

        val repo= Repository.getInstance((context.applicationContext) as Application)
        val placePrefs = repo.getAllPlacePrefsBackground()
        val geoWorker = GeoWorkerUtil(context)
        for (pref in placePrefs){
            geoWorker.addPlaceForMonitoring(pref)
        }
    }
}
