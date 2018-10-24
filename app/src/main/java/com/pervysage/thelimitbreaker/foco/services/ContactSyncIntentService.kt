package com.pervysage.thelimitbreaker.foco.services

import android.app.Application
import android.app.IntentService
import android.content.Intent
import android.provider.ContactsContract
import android.util.Log
import com.pervysage.thelimitbreaker.foco.database.Repository
import com.pervysage.thelimitbreaker.foco.database.entities.ContactInfo


class ContactSyncIntentService : IntentService("ContactSyncIntentService") {

    override fun onHandleIntent(intent: Intent?) {

        val repository = Repository.getInstance((baseContext.applicationContext) as Application)
        val contacts = repository.getAllContacts()

        if (contacts.isNotEmpty()) {
            val selectionArgs = ArrayList<String>()
            for (contact in contacts) {
                selectionArgs.add(contact.number)
            }
            var selectionString = ""
            for (i in selectionArgs.indices) {
                selectionString += "${ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER} LIKE ? OR "
            }
            selectionString = selectionString.substring(0, selectionString.length - 4)
            val cursor = contentResolver.query(
                    ContactsContract.Data.CONTENT_URI,
                    arrayOf(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME),
                    selectionString,
                    selectionArgs.toArray(Array(selectionArgs.size) { i -> selectionArgs[i] }),
                    null
            )
            val hashMap = HashMap<String, String>()
            cursor?.run {
                while (cursor.moveToNext()) {
                    val number = getString(getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER))
                    val name = getString(getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                    hashMap[number] = name
                }
            }

            val listUpdate = ArrayList<ContactInfo>()
            val listDelete = ArrayList<ContactInfo>()
            for (contact in contacts) {

                if (hashMap.containsKey(contact.number)) {
                    val mapContactName = hashMap[contact.number]
                    mapContactName?.run {
                        if (contact.name != mapContactName) {
                            listUpdate.add(ContactInfo(this, contact.number))
                        }
                    }
                } else {
                    listDelete.add(contact)
                }
            }
            repository.updateContact(*listUpdate.toTypedArray())
            repository.deleteContact(*listDelete.toTypedArray())
        }
    }


}
