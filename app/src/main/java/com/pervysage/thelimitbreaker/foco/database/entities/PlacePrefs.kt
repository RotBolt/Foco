package com.pervysage.thelimitbreaker.foco.database.entities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import java.util.*


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
        var geoKey:Int,
        var radius:Int,
        var active:Int,
        var contactGroup:String,
        var isExpanded:Boolean = false
)


fun generateGeoKey():Int{
    val rand = Random(System.currentTimeMillis())
    return rand.nextInt(Int.MAX_VALUE)
}
