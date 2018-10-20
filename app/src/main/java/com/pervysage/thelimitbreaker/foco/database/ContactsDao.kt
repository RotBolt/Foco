package com.pervysage.thelimitbreaker.foco.database

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.persistence.room.*
import com.pervysage.thelimitbreaker.foco.database.entities.ContactInfo


@Dao
interface ContactsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(contactInfo:ContactInfo)

    @Delete
    fun delete(contactInfo:ContactInfo)

    @Query("SELECT * FROM contact_info ORDER BY name")
    fun getAllContacts(): List<ContactInfo>

    @Update
    fun update(contactInfo: ContactInfo)

    @Query("SELECT * FROM contact_info WHERE number LIKE :number")
    fun getInfoFromNumber(number:String):ContactInfo?

}