package com.pervysage.thelimitbreaker.foco.actvities

import android.app.Activity
import android.arch.lifecycle.Observer
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.pervysage.thelimitbreaker.foco.R
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

    private lateinit var repo: Repository

    private class ContactAdapter(private val list: List<ContactInfo>, private val context: Context) : RecyclerView.Adapter<ContactAdapter.ViewHolder>() {
        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
            val li = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            return ViewHolder(li.inflate(R.layout.layout_contacts, p0, false), context, onContactCheck)
        }

        private lateinit var onContactCheck:(ContactInfo)->Unit

        fun setOnContactCheckListener(l:(ContactInfo)->Unit){
            onContactCheck=l
        }

        override fun getItemCount() = list.size

        override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
            p0.bind(list[p1])
        }

        private class ViewHolder(itemView: View,private val context: Context,private val onContactCheck:(ContactInfo)->Unit) : RecyclerView.ViewHolder(itemView) {
            private val tvPersonName = itemView.findViewById<TextView>(R.id.tvPersonName)
            private val ivChecked = itemView.findViewById<ImageView>(R.id.ivChecked)
            private val tvInitial = itemView.findViewById<TextView>(R.id.tvInitial)

            private fun getRandColor():Array<Int>{
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
                return arrayOf(color,colorTrans)
            }

            fun bind(info: ContactInfo) {
                tvPersonName.text = info.name
                tvInitial.text= info.name[0].toUpperCase().toString()

                val colors = getRandColor()
                tvInitial.setTextColor(colors[0])
                tvInitial.background.colorFilter=PorterDuffColorFilter(colors[1],PorterDuff.Mode.SRC_ATOP)

                if (info.isChecked) ivChecked.visibility = View.VISIBLE else ivChecked.visibility = View.INVISIBLE

                itemView.setOnClickListener {
                    if (info.isChecked) {
                        ivChecked.visibility = View.INVISIBLE
                        onContactCheck(info)


                    } else {
                        ivChecked.visibility = View.VISIBLE
                        onContactCheck(info)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_conatcts)

        repo = Repository.getInstance(application)

        val contactAdapter = ContactAdapter(getAllContacts(), this)
        rvContacts.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvContacts.adapter = contactAdapter
        ivClose.setOnClickListener {
            finish()
        }

        contactAdapter.setOnContactCheckListener {
            if (it.isChecked){
                it.isChecked=false
                count--
                tvCount.text="$count"
                marked.remove(it)
            }else{
                it.isChecked=true
                count++
                tvCount.text="$count"
                marked.add(it)
            }
        }
        ivDone.setOnClickListener {

            val projection = arrayOf(
                    ContactsContract.CommonDataKinds.Phone.NUMBER
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
                        val number = getString(getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        val info = ContactInfo(obj.name, number)
                        repo.insertContact(info)
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
                else if(oldMap.isEmpty()){
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
        val list = repo.getMyContacts()
        list.observe(this, object : Observer<List<com.pervysage.thelimitbreaker.foco.database.entities.ContactInfo>> {
            override fun onChanged(t: List<com.pervysage.thelimitbreaker.foco.database.entities.ContactInfo>?) {
                list.removeObserver(this)
            }
        })
        val l = list.value
        val orderedMap = HashMap<String, ArrayList<String>>()
        l?.run {
            for (info in l) {
                var numbers = orderedMap[info.name]
                if (numbers == null) numbers = ArrayList()
                numbers.add(info.number)
                orderedMap[info.name] = numbers
            }
        }
        return orderedMap
    }




}

