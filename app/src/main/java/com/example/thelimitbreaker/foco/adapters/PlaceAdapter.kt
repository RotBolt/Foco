package com.example.thelimitbreaker.foco.adapters

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.SwitchCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.thelimitbreaker.foco.ExpandCollapseController
import com.example.thelimitbreaker.foco.R
import com.example.thelimitbreaker.foco.models.PlacePrefs
import kotlinx.android.synthetic.main.layout_place_prefs.view.*

class PlaceAdapter(private val context: Context,
                   private val places: ArrayList<PlacePrefs>,
                   private val listView: ListView) : BaseAdapter() {

    private var lastExpandedName = ""
    private var lastExpandedPos=-1
    private val TAG = "PlaceAdapter"
    private val expandCollapseController = ExpandCollapseController(listView, context)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        Log.d(TAG, "getView $position")
        var itemView = convertView
        val thisPref = places[position]
        parent?.run {
            if (itemView == null) {
                val li = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                itemView = li.inflate(R.layout.layout_place_prefs, parent, false)
                itemView!!.setTag(
                        R.id.HEAD_KEY,
                        CardHeadHolder(itemView!!)
                )
            }
            val headHolder = itemView!!.getTag(R.id.HEAD_KEY) as CardHeadHolder
            headHolder.bind(itemView!!, thisPref, position)
        }
        return itemView
    }

    override fun getItem(position: Int): PlacePrefs = places[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = places.size

    inner class CardHeadHolder(itemView: View) {

        private val tvPlaceTitle = itemView.findViewById<TextView>(R.id.tvPlaceTitle)
        private val tvTimeHead = itemView.findViewById<TextView>(R.id.tvTimeHead)
        private val turnOnOff = itemView.findViewById<SwitchCompat>(R.id.turnOnOff)
        private val extraContent = itemView.findViewById<RelativeLayout>(R.id.extraContent)
        private val ivExpandMore = itemView.findViewById<ImageView>(R.id.ivExpand)
        fun bind(itemView: View, placePref: PlacePrefs, pos: Int) {
            tvPlaceTitle.text = placePref.name
            tvTimeHead.text = "${placePref.hour} hr ${placePref.minutes} min"
            turnOnOff.isChecked = placePref.isOn == 1

            if (placePref.isExpanded) {
                itemView.background=ContextCompat.getDrawable(context,R.drawable.reg_background)
                tvPlaceTitle.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                extraContent.visibility = View.VISIBLE
                tvTimeHead.visibility = View.GONE
                ivExpandMore.visibility = View.GONE
            } else {
                itemView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                tvPlaceTitle.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
                extraContent.visibility = View.GONE
                tvTimeHead.visibility = View.VISIBLE
                ivExpandMore.visibility = View.VISIBLE
            }

            itemView.setOnClickListener(null)
            itemView.setOnClickListener {
                if (placePref.isExpanded) {
                    expandCollapseController.collapseView(itemView)
                    lastExpandedPos = -1
                    lastExpandedName=""
                    placePref.isExpanded = false
                } else {
                    val bindExtraData = {
                        with(itemView) {
                            tvAddress.text = placePref.address
                            tvContactGroup.text = placePref.contactGroup
                            tvTimeChild.text = "${placePref.hour} hr ${placePref.minutes} min"
                            tvRadius.text = "${placePref.radius} m"

                        }
                    }
                    var prevView: View? = null
                    if (lastExpandedName != "") {
                        places[lastExpandedPos].isExpanded = false
                        for (i in 0 until listView.childCount){
                            val v = listView.getChildAt(i)
                            if(v.tvPlaceTitle.text.toString()==lastExpandedName){
                                prevView=v
                                break
                            }
                        }

                        Log.d(TAG,"prev $prevView")
                    }
                    lastExpandedPos=pos
                    lastExpandedName=itemView.tvPlaceTitle.text.toString()
                    expandCollapseController.expandView(itemView, bindExtraData, prevView)
                    placePref.isExpanded = true
                }
            }
        }
    }
}

