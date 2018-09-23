package com.pervysage.thelimitbreaker.foco.database

import android.app.Application
import android.arch.lifecycle.LiveData
import android.os.AsyncTask
import android.util.Log
import com.pervysage.thelimitbreaker.foco.database.entities.ContactInfo
import com.pervysage.thelimitbreaker.foco.database.entities.PlacePrefs

class Repository private constructor(application: Application) {

    private var placePrefsDao:PlacePrefsDao
    private var contactsDao:ContactsDao
    private var allPlacePrefs:LiveData<List<PlacePrefs>>
    private var myContacts:LiveData<List<ContactInfo>>

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


    fun getMyContacts():LiveData<List<ContactInfo>> = myContacts

    fun getAllPlacePrefs():LiveData<List<PlacePrefs>> = allPlacePrefs

    fun insertPref(prefs: PlacePrefs){
        DbQueryAsyncTask(placePrefsDao,QUERY_TYPE.INSERT_PREF).execute(prefs)
    }

    fun updatePref(prefs: PlacePrefs){
        DbQueryAsyncTask(placePrefsDao,QUERY_TYPE.UPDATE_PREF).execute(prefs)
    }

    fun deletePref(prefs: PlacePrefs){
        DbQueryAsyncTask(placePrefsDao,QUERY_TYPE.DELETE_PREF).execute(prefs)
    }
    fun insertContact(contactInfo:ContactInfo){
        ContactQueryAsyncTask(contactsDao,QUERY_TYPE.INSERT_CONTACT).execute(contactInfo)
    }


    fun deleteContact(contact:ContactInfo){
        ContactQueryAsyncTask(contactsDao,QUERY_TYPE.DELETE_CONTACT).execute(contact)
    }


    enum class QUERY_TYPE{
        INSERT_PREF,UPDATE_PREF,DELETE_PREF,INSERT_CONTACT,DELETE_CONTACT
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
            }

            return "Konoyarou"
        }

    }

    private class DbQueryAsyncTask(
            private val prefDao:PlacePrefsDao,
            private val queryType:Enum<QUERY_TYPE>
    ):AsyncTask<PlacePrefs,Unit,String>(){
        private val TAG="Repository"
        override fun doInBackground(vararg params: PlacePrefs?): String {
            when(queryType){
                QUERY_TYPE.INSERT_PREF->{
                    prefDao.insert(params[0]!!)
                    return "Insert Success"
                }
                QUERY_TYPE.UPDATE_PREF->{
                    prefDao.update(params[0]!!)
                    return "Update Success"
                }
                QUERY_TYPE.DELETE_PREF->{
                    prefDao.delete(params[0]!!)
                    return "Delete Success"
                }
            }
            return " Konoyarou!! "
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            Log.d(TAG,"$result")
        }
    }

    init {
        val dbInstance = AppDatabase.getInstance(application)
        placePrefsDao = dbInstance.placePrefsDao()
        contactsDao=dbInstance.contactInfoDao()
        allPlacePrefs = placePrefsDao.getAllPrefs()
        myContacts=contactsDao.getAll()
    }


}