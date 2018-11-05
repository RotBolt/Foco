package com.pervysage.thelimitbreaker.foco.actvities

import android.arch.lifecycle.Observer
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.pervysage.thelimitbreaker.foco.R
import com.pervysage.thelimitbreaker.foco.adapters.MyContactsAdapter
import com.pervysage.thelimitbreaker.foco.database.Repository
import com.pervysage.thelimitbreaker.foco.database.entities.ContactInfo
import com.pervysage.thelimitbreaker.foco.dialogs.ContactInfoDialog
import com.pervysage.thelimitbreaker.foco.services.ContactSyncIntentService
import kotlinx.android.synthetic.main.activity_my_contacts.*
import kotlin.collections.ArrayList

class MyContactsActivity : AppCompatActivity() {

    private var areContactEmpty = true

    private lateinit var contactAdapter: MyContactsAdapter

    private fun toggleViews() {
        if (areContactEmpty) {
            ivContactHeader.visibility = View.VISIBLE
            tvNoContacts.visibility = View.VISIBLE
            rvMyContacts.visibility = View.GONE
        } else {
            ivContactHeader.visibility = View.GONE
            tvNoContacts.visibility = View.GONE
            rvMyContacts.visibility = View.VISIBLE
        }
    }


    private lateinit var repository: Repository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_contacts)

        repository = Repository.getInstance(application)

        contactAdapter = MyContactsAdapter(ArrayList(), this)

        rvMyContacts.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvMyContacts.adapter = contactAdapter

        val contactsLive = repository.getAllContactsLive()
        contactsLive.observe(this, Observer<List<ContactInfo>> { it ->
            it?.run {
                val sortedList = sortedBy { it.name }
                areContactEmpty = this.isEmpty()
                toggleViews()
                contactAdapter.updateList(sortedList)
            }
        })

        contactAdapter.setOnContactClickListener { name, numbers, colors ->

            val dialog = ContactInfoDialog()
            dialog.setParams(name, numbers, colors)
            dialog.setOnContactDeleteListener {
                contactsLive.value?.run {
                    val list = ArrayList<ContactInfo>()
                    for (contact in this) {
                        if (name == contact.name) {
                            list.add(contact)
                        }
                    }
                    repository.deleteContact(*list.toTypedArray())
                    dialog.dismiss()
                }

            }
            dialog.show(supportFragmentManager, "ContactInfoDialog")
        }


        ivAddContact.setOnClickListener {
            startActivity(Intent(this@MyContactsActivity, PickContactsActivity::class.java))
        }

        ivBack.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        startService(Intent(this, ContactSyncIntentService::class.java))
    }
}
