package com.pervysage.thelimitbreaker.foco.actvities

import android.app.Activity
import android.os.Bundle
import android.provider.ContactsContract
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.widget.Toast
import com.pervysage.thelimitbreaker.foco.R
import com.pervysage.thelimitbreaker.foco.adapters.ContactAdapter
import com.pervysage.thelimitbreaker.foco.database.Repository
import com.pervysage.thelimitbreaker.foco.database.entities.ContactInfo
import kotlinx.android.synthetic.main.activity_pick_contacts.*
import java.util.*
import kotlin.collections.ArrayList

class PickContactsActivity : AppCompatActivity() {

    private var count = 0

    data class ContactModel(
            val name: String,
            val lookUpKey: String,
            var isChecked: Boolean
    )

    private val marked = ArrayList<ContactModel>()

    private lateinit var repository: Repository


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_contacts)

        repository = Repository.getInstance(application)

        val contactAdapter = ContactAdapter(getAllContacts(), this)
        rvContacts.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvContacts.adapter = contactAdapter

        fastScroller.setRecyclerView(rvContacts)
        ivClose.setOnClickListener {
            finish()
        }

        contactAdapter.setOnContactCheckListener {
            if (it.isChecked) {
                it.isChecked = false
                count--
                tvCount.text = "$count"
                marked.remove(it)
            } else {
                it.isChecked = true
                count++
                tvCount.text = "$count"
                marked.add(it)
            }
        }
        ivDone.setOnClickListener {

            val projection = arrayOf(
                    ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER
            )
            val selection = "${ContactsContract.Contacts.LOOKUP_KEY} = ? AND ${ContactsContract.Contacts.DISPLAY_NAME} = ? AND ${ContactsContract.Data.MIMETYPE} = ?"
            for (obj in marked) {
                val cursor = contentResolver.query(
                        ContactsContract.Data.CONTENT_URI,
                        projection,
                        selection,
                        arrayOf(obj.lookUpKey, obj.name, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE),
                        null
                )
                cursor?.run {
                    if (cursor.count > 0) {

                        val list = ArrayList<ContactInfo>()
                        while (cursor.moveToNext()) {
                            val number = getString(getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER))
                            val info = ContactInfo(obj.name, number)
                            list.add(info)
                        }
                        repository.insertContact(*list.toTypedArray())
                    } else {
                        Toast.makeText(this@PickContactsActivity, "No Number found for ${obj.name}", Toast.LENGTH_SHORT).show()
                    }
                    close()
                }
            }

            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    private fun getAllContacts(): List<ContactModel> {
        val contactList = ArrayList<ContactModel>()
        val contactMap = HashMap<String, String>()
        val oldMap = getOrderedContactMap()

        val cursor = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                arrayOf(
                        ContactsContract.Contacts.LOOKUP_KEY,
                        ContactsContract.Contacts.DISPLAY_NAME
                ),
                null,
                null,
                "${ContactsContract.Contacts.DISPLAY_NAME} ASC"
        )
        cursor?.run {
            while (cursor.moveToNext()) {
                val name = getString(getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                val lookUpKey = getString(getColumnIndexOrThrow(ContactsContract.Contacts.LOOKUP_KEY))
                if (oldMap.isNotEmpty() && !oldMap.containsKey(name))
                    contactMap[name] = lookUpKey
                else if (oldMap.isEmpty()) {
                    contactMap[name] = lookUpKey
                }
            }
        }
        contactMap.mapTo(contactList) {
            ContactModel(it.key, it.value, false)
        }
        contactList.sortWith(Comparator { o1, o2 ->
            o1.name.compareTo(o2.name)
        })
        return contactList
    }

    private fun getOrderedContactMap(): HashMap<String, ArrayList<String>> {
        val list = repository.getAllContacts()
        val orderedMap = HashMap<String, ArrayList<String>>()
        for (info in list) {
            var numbers = orderedMap[info.name]
            if (numbers == null) numbers = ArrayList()
            numbers.add(info.number)
            orderedMap[info.name] = numbers
        }
        return orderedMap
    }


}

