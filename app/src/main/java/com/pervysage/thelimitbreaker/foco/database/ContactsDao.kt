package com.pervysage.thelimitbreaker.foco.database

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*
import com.pervysage.thelimitbreaker.foco.database.entities.ContactInfo


@Dao
interface ContactsDao {

    @Insert(onConflict = OnConflictStrategy.ROLLBACK)
    fun insert(contactInfo:ContactInfo)

    @Delete
    fun delete(contactInfo:ContactInfo)

    @Query("SELECT * FROM contact_info ORDER BY name")
    fun getAll():LiveData<List<ContactInfo>>

}