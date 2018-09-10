package com.pervysage.thelimitbreaker.foco.database

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore


@Entity(
        primaryKeys = ["latitude","longitude"],
        tableName = "place_prefs"
)
data class PlacePrefs(
        var name:String,
        var address:String,

        @ColumnInfo(name="latitude")
        var latitude:Double,

        @ColumnInfo(name="longitude")
        var longitude:Double,
        var hour:Int,
        var minutes:Int,
        var radius:Int,
        var active:Int,
        var contactGroup:String,
        var isExpanded:Boolean
)
