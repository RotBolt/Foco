package com.pervysage.thelimitbreaker.foco.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.pervysage.thelimitbreaker.foco.broadcastReceivers.DeleteOldRejectedReceiver
import java.util.*

fun scheduleDeleteOldRejectedCallers(context: Context) {
    val deleteOldIntent = Intent(context, DeleteOldRejectedReceiver::class.java).apply {
        putExtra(MESSAGE_NOTIFY_KEY, MESSAGE_NOTIFY_ID)
    }.let { it -> PendingIntent.getBroadcast(context, MESSAGE_NOTIFY_ID, it, PendingIntent.FLAG_UPDATE_CURRENT) }

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val calendar: Calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE,45)
    }
    if (calendar.timeInMillis>System.currentTimeMillis()) {
        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                deleteOldIntent
        )
    }

}