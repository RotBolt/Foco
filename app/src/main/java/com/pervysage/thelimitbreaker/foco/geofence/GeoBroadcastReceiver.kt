package com.pervysage.thelimitbreaker.foco.geofence

import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.util.Log


class GeoBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("GeoBroadcast","received GeoBroadcast")
        // Enqueues a JobIntentService passing the context and intent as parameters
        GeoActionsIntentService.enqueueWork(context, intent)
    }
}