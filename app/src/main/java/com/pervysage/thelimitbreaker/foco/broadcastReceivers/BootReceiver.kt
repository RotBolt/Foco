package com.pervysage.thelimitbreaker.foco.broadcastReceivers

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import com.pervysage.thelimitbreaker.foco.R
import com.pervysage.thelimitbreaker.foco.services.GeofenceReAddJob
import com.pervysage.thelimitbreaker.foco.services.GeofenceReAddService
import com.pervysage.thelimitbreaker.foco.utils.DriveActivityRecogUtil

class BootReceiver : BroadcastReceiver() {

    private val JOB_ID = 2528

    private fun startReAddGeofenceJob(context: Context){
        val component = ComponentName(context, GeofenceReAddJob::class.java)
        val builder = JobInfo.Builder(JOB_ID, component)
                .setBackoffCriteria(1000, JobInfo.BACKOFF_POLICY_LINEAR)
                .setMinimumLatency(30 * 1000)
        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        scheduler.schedule(builder.build())
    }

    override fun onReceive(context: Context, intent: Intent) {


        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            startReAddGeofenceJob(context)
        }else{
            try {
                context.startService(Intent(context,GeofenceReAddService::class.java))
            }catch (ile:IllegalStateException){
                startReAddGeofenceJob(context)
            }catch (e:Exception){
                startReAddGeofenceJob(context)
            }

        }

        val sharedPrefs = context.getSharedPreferences(context.getString(R.string.SHARED_PREF_KEY), Context.MODE_PRIVATE)
        val isDriveModeEnabled = sharedPrefs?.getInt(context.resources.getString(R.string.DRIVE_MODE_ENABLED), -1)
                ?: 0
        if (isDriveModeEnabled == 1) {
            DriveActivityRecogUtil(context).startDriveModeRecog()
        }

        with(sharedPrefs.edit()) {
            putBoolean(context.getString(R.string.GEO_STATUS), false)
            putString(context.getString(R.string.GEO_ACTIVE_GROUP), "")
            putString(context.getString(R.string.ACTIVE_NAME), "")
            putString(context.getString(R.string.ACTIVE_LAT), "")
            putString(context.getString(R.string.ACTIVE_LNG), "")
        }.commit()
    }
}
