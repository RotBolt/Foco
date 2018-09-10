package com.pervysage.thelimitbreaker.foco.adapters

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.SwitchCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.pervysage.thelimitbreaker.foco.ExpandCollapseController
import com.pervysage.thelimitbreaker.foco.R
import com.pervysage.thelimitbreaker.foco.database.PlacePrefs
import com.pervysage.thelimitbreaker.foco.database.Repository
import kotlinx.android.synthetic.main.layout_place_prefs.view.*

class PlaceAdapter(private val context: Context,
                   private var places: List<PlacePrefs>,
                   private val listView: ListView,
                   private val repository: Repository) : BaseAdapter() {

    private var lastExpandedName = ""
    private var lastExpandedPos=-1
    private val TAG = "PlaceAdapter"

    private val expandCollapseController = ExpandCollapseController(listView, context,repository)

    fun updateList(newList:List<PlacePrefs>,isNew:Boolean){
        places=newList

        notifyDataSetChanged()
        if(places.isNotEmpty()&& isNew) {
            places[places.size - 1].isExpanded = true
            listView.setSelection(places.size-1)
        }
    }

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
            return itemView
        }
        return null
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
        private val listDivider = itemView.findViewById<FrameLayout>(R.id.divideContainer)



        fun bind(itemView: View, placePref: PlacePrefs, pos: Int) {
            Log.d(TAG,"bind ${placePref.name}")
            tvPlaceTitle.text = placePref.name
            tvTimeHead.text = "${placePref.hour} hr ${placePref.minutes} min"
            turnOnOff.isChecked = placePref.active == 1

            val bindExtraData = {
                Log.d(TAG,"bind extra data")
                with(itemView) {
                    tvAddress.text = placePref.address
                    tvContactGroup.text = placePref.contactGroup
                    tvTimeChild.text = "${placePref.hour} hr ${placePref.minutes} min"
                    tvRadius.text = "${placePref.radius} m"

                }
            }

            if (placePref.isExpanded) {
                itemView.background=ContextCompat.getDrawable(context,R.drawable.reg_background)
                itemView.elevation=20.0f
                tvPlaceTitle.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                tvTimeHead.visibility = View.GONE
                ivExpandMore.visibility = View.GONE
                listDivider.visibility=View.GONE
                extraContent.visibility = View.VISIBLE

                Log.d(TAG,"in expanded")
                bindExtraData()
            } else {
                itemView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                tvPlaceTitle.setTextColor(ContextCompat.getColor(context, R.color.colorTextDark))
                extraContent.visibility = View.GONE
                tvTimeHead.visibility = View.VISIBLE
                ivExpandMore.visibility = View.VISIBLE
                listDivider.visibility=View.VISIBLE
            }

            itemView.setOnClickListener(null)

            itemView.setOnClickListener {
                if (placePref.isExpanded) {
                    expandCollapseController.collapseView(itemView,placePref)
                    lastExpandedPos = -1
                    lastExpandedName=""
                    placePref.isExpanded = false
                } else {

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

