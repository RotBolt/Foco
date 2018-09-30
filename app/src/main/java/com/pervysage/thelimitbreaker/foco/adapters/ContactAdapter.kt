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
import com.pervysage.thelimitbreaker.foco.actvities.PickContactsActivity
import java.util.*


class ContactAdapter(private val list: List<PickContactsActivity.ContactInfo>, private val context: Context) : RecyclerView.Adapter<ContactAdapter.ViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        val li = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        return ViewHolder(li.inflate(R.layout.layout_contacts, p0, false), context, onContactCheck)
    }

    private lateinit var onContactCheck:(PickContactsActivity.ContactInfo)->Unit

    fun setOnContactCheckListener(l:(PickContactsActivity.ContactInfo)->Unit){
        onContactCheck=l
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.bind(list[p1])
    }

    class ViewHolder(itemView: View, private val context: Context, private val onContactCheck:(PickContactsActivity.ContactInfo)->Unit) : RecyclerView.ViewHolder(itemView) {
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

        fun bind(info: PickContactsActivity.ContactInfo) {
            tvPersonName.text = info.name
            tvInitial.text= info.name[0].toUpperCase().toString()

            val colors = getRandColor()
            tvInitial.setTextColor(colors[0])
            tvInitial.background.colorFilter= PorterDuffColorFilter(colors[1], PorterDuff.Mode.SRC_ATOP)

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