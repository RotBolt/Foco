package com.pervysage.thelimitbreaker.foco.services

import android.app.Application
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.location.LocationManager
import com.pervysage.thelimitbreaker.foco.database.Repository
import com.pervysage.thelimitbreaker.foco.utils.GeoWorkerUtil

class GeofenceReAddService : JobService() {
    private val JOB_ID=2528
    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }
    private fun startNewJob(){
        val component = ComponentName(baseContext,GeofenceReAddService::class.java)
        val builder = JobInfo.Builder(JOB_ID,component)
                .setBackoffCriteria(1000, JobInfo.BACKOFF_POLICY_LINEAR)
                .setMinimumLatency(30*1000)
        val scheduler=baseContext.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        scheduler.cancel(JOB_ID)
        scheduler.schedule(builder.build())
    }
    override fun onStartJob(params: JobParameters?): Boolean {
        val gpsEnabled = (baseContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager).isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (gpsEnabled){
            val repo = Repository.getInstance((applicationContext) as Application)
            val placePrefs = repo.getAllPlacePrefs()
            val geoWorker = GeoWorkerUtil(baseContext)
            for (pref in placePrefs) {
                if (pref.active == 1){
                    geoWorker.addPlaceForMonitoring(pref)
                            .addOnFailureListener {
                                jobFinished(params,false)
                                startNewJob()
                            }
                }
            }
            jobFinished(params,false)
        }else{
            jobFinished(params,false)
            startNewJob()
        }
        return false
    }


}
