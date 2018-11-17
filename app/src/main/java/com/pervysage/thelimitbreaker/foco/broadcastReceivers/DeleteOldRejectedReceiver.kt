package com.pervysage.thelimitbreaker.foco.broadcastReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pervysage.thelimitbreaker.foco.R
import com.pervysage.thelimitbreaker.foco.utils.MESSAGE_NOTIFY_ID
import com.pervysage.thelimitbreaker.foco.utils.MESSAGE_NOTIFY_KEY
import com.pervysage.thelimitbreaker.foco.utils.scheduleDeleteOldRejectedCallers

class DeleteOldRejectedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationId = intent.extras?.getInt(MESSAGE_NOTIFY_KEY)?:0
        if (notificationId== MESSAGE_NOTIFY_ID){
            val sharedPrefs = context.
                    getSharedPreferences(context.getString(R.string.SHARED_PREF_KEY),Context.MODE_PRIVATE)
            sharedPrefs.edit().apply {
                putString(context.getString(R.string.REJECTED_CALLERS_KEY),"")
                putString(context.getString(R.string.REJECTED_NUMBERS_KEY),"")
                putString(context.getString(R.string.REJECTED_TIME),"")
            }.commit()

            scheduleDeleteOldRejectedCallers(context)
        }
    }
}
