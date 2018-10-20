package com.pervysage.thelimitbreaker.foco.actvities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.pervysage.thelimitbreaker.foco.R
import com.pervysage.thelimitbreaker.foco.adapters.MyContactsAdapter
import com.pervysage.thelimitbreaker.foco.database.Repository
import com.pervysage.thelimitbreaker.foco.dialogs.ContactInfoDialog
import com.pervysage.thelimitbreaker.foco.services.ContactSyncIntentService
import kotlinx.android.synthetic.main.activity_my_contacts.*

class MyContactsActivity : AppCompatActivity() {

    private var areContactEmpty = true

    private var PICK_CONTACTS_REQUEST=2801

    private lateinit var contactAdapter:MyContactsAdapter

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


        val contacts = repository.getAllContacts()
        areContactEmpty=contacts.isEmpty()
        toggleViews()
        contactAdapter = MyContactsAdapter(contacts,this)
        contactAdapter.setOnContactClickListener { name, numbers, colors ->
            val dialog = ContactInfoDialog()
            dialog.setParams(name, numbers, colors)
            dialog.setOnContactDeleteListener {
                for (contact in contacts) {
                    if (name == contact.name) {
                        repository.deleteContact(contact)
                        val list = repository.getAllContacts()
                        contactAdapter.updateList(list)
                    }
                }
                dialog.dismiss()
            }
            dialog.show(supportFragmentManager, "ContactInfoDialog")
        }

        rvMyContacts.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvMyContacts.adapter = contactAdapter

        ivAddContact.setOnClickListener {
            startActivityForResult(Intent(this@MyContactsActivity, PickContactsActivity::class.java),PICK_CONTACTS_REQUEST)
        }

        ivBack.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        startService(Intent(this, ContactSyncIntentService::class.java))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==PICK_CONTACTS_REQUEST){
            if (resultCode== Activity.RESULT_OK){
                val list = repository.getAllContacts()
                areContactEmpty=list.isEmpty()
                toggleViews()
                contactAdapter.updateList(list)
            }
        }
    }
}
