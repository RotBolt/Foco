package com.pervysage.thelimitbreaker.foco.actvities

import android.app.Activity
import android.os.Bundle
import android.provider.ContactsContract
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.pervysage.thelimitbreaker.foco.R
import com.pervysage.thelimitbreaker.foco.adapters.ContactAdapter
import com.pervysage.thelimitbreaker.foco.database.Repository
import com.pervysage.thelimitbreaker.foco.database.entities.ContactInfo
import kotlinx.android.synthetic.main.activity_pick_conatcts.*
import java.util.*

class PickContactsActivity : AppCompatActivity() {

    private var count = 0

    data class ContactInfo(
            val name: String,
            val lookUpKey: String,
            var isChecked: Boolean
    )

    private val marked = ArrayList<ContactInfo>()

    private lateinit var repository: Repository


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_conatcts)

        repository = Repository.getInstance(application)

        val contactAdapter = ContactAdapter(getAllContacts(), this)
        rvContacts.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvContacts.adapter = contactAdapter
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
            val selection = "${ContactsContract.Contacts.LOOKUP_KEY} = ? AND ${ContactsContract.Data.MIMETYPE} = ?"
            for (obj in marked) {
                val cursor = contentResolver.query(
                        ContactsContract.Data.CONTENT_URI,
                        projection,
                        selection,
                        arrayOf(obj.lookUpKey, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE),
                        null
                )
                cursor?.run {
                    while (cursor.moveToNext()) {
                        val number = getString(getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER))

                        val info = ContactInfo(obj.name, number)
                        repository.insertContact(info)

                    }
                }
            }

            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    private fun getAllContacts(): List<ContactInfo> {
        val contactList = ArrayList<ContactInfo>()
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
            ContactInfo(it.key, it.value, false)
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

