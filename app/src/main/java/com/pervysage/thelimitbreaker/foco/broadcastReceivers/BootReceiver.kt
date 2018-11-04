package com.pervysage.thelimitbreaker.foco.broadcastReceivers

import android.app.Application
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.util.Log
import com.pervysage.thelimitbreaker.foco.R
import com.pervysage.thelimitbreaker.foco.database.Repository
import com.pervysage.thelimitbreaker.foco.services.GeofenceReAddService
import com.pervysage.thelimitbreaker.foco.utils.DriveActivityRecogUtil
import com.pervysage.thelimitbreaker.foco.utils.GeoWorkerUtil

class BootReceiver : BroadcastReceiver() {

    private val JOB_ID=2528

    override fun onReceive(context: Context, intent: Intent) {
        Log.i("myBootReceiver","onReceive")

        val gpsEnabled = (context.getSystemService(Context.LOCATION_SERVICE) as LocationManager).isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (gpsEnabled) {
            val repo = Repository.getInstance((context.applicationContext) as Application)
            val placePrefs = repo.getAllPlacePrefs()
            val geoWorker = GeoWorkerUtil(context)
            for (pref in placePrefs) {
                if (pref.active == 1) geoWorker.addPlaceForMonitoring(pref)
            }
        }else{

            val component = ComponentName(context,GeofenceReAddService::class.java)
            val builder = JobInfo.Builder(JOB_ID,component)
                    .setBackoffCriteria(1000,JobInfo.BACKOFF_POLICY_LINEAR)
                    .setMinimumLatency(30*1000)
            val scheduler=context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            scheduler.schedule(builder.build())
        }

        val sharedPrefs = context.getSharedPreferences(context.getString(R.string.SHARED_PREF_KEY), Context.MODE_PRIVATE)
        val isDriveModeEnabled = sharedPrefs?.getInt(context.resources.getString(R.string.DRIVE_MODE_ENABLED), -1)
                ?: 0
        if (isDriveModeEnabled == 1) {
            DriveActivityRecogUtil(context).startDriveModeRecog()
        }

        with(sharedPrefs.edit()){
            putBoolean(context.getString(R.string.GEO_STATUS),false)
            putString(context.getString(R.string.GEO_ACTIVE_GROUP),"")
            putString(context.getString(R.string.ACTIVE_NAME),"")
            putString(context.getString(R.string.ACTIVE_LAT),"")
            putString(context.getString(R.string.ACTIVE_LNG),"")
        }.commit()
    }
}
