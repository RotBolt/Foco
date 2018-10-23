package com.pervysage.thelimitbreaker.foco.database

import android.app.AlertDialog
import android.app.Application
import android.arch.lifecycle.LiveData
import com.pervysage.thelimitbreaker.foco.database.entities.ContactInfo
import com.pervysage.thelimitbreaker.foco.database.entities.PlacePrefs
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors

class Repository private constructor(application: Application) {

    private var placePrefsDao:PlacePrefsDao
    private var contactsDao:ContactsDao
    private var  exceptionDialog:AlertDialog?=null
    companion object {
        private var instance:Repository?=null
        fun getInstance(application: Application):Repository{
            if(instance==null){
                synchronized(Repository::class.java){
                    if(instance==null){
                        instance= Repository(application)
                    }
                }
            }
            return instance!!
        }
    }

    fun setExceptionDialog(exDialog:AlertDialog){
        exceptionDialog=exDialog
    }

    fun getPlacePref(lat:Double,lng:Double):PlacePrefs?{
        return placePrefsDao.getPlacePref(lat, lng)
    }

    fun getAllContacts():List<ContactInfo>{
        val executor = Executors.newSingleThreadExecutor()
        val getAllPrefs = Callable { contactsDao.getAllContacts() }
        val future = executor.submit(getAllPrefs)
        return  future.get()
    }
    fun getAllPlacePrefs():List<PlacePrefs>{
        val executor = Executors.newSingleThreadExecutor()
        val getAllPrefs = Callable { placePrefsDao.getAllPrefs() }
        val future = executor.submit(getAllPrefs)
        return  future.get()
    }

    fun getInfoFromNumber(number: String):ContactInfo?{
        val executor = Executors.newSingleThreadExecutor()
        val getInfoTask= Callable { contactsDao.getInfoFromNumber(number) }
        val future = executor.submit(getInfoTask)
        return future.get()
    }


    fun insertPref(prefs: PlacePrefs):Boolean{
        val executor= Executors.newSingleThreadExecutor()
        val insertPrefs= Callable { placePrefsDao.insert(prefs) }
        return try {
            val future = executor.submit(insertPrefs)
            future.get()
            true
        }catch (ee:ExecutionException){
            exceptionDialog?.show()
            false
        }

    }

    fun updatePref(prefs: PlacePrefs){
        val executor= Executors.newSingleThreadExecutor()
        val updatePrefs= Callable { placePrefsDao.update(prefs) }
        val future = executor.submit(updatePrefs)
        return future.get()
    }

    fun deletePref(prefs: PlacePrefs){
        val executor= Executors.newSingleThreadExecutor()
        val deletePrefs= Callable { placePrefsDao.delete(prefs) }
        val future = executor.submit(deletePrefs)
        return future.get()
    }


    fun insertContact(contactInfo:ContactInfo){
        val executor = Executors.newSingleThreadExecutor()
        val getAllPrefs = Callable { contactsDao.insert(contactInfo) }
        val future = executor.submit(getAllPrefs)
        return  future.get()
    }


    fun updateContact(contactInfo: ContactInfo){
        val executor = Executors.newSingleThreadExecutor()
        val getAllPrefs = Callable { contactsDao.update(contactInfo) }
        val future = executor.submit(getAllPrefs)
        return  future.get()
    }
    fun deleteContact(contactInfo:ContactInfo){
        val executor = Executors.newSingleThreadExecutor()
        val getAllPrefs = Callable { contactsDao.delete(contactInfo) }
        val future = executor.submit(getAllPrefs)
        return  future.get()
    }
    init {
        val dbInstance = AppDatabase.getInstance(application)
        placePrefsDao = dbInstance.placePrefsDao()
        contactsDao=dbInstance.contactInfoDao()

    }


}