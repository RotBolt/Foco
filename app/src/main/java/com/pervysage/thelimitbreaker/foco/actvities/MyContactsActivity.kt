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

    data class OrderedContactInfo(
            val name: String,
            val numbers: ArrayList<String>
    )


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
        val contactAdapter = ContactAdapter(ArrayList(), this)
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


    private class ContactAdapter(private var list: List<ContactInfo>, private val context: Context) : RecyclerView.Adapter<ContactAdapter.ViewHolder>() {

        private var orderedList = getOrderedList(list)

        private lateinit var onContactClickListener:(name:String,numbers:ArrayList<String>,colors:Array<Int>)->Unit

        fun setOnContactClickListener(l:(name:String,numbers:ArrayList<String>,colors:Array<Int>)->Unit){
            onContactClickListener=l
        }

        fun updateList(l: List<ContactInfo>) {
            list = l
            orderedList = getOrderedList(l)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
            val li = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            return ViewHolder(li.inflate(R.layout.layout_contacts, p0, false), onContactClickListener, context)
        }

        override fun getItemCount() = orderedList.size

        override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
            p0.bind(orderedList[p1], p1)
        }

        private fun getOrderedList(list: List<ContactInfo>): List<OrderedContactInfo> {

            val listOrdered = ArrayList<OrderedContactInfo>()
            val orderedMap = HashMap<String, ArrayList<String>>()
            for (info in list) {
                var numbers = orderedMap[info.name]
                if (numbers == null) numbers = ArrayList()
                numbers.add(info.number)
                orderedMap[info.name] = numbers
            }
            orderedMap.mapTo(listOrdered) {
                OrderedContactInfo(it.key, it.value)
            }
            return listOrdered
        }



        private class ViewHolder(itemView: View,
                                 private  val onContactClick:(name:String,numbers:ArrayList<String>,colors:Array<Int>)->Unit,
                                 private val context: Context) : RecyclerView.ViewHolder(itemView) {
            val tvPersonName = itemView.findViewById<TextView>(R.id.tvPersonName)
            val tvInitial = itemView.findViewById<TextView>(R.id.tvInitial)
            val ivContactHeader = itemView.findViewById<ImageView>(R.id.ivContactHeader)


            private fun getRandColor(): Array<Int> {
                val rand = Random()
                val colorPalette = arrayOf(
                        ContextCompat.getColor(context, R.color.red_A700),
                        ContextCompat.getColor(context, R.color.pink_A700),
                        ContextCompat.getColor(context, R.color.purple_800),
                        ContextCompat.getColor(context, R.color.indigo_900),
                        ContextCompat.getColor(context, R.color.deep_orange_A700),
                        ContextCompat.getColor(context, R.color.cyan_800),
                        ContextCompat.getColor(context, R.color.green_800)

                )
                val colorPaletteTrans = arrayOf(
                        ContextCompat.getColor(context, R.color.red_A200),
                        ContextCompat.getColor(context, R.color.pink_A200),
                        ContextCompat.getColor(context, R.color.purple_A200),
                        ContextCompat.getColor(context, R.color.indigo_A200),
                        ContextCompat.getColor(context, R.color.amber_A200),
                        ContextCompat.getColor(context, R.color.cyan_300),
                        ContextCompat.getColor(context, R.color.green_300)
                )
                val colorIndex = rand.nextInt(colorPalette.size)
                val color = colorPalette[colorIndex]
                val colorTrans = colorPaletteTrans[colorIndex]
                return arrayOf(color, colorTrans)
            }

            fun bind(orderedContactInfo: OrderedContactInfo, position: Int) {
                ivContactHeader.setOnTouchListener { _, _ -> true }
                val colors = getRandColor()
                itemView.setOnClickListener {
                   onContactClick(orderedContactInfo.name, orderedContactInfo.numbers, colors)
                }
                tvInitial.text = orderedContactInfo.name[0].toUpperCase().toString()

                tvInitial.setTextColor(colors[0])
                tvInitial.background.colorFilter = PorterDuffColorFilter(colors[1], PorterDuff.Mode.SRC_ATOP)
                tvPersonName.text = orderedContactInfo.name
                if (position == 0) {
                    ivContactHeader.visibility = View.VISIBLE
                } else {
                    ivContactHeader.visibility = View.GONE
                }
            }
        }
    }


}
