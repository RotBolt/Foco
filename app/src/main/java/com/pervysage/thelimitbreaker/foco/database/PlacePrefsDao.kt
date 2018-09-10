package com.pervysage.thelimitbreaker.foco.database

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*


@Dao
interface PlacePrefsDao{

    @Insert(onConflict = OnConflictStrategy.ROLLBACK)
    fun insert(placePrefs:PlacePrefs)

    @Update
    fun update(placePrefs: PlacePrefs)

    @Delete
    fun delete(placePrefs: PlacePrefs)

    @Query("SELECT * FROM place_prefs")
    fun getAllPrefs():LiveData<List<PlacePrefs>>

}