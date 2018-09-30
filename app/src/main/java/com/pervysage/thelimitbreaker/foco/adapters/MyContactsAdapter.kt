package com.pervysage.thelimitbreaker.foco.adapters

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.pervysage.thelimitbreaker.foco.R
import com.pervysage.thelimitbreaker.foco.actvities.MyContactsActivity
import com.pervysage.thelimitbreaker.foco.database.entities.ContactInfo
import java.util.*

class MyContactsAdapter(private var list: List<ContactInfo>, private val context: Context) : RecyclerView.Adapter<MyContactsAdapter.ViewHolder>() {

    data class OrderedContactInfo(
            val name: String,
            val numbers: ArrayList<String>
    )

    override fun onBindViewHolder(p0:ViewHolder, p1: Int) {
        p0.bind(orderedList[p1], p1)
    }

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

    class ViewHolder(itemView: View,
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