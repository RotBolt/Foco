package com.pervysage.thelimitbreaker.foco.adapters

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.media.AudioManager
import android.support.v4.app.FragmentActivity
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.SwitchCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.pervysage.thelimitbreaker.foco.R
import com.pervysage.thelimitbreaker.foco.database.Repository
import com.pervysage.thelimitbreaker.foco.database.entities.PlacePrefs
import com.pervysage.thelimitbreaker.foco.dialogs.ContactGroupPickDialog
import com.pervysage.thelimitbreaker.foco.dialogs.EditPlaceNameDialog
import com.pervysage.thelimitbreaker.foco.dialogs.RadiusPickDialog
import com.pervysage.thelimitbreaker.foco.expandCollapseController.ExpandableObj
import com.pervysage.thelimitbreaker.foco.expandCollapseController.MyListView
import com.pervysage.thelimitbreaker.foco.expandCollapseController.ViewController
import com.pervysage.thelimitbreaker.foco.utils.GeoWorkerUtil
import com.pervysage.thelimitbreaker.foco.utils.sendNotification
import java.lang.ref.WeakReference

class PlaceAdapter(context: Context,
                   private var places: List<PlacePrefs>,
                   private val listView: MyListView,
                   private val repository: Repository) : BaseAdapter() {

    private val TAG = "PlaceAdapter"
    private var isNew = false

    private val geoWorker = GeoWorkerUtil(context)


    private val viewController = MyViewController(listView, context)

    fun updateList(newList: List<PlacePrefs>, isNew: Boolean) {

        places = newList
        if (places.isNotEmpty() && isNew) {
            this.isNew = isNew
            places[places.size - 1].isExpanded = true
        }
        notifyDataSetChanged()
        if (places.isNotEmpty() && isNew) {
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
                        HeadHolder(itemView!!, context, WeakReference(repository))
                )
                itemView!!.setTag(
                        R.id.BODY_KEY,
                        BodyHolder(itemView!!, context, WeakReference(repository))
                )

            }
            viewController.setUp(itemView!!, thisPref, position)
            return itemView
        }

        return null
    }

    override fun getItem(position: Int): PlacePrefs = places[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int = places.size

    inner class MyViewController(private val listView: MyListView, private val context: Context) : ViewController(listView) {


        override fun bindExtraDataDuringAnimation(itemView: View, modelObj: ExpandableObj, position: Int) {
            val headHolder = itemView.getTag(R.id.HEAD_KEY) as HeadHolder
            val bodyHolder = itemView.getTag(R.id.BODY_KEY) as BodyHolder
            modelObj as PlacePrefs
            headHolder.bindData(modelObj, position)
            bodyHolder.bindData(modelObj)
        }

        private var lastExpandPos = -1
        private var lastExpandName: String? = null


        override fun workInExpand(itemView: View, modelObj: ExpandableObj, position: Int) {
            val headHolder = itemView.getTag(R.id.HEAD_KEY) as HeadHolder
            val bodyHolder = itemView.getTag(R.id.BODY_KEY) as BodyHolder
            with(headHolder) {
                actualCard.background = ContextCompat.getDrawable(context, R.drawable.reg_background)
                actualCard.elevation = 20.0f
                tvPlaceTitle.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                tvDetails.visibility = View.GONE
                ivExpand.visibility = View.GONE
                viewDivider.visibility = View.GONE
            }
            bodyHolder.extraContent.visibility = View.VISIBLE

            modelObj as PlacePrefs

            headHolder.bindData(modelObj, position)
            bodyHolder.bindData(modelObj)


            headHolder.setOnClickListeners(modelObj)
            if (isNew) {
                headHolder.tvPlaceTitle.callOnClick()
                isNew = false
            }

            bodyHolder.setOnClickListeners(modelObj)


        }

        override fun workInCollapse(itemView: View, modelObj: ExpandableObj, position: Int, isExplicit: Boolean) {
            val headHolder = itemView.getTag(R.id.HEAD_KEY) as HeadHolder
            val bodyHolder = itemView.getTag(R.id.BODY_KEY) as BodyHolder
            modelObj as PlacePrefs
            with(headHolder) {

                prefView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                actualCard.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                tvDetails.visibility = View.VISIBLE
                ivExpand.visibility = View.VISIBLE
                viewDivider.visibility = View.VISIBLE
                if (modelObj.active == 1) {
                    tvPlaceTitle.setTextColor(ContextCompat.getColor(context, R.color.colorTextDark))
                    tvDetails.setTextColor(ContextCompat.getColor(context, R.color.colorTextDark))
                    viewDivider.getChildAt(0).background =
                            ContextCompat.getDrawable(context, R.drawable.reg_background)

                    ivExpand.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_expand_more))

                } else {
                    tvPlaceTitle.setTextColor(ContextCompat.getColor(context, R.color.colorTextDisable))
                    tvDetails.setTextColor(ContextCompat.getColor(context, R.color.colorTextDisable))
                    val disableColorFilter = PorterDuffColorFilter(
                            ContextCompat.getColor(context, R.color.colorTextDisable),
                            PorterDuff.Mode.SRC_ATOP
                    )
                    val disabledDividerDrawable = ContextCompat.getDrawable(context, R.drawable.reg_background)?.mutate()
                    disabledDividerDrawable?.colorFilter = disableColorFilter
                    viewDivider.getChildAt(0).background = disabledDividerDrawable

                    val disabledExpandIcon = ContextCompat.getDrawable(context, R.drawable.ic_expand_more)?.mutate()
                    disabledExpandIcon?.colorFilter = disableColorFilter
                    ivExpand.setImageDrawable(disabledExpandIcon)

                }

            }
            bodyHolder.extraContent.visibility = View.GONE

            headHolder.bindData(modelObj, position)
            headHolder.revertClickListeners()

            if (isExplicit && modelObj.active == 1) geoWorker.updatePlacePrefsForMonitoring(modelObj)

        }


        override fun setUp(itemView: View, modelObj: ExpandableObj, pos: Int) {
            super.setUp(itemView, modelObj, pos)
            modelObj as PlacePrefs
            val headHolder = itemView.getTag(R.id.HEAD_KEY) as HeadHolder
            val bodyHolder = itemView.getTag(R.id.BODY_KEY) as BodyHolder

            Log.d(TAG, "lat : ${modelObj.latitude}, lng : ${modelObj.longitude}")
            bodyHolder.setWorkOnDelete {
                geoWorker.removePlaceFromMonitoring(it)
                repository.deletePref(it)
                lastExpandPos = -1
                lastExpandName = null
            }

            headHolder.setOnSwitchChange { isOn, placePrefs ->
                if (isOn) geoWorker.addPlaceForMonitoring(placePrefs)
                else {
                    geoWorker.removePlaceFromMonitoring(placePrefs)
                    val sharedPrefs = context.getSharedPreferences(context.getString(R.string.SHARED_PREF_KEY), Context.MODE_PRIVATE)
                    val serviceStatus = sharedPrefs.getBoolean(context.getString(R.string.SERVICE_STATUS), false)
                    if (serviceStatus) {
                        val notificationManagerCompat = NotificationManagerCompat.from(context)
                        notificationManagerCompat.cancel(0)
                        val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                        am.ringerMode=AudioManager.RINGER_MODE_NORMAL
                        am.setStreamVolume(AudioManager.STREAM_NOTIFICATION, am.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION), AudioManager.FLAG_PLAY_SOUND)
                        sendNotification("Service Stopped for ${placePrefs.name}", -1, context)
                        with(sharedPrefs.edit()){
                            putBoolean(context.getString(R.string.SERVICE_STATUS),false)
                            putString(context.getString(R.string.ACTIVE_CONTACT_GROUP),"")
                        }.apply()
                    }

                }
            }

            headHolder.prefView.setOnClickListener {
                if (modelObj.isExpanded) {
                    setOnCollapseListener {
                        repository.updatePref(modelObj)
                    }
                    collapse(headHolder.prefView, modelObj, pos)

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
                    expand(headHolder.prefView, modelObj, prevExpandView, collapseModelObj, pos)
                    lastExpandPos = pos
                    lastExpandName = headHolder.tvPlaceTitle.text.toString()

                }
            }

        }
    }


    private class HeadHolder(itemView: View, private val context: Context, private val repository: WeakReference<Repository>) {
        val prefView = itemView
        val actualCard = itemView.findViewById<RelativeLayout>(R.id.actualCard)
        val ivWorkHeader = itemView.findViewById<ImageView>(R.id.ivWorkHeader)
        val tvPlaceTitle = itemView.findViewById<TextView>(R.id.tvPlaceTitle)
        val tvDetails = itemView.findViewById<TextView>(R.id.tvDetails)
        val viewDivider = itemView.findViewById<FrameLayout>(R.id.divideContainer)
        val ivExpand = itemView.findViewById<ImageView>(R.id.ivExpand)
        val serviceSwitch = itemView.findViewById<SwitchCompat>(R.id.serviceSwitch)

        init {
            ivWorkHeader.setOnTouchListener { _, _ -> true }
        }

        private lateinit var onSwitchChange: (isOn: Boolean, placePrefs: PlacePrefs) -> Unit

        fun setOnSwitchChange(l: (Boolean, PlacePrefs) -> Unit) {
            onSwitchChange = l
        }

        fun bindData(placePref: PlacePrefs, position: Int) {
            if (position == 0) {
                ivWorkHeader.visibility = View.VISIBLE
            } else {
                ivWorkHeader.visibility = View.GONE
            }
            tvPlaceTitle.text = placePref.name
            val radius = when (placePref.radius) {
                1000 -> "1 km"
                2000 -> "2 km"
                5000 -> "5 km"
                else -> "500 m"
            }
            val group = when (placePref.contactGroup) {
                "All Contacts" -> "All Contacts"
                "None" -> "Total Silence"
                else -> "Priority Contacts"
            }
            tvDetails.text = "$group, $radius"
            serviceSwitch.setOnCheckedChangeListener(null)
            serviceSwitch.isChecked = placePref.active == 1
            serviceSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) placePref.active = 1 else placePref.active = 0
                onSwitchChange(isChecked, placePref)
                repository.get()?.updatePref(placePref)
            }
        }

        fun setOnClickListeners(placePref: PlacePrefs) {

            tvPlaceTitle.setOnClickListener {
                val dialog = EditPlaceNameDialog()
                dialog.hint = placePref.name
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
            tvPlaceTitle.setOnClickListener {
                prefView.callOnClick()
            }
        }

    }

    private class BodyHolder(itemView: View, private val context: Context, private val repository: WeakReference<Repository>) {

        val tvAddress = itemView.findViewById<TextView>(R.id.tvAddress)
        val tvRadius = itemView.findViewById<TextView>(R.id.tvRadius)
        val tvContactGroup = itemView.findViewById<TextView>(R.id.tvContactGroup)
        val tvDelete = itemView.findViewById<TextView>(R.id.tvDelete)
        val extraContent = itemView.findViewById<RelativeLayout>(R.id.extraContent)

        private lateinit var workOnDelete: (PlacePrefs) -> Unit
        fun setWorkOnDelete(l: (PlacePrefs) -> Unit) {
            workOnDelete = l
        }

        fun bindData(placePref: PlacePrefs) = with(placePref) {
            tvAddress.text = address
            tvContactGroup.text = contactGroup
            tvRadius.text = when (placePref.radius) {
                1000 -> "1 km"
                2000 -> "2 km"
                5000 -> "5 km"
                else -> "500 m"
            }

        }

        fun setOnClickListeners(placePref: PlacePrefs) {
            tvRadius.setOnClickListener {
                val dialog = RadiusPickDialog()
                dialog.setOnRadiusPickListener(
                        { radiusString, radiusInt ->
                            tvRadius.text = radiusString
                            placePref.radius = radiusInt
                        },
                        when (placePref.radius) {
                            500 -> 0
                            1000 -> 1
                            2000 -> 2
                            5000 -> 3
                            else -> -1
                        }
                )
                dialog.show((context as FragmentActivity).supportFragmentManager, "RadiusPickDialog")
            }

            tvContactGroup.setOnClickListener {
                val dialog = ContactGroupPickDialog()
                dialog.setOnContactGroupPickListener(
                        { group ->
                            tvContactGroup.text = group
                            placePref.contactGroup = group

                            when (group) {
                                "All Contacts" -> {
                                    // set flag for all contacts
                                }
                                "Priority Contacts" -> {
                                    // Pick priority contacts
                                    // start contact picker activity
                                }
                                "None" -> {
                                    // disable all contacts
                                }
                            }
                        },
                        when (placePref.contactGroup) {
                            "All Contacts" -> 0
                            "Priority Contacts" -> 1
                            "None" -> 2
                            else -> -1
                        }
                )
                dialog.show((context as FragmentActivity).supportFragmentManager, "ContactGroupPickDialog")

            }

            tvDelete.setOnClickListener {
                workOnDelete(placePref)
            }
        }

    }

}