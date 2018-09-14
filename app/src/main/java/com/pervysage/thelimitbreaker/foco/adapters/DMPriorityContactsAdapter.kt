package com.pervysage.thelimitbreaker.foco.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.pervysage.thelimitbreaker.foco.R

class DMPriorityContactsAdapter(private val list:List<String>,private val context: Context):RecyclerView.Adapter<DMPriorityContactsAdapter.ViewHolder>(){
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val li = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        return ViewHolder(li.inflate(R.layout.contact_icon,p0,false))
    }


    override fun getItemCount()=list.size

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.bind(list[p1])
    }

    class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
       private val tvPersonName = itemView.findViewById<TextView>(R.id.tvPersonName)

        fun bind(name:String){
            tvPersonName.text=name
        }
    }
}