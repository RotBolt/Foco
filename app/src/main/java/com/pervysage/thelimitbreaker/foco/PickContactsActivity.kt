package com.pervysage.thelimitbreaker.foco

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.support.annotation.IntegerRes
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_pick_conatcts.*

class PickContactsActivity : AppCompatActivity() {

    private var count=0

    data class ContactInfo(
            val name:String,
            val lookUpKey:String,
            var isChecked:Boolean = false
    )

    private val marked=ArrayList<ContactInfo>()

    inner class ContactAdapter(private val list:List<ContactInfo>):RecyclerView.Adapter<ContactAdapter.ViewHolder>(){
        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
            val li = this@PickContactsActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            return ViewHolder(li.inflate(R.layout.layout_contacts,p0,false))
        }

        override fun getItemCount()=list.size

        override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
            p0.bind(list[p1])
        }

        inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
            private val tvPersonName = itemView.findViewById<TextView>(R.id.tvPersonName)
            private val ivChecked = itemView.findViewById<ImageView>(R.id.ivChecked)

            fun bind(info:ContactInfo){
                tvPersonName.text=info.name

                if(info.isChecked) ivChecked.visibility=View.VISIBLE else ivChecked.visibility=View.INVISIBLE

                itemView.setOnClickListener {
                    if (info.isChecked){
                        info.isChecked=false
                        count--
                        tvCount.text="$count"
                        ivChecked.visibility=View.INVISIBLE
                        marked.remove(info)

                    }else{
                        info.isChecked=true
                        count++
                        tvCount.text="$count"
                        ivChecked.visibility=View.VISIBLE
                        marked.add(info)
                    }
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_conatcts)

        val contactAdapter = ContactAdapter(getAllContacts())
        rvContacts.layoutManager=LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        rvContacts.adapter=contactAdapter
        ivClose.setOnClickListener {
            finish()

        }
        ivDone.setOnClickListener {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    private fun getAllContacts():List<ContactInfo>{
        val contactList = ArrayList<ContactInfo>()
        val contactMap =HashMap<String,String>()
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
            while (cursor.moveToNext()){
                val name = getString(getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                val lookUpKey=getString(getColumnIndexOrThrow(ContactsContract.Contacts.LOOKUP_KEY))
                contactMap[name]=lookUpKey
            }
        }
        contactMap.mapTo(contactList){
            ContactInfo(it.key,it.value)
        }
        contactList.sortWith(Comparator { o1, o2 ->
            o1.name.compareTo(o2.name)
        })
        return contactList
    }
}
