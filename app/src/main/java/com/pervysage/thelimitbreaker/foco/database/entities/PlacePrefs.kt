package com.pervysage.thelimitbreaker.foco.database.entities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import com.pervysage.thelimitbreaker.foco.expandCollapseController.ExpandableObj


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
        var radius:Int,
        var active:Int,
        var contactGroup:String
): ExpandableObj(false)
