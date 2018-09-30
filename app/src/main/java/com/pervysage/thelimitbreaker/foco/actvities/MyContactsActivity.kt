package com.pervysage.thelimitbreaker.foco.actvities

import android.app.Activity
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.pervysage.thelimitbreaker.foco.R
import com.pervysage.thelimitbreaker.foco.adapters.MyContactsAdapter
import com.pervysage.thelimitbreaker.foco.database.Repository
import com.pervysage.thelimitbreaker.foco.database.entities.ContactInfo
import com.pervysage.thelimitbreaker.foco.dialogs.ContactInfoDialog
import kotlinx.android.synthetic.main.activity_my_contacts.*
import java.util.*
import kotlin.collections.ArrayList

class MyContactsActivity : AppCompatActivity() {

    private val PICK_CONTACTS = 1
    private val PERMISSION_REQUEST = 2
    private var areContactEmpty = true


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


    private lateinit var repo: Repository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_contacts)

        repo = Repository.getInstance(application)
        val contactAdapter = MyContactsAdapter(ArrayList(),this)
        rvMyContacts.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvMyContacts.adapter = contactAdapter

        val contacts = repo.getMyContacts()
        contacts.observe(this, Observer<List<ContactInfo>> {
            areContactEmpty = it!!.isEmpty()
            contactAdapter.updateList(it)
            toggleViews()

        })
        contactAdapter.setOnContactClickListener { name, numbers, colors ->
            val list = contacts.value
            val dialog = ContactInfoDialog()
            dialog.setParams(name, numbers, colors)
            dialog.setOnContactDeleteListener {
                for (contact in list!!) {
                    if (name == contact.name) {
                        repo.deleteContact(contact)
                    }
                }
                dialog.dismiss()
            }
            dialog.show(supportFragmentManager, "ContactInfoDialog")
        }
        ivAddContact.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                            this@MyContactsActivity,
                            android.Manifest.permission.READ_CONTACTS
                    ) == PackageManager.PERMISSION_GRANTED
            ) {
                startActivityForResult(Intent(this@MyContactsActivity, PickContactsActivity::class.java), PICK_CONTACTS)
            } else {
                ActivityCompat.requestPermissions(
                        this,
                        arrayOf(android.Manifest.permission.READ_CONTACTS),
                        PERMISSION_REQUEST
                )
            }
        }

        ivBack.setOnClickListener {
            finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startActivityForResult(Intent(this@MyContactsActivity, PickContactsActivity::class.java), PICK_CONTACTS)
        } else {
            Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_CONTACTS && resultCode == Activity.RESULT_OK) {

        }
    }

}
