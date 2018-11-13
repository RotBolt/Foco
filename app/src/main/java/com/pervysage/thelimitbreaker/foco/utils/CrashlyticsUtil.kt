package com.pervysage.thelimitbreaker.foco.utils

import android.content.Context
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric

fun initCrashlytics(context: Context){
    Fabric.with(Fabric.Builder(context)
            .kits(Crashlytics())
            .debuggable(false)  // Enables Crashlytics debugger
            .build())
}