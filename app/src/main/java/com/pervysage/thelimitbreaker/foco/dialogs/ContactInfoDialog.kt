package com.pervysage.thelimitbreaker.foco.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import com.pervysage.thelimitbreaker.foco.R

class ContactInfoDialog:DialogFragment(){
    private lateinit var personName:String
    private lateinit var numbers:List<String>
    private lateinit var colors: Array<Int>



    fun setParams(personName:String,numbers:List<String>,colors:Array<Int>){
        this.personName=personName
        this.numbers=numbers
        this.colors=colors
    }

    private lateinit var onDelete:(name:String)->Unit

    fun setOnContactDeleteListener(l:(String)->Unit){
        onDelete=l
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val li = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val itemView = li.inflate(R.layout.layout_contact_info_dialog,null,false)

        bindData(itemView)

        val builder=AlertDialog.Builder(context)
                .setView(itemView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        return dialog
    }
    private fun bindData(itemView: View){
        val ivDelete = itemView.findViewById<ImageView>(R.id.ivDelete)
        ivDelete.setOnClickListener {
            onDelete(personName)
        }
        val tvPersonName = itemView.findViewById<TextView>(R.id.tvPersonName)
        tvPersonName.text=personName

        val tvInitial = itemView.findViewById<TextView>(R.id.tvInitial)
        tvInitial.text=personName[0].toUpperCase().toString()

        tvInitial.setTextColor(colors[0])
        tvInitial.background.colorFilter=PorterDuffColorFilter(colors[1],PorterDuff.Mode.SRC_ATOP)

        val lvNumbers = itemView.findViewById<ListView>(R.id.lvNumbers)
        lvNumbers.adapter=NumberAdapter(numbers,context!!)
    }

    private class NumberAdapter(private val list:List<String>,private val context: Context):BaseAdapter(){
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var itemView=convertView
            if(itemView==null){
                val li = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                itemView=li.inflate(R.layout.layout_number,parent,false)
                val tvNumber = itemView!!.findViewById<TextView>(R.id.tvNumber)
                tvNumber.text=list[position]
            }
            return itemView
        }

        override fun getItem(position: Int)=list[position]

        override fun getItemId(position: Int)=position.toLong()

        override fun getCount()=list.size

    }
}