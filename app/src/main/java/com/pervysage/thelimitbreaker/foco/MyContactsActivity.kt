package com.pervysage.thelimitbreaker.foco

import android.app.Activity
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.opengl.Visibility
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.pervysage.thelimitbreaker.foco.database.Repository
import com.pervysage.thelimitbreaker.foco.database.entities.ContactInfo
import kotlinx.android.synthetic.main.activity_my_contacts.*

class MyContactsActivity : AppCompatActivity() {

    private val PICK_CONTACTS=1
    private val PERMISSION_REQUEST=2
    private var areContactEmpty=true

    data class OrderedContactInfo(
            val name: String,
            val numbers:ArrayList<String>
    )


    private fun toogleViews(){
        if(areContactEmpty){
            ivContactHeader.visibility=View.VISIBLE
            tvNoContacts.visibility=View.VISIBLE
            rvMyContacts.visibility=View.GONE
        }else{
            ivContactHeader.visibility=View.GONE
            tvNoContacts.visibility=View.GONE
            rvMyContacts.visibility=View.VISIBLE
        }
    }


    private lateinit var repo:Repository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_contacts)

        repo= Repository.getInstance(application)
        val contactAdapter=ContactAdapter(ArrayList())
        rvMyContacts.layoutManager=LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        rvMyContacts.adapter=contactAdapter

        val contacts = repo.getMyContacts()
        contacts.observe(this, Observer<List<ContactInfo>>{
            areContactEmpty= it!!.isEmpty()
            val orderedList = getOrderedList(it)
            contactAdapter.updateList(orderedList)
            toogleViews()

        })
        ivAddContact.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                            this@MyContactsActivity,
                            android.Manifest.permission.READ_CONTACTS
                            )==PackageManager.PERMISSION_GRANTED
            ){
                startActivityForResult(Intent(this@MyContactsActivity,PickContactsActivity::class.java),PICK_CONTACTS)
            }else{
                ActivityCompat.requestPermissions(
                        this,
                        arrayOf(android.Manifest.permission.READ_CONTACTS),
                        PERMISSION_REQUEST
                )
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==PERMISSION_REQUEST && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            startActivityForResult(Intent(this@MyContactsActivity,PickContactsActivity::class.java),PICK_CONTACTS)
        }else{
            Toast.makeText(this,"You denied the permission",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==PICK_CONTACTS && resultCode== Activity.RESULT_OK){

        }
    }

    private fun  getOrderedList(list:List<ContactInfo>):List<OrderedContactInfo>{
        Log.d("PUI","getOrderedList")
        val listOrdered = ArrayList<OrderedContactInfo>()
        var i = 0
        while (i<list.size){
            Log.d("PUI","getOrderList index $i")
            val name = list[i].name
            val numbers= ArrayList<String>()
            numbers.add(list[i].number)
            if (i+1<=list.size-1 && list[i+1].name==name){
                i++
                var infoObj = list[i]
                while (infoObj.name==name){
                    Log.d("PUI","getOrderList inner index $i")

                    numbers.add(infoObj.number)
                    i++
                    if (i==list.size) break
                    infoObj=list[i]
                }
            }else{
                i++
            }
            listOrdered.add(OrderedContactInfo(name, numbers))
        }
        return  listOrdered
    }

    inner class ContactAdapter(private var list: List<OrderedContactInfo>):RecyclerView.Adapter<ContactAdapter.ViewHolder>(){

        fun updateList(l :List<OrderedContactInfo>){
            Log.d("PUI","Update List")
            list=l
            notifyDataSetChanged()
        }
        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
            val li = this@MyContactsActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            return ViewHolder(li.inflate(R.layout.layout_contacts,p0,false))
        }

        override fun getItemCount()= list.size

        override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
            p0.bind(list[p1],p1)
        }

        inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
            val tvPersonName = itemView.findViewById<TextView>(R.id.tvPersonName)
            val ivContactHeader = itemView.findViewById<ImageView>(R.id.ivContactHeader)

            fun bind(orderedContactInfo: OrderedContactInfo,position:Int){
                tvPersonName.text=orderedContactInfo.name
                if(position==0){
                    ivContactHeader.visibility=View.VISIBLE
                }else{
                    ivContactHeader.visibility=View.GONE
                }
            }
        }
    }
}
