package com.pervysage.thelimitbreaker.foco.database

import android.app.Application
import android.arch.lifecycle.LiveData
import android.os.AsyncTask
import android.util.Log
import com.pervysage.thelimitbreaker.foco.database.entities.PlacePrefs

class Repository private constructor(application: Application) {

    private var placePrefsDao:PlacePrefsDao
    private var allPlacePrefs:LiveData<List<PlacePrefs>>

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


    fun getAllPlacePrefs():LiveData<List<PlacePrefs>> = allPlacePrefs

    fun insert(prefs: PlacePrefs){
        DbQueryAsyncTask(placePrefsDao,QUERY_TYPE.INSERT).execute(prefs)
    }

    fun update(prefs: PlacePrefs){
        DbQueryAsyncTask(placePrefsDao,QUERY_TYPE.UPDATE).execute(prefs)
    }

    fun delete(prefs: PlacePrefs){
        DbQueryAsyncTask(placePrefsDao,QUERY_TYPE.DELETE).execute(prefs)
    }


    enum class QUERY_TYPE{
        INSERT,UPDATE,DELETE
    }

    private class DbQueryAsyncTask(
            private val dao:PlacePrefsDao,
            private val queryType:Enum<QUERY_TYPE>
    ):AsyncTask<PlacePrefs,Unit,String>(){
        private val TAG="Repository"
        override fun doInBackground(vararg params: PlacePrefs?): String {
            when(queryType){
                QUERY_TYPE.INSERT->{
                    dao.insert(params[0]!!)
                    return "Insert Success"
                }
                QUERY_TYPE.UPDATE->{
                    dao.update(params[0]!!)
                    return "Update Success"
                }
                QUERY_TYPE.DELETE->{
                    dao.delete(params[0]!!)
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
        allPlacePrefs = placePrefsDao.getAllPrefs()
    }


}