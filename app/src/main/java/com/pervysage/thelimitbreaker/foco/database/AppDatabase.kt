package com.pervysage.thelimitbreaker.foco.database

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.pervysage.thelimitbreaker.foco.database.entities.ContactInfo
import com.pervysage.thelimitbreaker.foco.database.entities.PlacePrefs

@Database(entities = [PlacePrefs::class,ContactInfo::class],version = 1)
abstract class AppDatabase:RoomDatabase(){
    abstract fun placePrefsDao():PlacePrefsDao

    abstract fun contactInfoDao():ContactsDao

    companion object {
        private var dbInstance:AppDatabase?=null
        fun getInstance(context: Context):AppDatabase{
            if(dbInstance==null){
                synchronized(AppDatabase::class){
                    if(dbInstance==null){
                        dbInstance= Room.databaseBuilder(
                                context,
                                AppDatabase::class.java,
                                "foco.db"
                        ).build()
                    }
                }
            }
            return dbInstance!!
        }
    }
}