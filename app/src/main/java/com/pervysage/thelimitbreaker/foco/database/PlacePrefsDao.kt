package com.pervysage.thelimitbreaker.foco.database

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.persistence.room.*
import android.database.sqlite.SQLiteConstraintException
import com.pervysage.thelimitbreaker.foco.database.entities.PlacePrefs


@Dao
interface PlacePrefsDao{

    @Insert
    @Throws(SQLiteConstraintException::class)
    fun insert(placePrefs: PlacePrefs)

    @Update
    fun update(placePrefs: PlacePrefs)

    @Delete
    fun delete(placePrefs: PlacePrefs)

    @Query("SELECT * FROM place_prefs")
    fun getAllPrefs(): List<PlacePrefs>



    @Query("SELECT * FROM place_prefs WHERE latitude LIKE :lat AND longitude LIKE :lng LIMIT 1")
    fun getPlacePref(lat:Double,lng:Double):PlacePrefs

}