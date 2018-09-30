package com.pervysage.thelimitbreaker.foco.broadcastReceivers

import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.util.Log
import com.pervysage.thelimitbreaker.foco.services.GeoActionsIntentService


class GeoBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("GeoBroadcast","received GeoBroadcast")
        // Enqueues a JobIntentService passing the context and intent as parameters
        GeoActionsIntentService.enqueueWork(context, intent)
    }
}