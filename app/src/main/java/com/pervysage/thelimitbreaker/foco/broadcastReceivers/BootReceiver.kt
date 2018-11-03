package com.pervysage.thelimitbreaker.foco.broadcastReceivers

import android.app.AlertDialog
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import android.util.Log
import com.pervysage.thelimitbreaker.foco.R
import com.pervysage.thelimitbreaker.foco.database.Repository
import com.pervysage.thelimitbreaker.foco.utils.DriveActivityRecogUtil
import com.pervysage.thelimitbreaker.foco.utils.GeoWorkerUtil

class BootReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent) {
        Log.d("myBootReceiver","onReceive")
        val repo = Repository.getInstance((context.applicationContext) as Application)
        val placePrefs = repo.getAllPlacePrefs()
        val geoWorker = GeoWorkerUtil(context)
        for (pref in placePrefs) {
            if (pref.active == 1) geoWorker.addPlaceForMonitoring(pref)
        }

        val sharedPrefs = context.getSharedPreferences(context.getString(R.string.SHARED_PREF_KEY), Context.MODE_PRIVATE)
        val isDriveModeEnabled = sharedPrefs?.getInt(context.resources.getString(R.string.DRIVE_MODE_ENABLED), -1)
                ?: 0
        if (isDriveModeEnabled == 1) {
            DriveActivityRecogUtil(context).startDriveModeRecog()
        }
    }
}
