package com.pervysage.thelimitbreaker.foco.database

import android.app.AlertDialog
import android.app.Application
import android.arch.lifecycle.LiveData
import android.database.sqlite.SQLiteConstraintException
import android.os.AsyncTask
import android.util.Log
import com.pervysage.thelimitbreaker.foco.database.entities.ContactInfo
import com.pervysage.thelimitbreaker.foco.database.entities.PlacePrefs
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class Repository private constructor(application: Application) {

    private var placePrefsDao:PlacePrefsDao
    private var contactsDao:ContactsDao

    private var myContacts:LiveData<List<ContactInfo>>

    private var  dialog:AlertDialog?=null
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
        dialog=exDialog
    }

    fun getPlacePref(lat:Double,lng:Double):PlacePrefs{
        return placePrefsDao.getPlacePref(lat, lng)
    }

    fun getAllContactsBackground():List<ContactInfo>{
        return contactsDao.getContactsBackground()
    }
    fun getAllPlacePrefs():List<PlacePrefs>{
        val executor = Executors.newSingleThreadExecutor()
        val getAllPrefs = Callable { placePrefsDao.getAllPrefsBackGround() }
        val future = executor.submit(getAllPrefs)
        return  future.get()
    }

    fun getInfoFromNumber(number: String):ContactInfo?{
        val executor = Executors.newSingleThreadExecutor()
        val getInfoTask= Callable { contactsDao.getInfoFromNumber(number) }
        val future = executor.submit(getInfoTask)
        return future.get()
    }
    fun getMyContacts():LiveData<List<ContactInfo>> = myContacts

    fun insertPref(prefs: PlacePrefs){
        val executor= Executors.newSingleThreadExecutor()
        val insertPrefs= Callable { placePrefsDao.insert(prefs) }
        val future = executor.submit(insertPrefs)
        return future.get()
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
        ContactQueryAsyncTask(contactsDao,QUERY_TYPE.INSERT_CONTACT).execute(contactInfo)
    }


    fun updateContact(contactInfo: ContactInfo){
        ContactQueryAsyncTask(contactsDao,QUERY_TYPE.UPDATE_CONTACT).execute(contactInfo)
    }
    fun deleteContact(contact:ContactInfo){
        ContactQueryAsyncTask(contactsDao,QUERY_TYPE.DELETE_CONTACT).execute(contact)
    }


    enum class QUERY_TYPE{
        INSERT_PREF,UPDATE_PREF,DELETE_PREF,INSERT_CONTACT,DELETE_CONTACT,UPDATE_CONTACT
    }

    private class ContactQueryAsyncTask(
            private val contactsDao: ContactsDao,
            private val queryType:Enum<QUERY_TYPE>
    ):AsyncTask<ContactInfo,Unit,String>(){
        override fun doInBackground(vararg params: ContactInfo): String {
            when(queryType){
                QUERY_TYPE.INSERT_CONTACT->{
                    contactsDao.insert(params[0])
                }
                QUERY_TYPE.DELETE_CONTACT->{
                    contactsDao.delete(params[0])
                }
                QUERY_TYPE.UPDATE_CONTACT->{
                    contactsDao.update(params[0])
                }
            }

            return "Konoyarou"
        }

    }
    init {
        val dbInstance = AppDatabase.getInstance(application)
        placePrefsDao = dbInstance.placePrefsDao()
        contactsDao=dbInstance.contactInfoDao()

        myContacts=contactsDao.getAll()
    }


}