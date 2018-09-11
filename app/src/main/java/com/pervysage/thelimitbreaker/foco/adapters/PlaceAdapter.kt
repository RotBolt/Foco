package com.pervysage.thelimitbreaker.foco.adapters

import android.content.Context
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.support.v7.widget.SwitchCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.pervysage.thelimitbreaker.foco.EditPlaceNameDialog
import com.pervysage.thelimitbreaker.foco.ExpandCollapseController
import com.pervysage.thelimitbreaker.foco.expandCollapseController.MyListView
import com.pervysage.thelimitbreaker.foco.R
import com.pervysage.thelimitbreaker.foco.expandCollapseController.ViewController
import com.pervysage.thelimitbreaker.foco.database.PlacePrefs
import com.pervysage.thelimitbreaker.foco.database.Repository

class PlaceAdapter(private val context: Context,
                   private var places: List<PlacePrefs>,
                   private val listView: MyListView,
                   private val repository: Repository) : BaseAdapter() {

    private var lastExpandedName = ""
    private var lastExpandedPos = -1
    private val TAG = "PlaceAdapter"

    private var isNew =false
    private val expandCollapseController = ExpandCollapseController(listView, context, repository)

    private val viewController = MyViewController(listView, context)


    fun updateList(newList: List<PlacePrefs>, isNew: Boolean) {

        places = newList

        notifyDataSetChanged()
        if (places.isNotEmpty() && isNew) {
            this.isNew=isNew
            places[places.size - 1].isExpanded = true
            listView.setSelection(places.size - 1)
        }
    }



    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var itemView = convertView
        val thisPref = places[position]
        parent?.run {
            if (itemView == null) {
                val li = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                itemView = li.inflate(R.layout.layout_place_prefs, parent, false)
                itemView!!.setTag(
                        R.id.HEAD_KEY,
                        HeadHolder(itemView!!)
                )
                itemView!!.setTag(
                        R.id.BODY_KEY,
                        BodyHolder(itemView!!)
                )
            }
            val headHolder = itemView!!.getTag(R.id.HEAD_KEY) as HeadHolder
            val bodyHolder = itemView!!.getTag(R.id.BODY_KEY) as BodyHolder
            viewController.setUp(headHolder, bodyHolder, thisPref, position)

            return itemView
        }
        return null
    }

    override fun getItem(position: Int): PlacePrefs = places[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = places.size

//    inner class CardHeadHolder(itemView: View) {
//
//
//        private val tvPlaceTitle = itemView.findViewById<TextView>(R.id.tvPlaceTitle)
//        private val tvTimeHead = itemView.findViewById<TextView>(R.id.tvRadiusHead)
//        private val tvAddress = itemView.findViewById<TextView>(R.id.tvAddress)
//        private val tvRadius = itemView.findViewById<TextView>(R.id.tvRadiusChild)
//        private val tvContactGroup = itemView.findViewById<TextView>(R.id.tvContactGroup)
//        private val tvDelete = itemView.findViewById<TextView>(R.id.tvDelete)
//        private val turnOnOff = itemView.findViewById<SwitchCompat>(R.id.serviceSwitch)
//        private val extraContent = itemView.findViewById<RelativeLayout>(R.id.extraContent)
//        private val ivExpandMore = itemView.findViewById<ImageView>(R.id.ivExpand)
//        private val listDivider = itemView.findViewById<FrameLayout>(R.id.divideContainer)
//
//
//        fun bind(itemView: View, placePref: PlacePrefs, pos: Int) {
//            Log.d(TAG, "bind ${placePref.name}")
//            tvPlaceTitle.text = placePref.name
//            tvTimeHead.text = "${placePref.hour} hr ${placePref.minutes} min"
//            turnOnOff.isChecked = placePref.active == 1
//
//            val bindExtraData = {
//                tvAddress.text = placePref.address
//                tvContactGroup.text = placePref.contactGroup
//
//                tvRadius.text = "${placePref.radius} m"
//            }
//
//            if (placePref.isExpanded) {
//                itemView.background = ContextCompat.getDrawable(context, R.drawable.reg_background)
//                itemView.elevation = 20.0f
//                tvPlaceTitle.setTextColor(ContextCompat.getColor(context, android.R.color.white))
//                tvTimeHead.visibility = View.GONE
//                ivExpandMore.visibility = View.GONE
//                listDivider.visibility = View.GONE
//                extraContent.visibility = View.VISIBLE
//
//                Log.d(TAG, "in expanded")
//                bindExtraData()
//            } else {
//                itemView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
//                tvPlaceTitle.setTextColor(ContextCompat.getColor(context, R.color.colorTextDark))
//                extraContent.visibility = View.GONE
//                tvTimeHead.visibility = View.VISIBLE
//                ivExpandMore.visibility = View.VISIBLE
//                listDivider.visibility = View.VISIBLE
//            }
//
//            itemView.setOnClickListener(null)
//
//            itemView.setOnClickListener {
//                if (placePref.isExpanded) {
//                    expandCollapseController.collapseView(itemView, placePref)
//                    lastExpandedPos = -1
//                    lastExpandedName = ""
//                    placePref.isExpanded = false
//                } else {
//
//                    var prevView: View? = null
//                    if (lastExpandedName != "") {
//                        places[lastExpandedPos].isExpanded = false
//                        for (i in 0 until listView.childCount) {
//                            val v = listView.getChildAt(i)
//                            if (v.tvPlaceTitle.text.toString() == lastExpandedName) {
//                                prevView = v
//                                break
//                            }
//                        }
//
//                    }
//                    lastExpandedPos = pos
//                    lastExpandedName = itemView.tvPlaceTitle.text.toString()
//                    expandCollapseController.expandView(itemView, bindExtraData, prevView)
//                    placePref.isExpanded = true
//                }
//            }
//        }
//
//
//    }


    inner class MyViewController(private val listView: MyListView, private val context: Context) : ViewController<HeadHolder, BodyHolder, PlacePrefs>(listView) {

        private var lastExpandPos = -1
        private var lastExpandName: String? = null

        override fun bindExtraData(headHolder: HeadHolder, bodyHolder: BodyHolder, modelObj: PlacePrefs) {
            headHolder.bindData(modelObj)
            bodyHolder.bindData(modelObj)
        }

        override fun workInExpand(headHolder: HeadHolder, bodyHolder: BodyHolder, modelObj: PlacePrefs) {
            with(headHolder) {
                prefView.background = ContextCompat.getDrawable(context, R.drawable.reg_background)
                prefView.elevation = 20.0f
                tvPlaceTitle.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                tvRadiusHead.visibility = View.GONE
                ivExpand.visibility = View.GONE
                viewDivider.visibility = View.GONE
            }
            bodyHolder.extraContent.visibility = View.VISIBLE

            headHolder.bindData(modelObj)
            bodyHolder.bindData(modelObj)


            headHolder.setOnClickListeners(modelObj)
            if (isNew){
                headHolder.tvPlaceTitle.callOnClick()
                isNew=false
            }



        }

        override fun workInCollapse(headHolder: HeadHolder, bodyHolder: BodyHolder, modelObj: PlacePrefs) {
            with(headHolder) {
                prefView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))

                tvPlaceTitle.setTextColor(ContextCompat.getColor(context, R.color.colorTextDark))
                tvRadiusHead.visibility = View.VISIBLE
                ivExpand.visibility = View.VISIBLE
                viewDivider.visibility = View.VISIBLE
            }
            bodyHolder.extraContent.visibility = View.GONE

            headHolder.bindData(modelObj)
            headHolder.revertClickListeners()

        }


        override fun setUp(headHolder: HeadHolder, bodyHolder: BodyHolder, modelObj: PlacePrefs, pos: Int) {
            super.setUp(headHolder, bodyHolder, modelObj, pos)
            bodyHolder.setWorkOnDelete {
                lastExpandPos = -1
                lastExpandName = null
            }

            headHolder.prefView.setOnClickListener {
                if (modelObj.isExpanded) {
                    setOnCollapseListener {
                        repository.update(modelObj)
                    }
                    collapse(headHolder.prefView, headHolder, bodyHolder, modelObj)

                    lastExpandPos = -1
                    lastExpandName = null

                } else {
                    var prevExpandView: View? = null
                    var collapseModelObj = if (lastExpandPos != -1) places[lastExpandPos] else null

                    if (lastExpandName != null) {
                        for (i in 0 until listView.childCount) {
                            val v = listView.getChildAt(i)
                            val thisHead = v.getTag(R.id.HEAD_KEY) as HeadHolder
                            if (thisHead.tvPlaceTitle.text.toString() == lastExpandName) {
                                prevExpandView = v
                                break
                            }
                        }
                    }
                    expand(headHolder.prefView, headHolder, bodyHolder, modelObj, prevExpandView, collapseModelObj)
                    lastExpandPos = pos
                    lastExpandName = headHolder.tvPlaceTitle.text.toString()

                }
            }
        }
    }

    inner class HeadHolder(itemView: View) {
        val prefView = itemView
        val tvPlaceTitle = itemView.findViewById<TextView>(R.id.tvPlaceTitle)
        val tvRadiusHead = itemView.findViewById<TextView>(R.id.tvRadiusHead)
        val viewDivider = itemView.findViewById<FrameLayout>(R.id.divideContainer)
        val ivExpand = itemView.findViewById<ImageView>(R.id.ivExpand)
        val serviceSwitch = itemView.findViewById<SwitchCompat>(R.id.serviceSwitch)

        fun bindData(placePref: PlacePrefs) {
            tvPlaceTitle.text = placePref.name
            tvRadiusHead.text = "${placePref.radius} m"
            serviceSwitch.isChecked = placePref.active == 1
        }

        fun setOnClickListeners(placePref: PlacePrefs) {


            tvPlaceTitle.setOnClickListener {
                    val dialog = EditPlaceNameDialog()
                dialog.hint=placePref.name
                    dialog.setOnNameConfirm {
                        tvPlaceTitle.text = it
                        placePref.name = it
                    }
                    dialog.show(
                            (context as FragmentActivity).supportFragmentManager,
                            "EditPlaceName"
                    )
            }

        }

        fun revertClickListeners() {
            tvPlaceTitle.setOnClickListener{
                prefView.callOnClick()
            }

        }
    }

    inner class BodyHolder(itemView: View) {
        val tvAddress = itemView.findViewById<TextView>(R.id.tvAddress)
        val tvRadiusChild = itemView.findViewById<TextView>(R.id.tvRadiusChild)
        val tvContactGroup = itemView.findViewById<TextView>(R.id.tvContactGroup)
        val tvDelete = itemView.findViewById<TextView>(R.id.tvDelete)
        val extraContent = itemView.findViewById<RelativeLayout>(R.id.extraContent)

        private lateinit var workOnDelete: () -> Unit
        fun setWorkOnDelete(l: () -> Unit) {
            workOnDelete = l
        }

        fun bindData(placePref: PlacePrefs) = with(placePref) {
            tvAddress.text = address
            tvContactGroup.text = contactGroup
            tvRadiusChild.text = "$radius m"
            tvDelete.setOnClickListener {
                repository.delete(placePref)
                workOnDelete()
            }
        }

    }

}

