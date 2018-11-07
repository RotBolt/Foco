package com.pervysage.thelimitbreaker.foco.adapters

import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.location.LocationManager
import android.media.AudioManager
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.SwitchCompat
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.crashlytics.android.Crashlytics
import com.pervysage.thelimitbreaker.foco.R
import com.pervysage.thelimitbreaker.foco.database.Repository
import com.pervysage.thelimitbreaker.foco.database.entities.PlacePrefs
import com.pervysage.thelimitbreaker.foco.dialogs.ContactGroupPickDialog
import com.pervysage.thelimitbreaker.foco.dialogs.EditPlaceNameDialog
import com.pervysage.thelimitbreaker.foco.dialogs.RadiusPickDialog
import com.pervysage.thelimitbreaker.foco.expandCollapseController.MyListView
import com.pervysage.thelimitbreaker.foco.expandCollapseController.ViewManager
import com.pervysage.thelimitbreaker.foco.utils.GeoWorkerUtil
import com.pervysage.thelimitbreaker.foco.utils.sendGeofenceNotification

class PlacePrefsAdapter(private val context: Context, private var placePrefList: List<PlacePrefs>, private val listView: MyListView) : BaseAdapter() {

    private val viewManager = ViewManager(listView)
    private val geoWorkerUtil = GeoWorkerUtil(context)
    private val repository = Repository.getInstance(context.applicationContext as Application)

    private var lastExpandPos = -1

    private var lastExpandName = ""

    private var toExecuteGeo = true

    private lateinit var onListEmpty: (Boolean) -> Unit

    fun setOnEmptyListListener(l: (Boolean) -> Unit) {
        onListEmpty = l
    }

    override fun getItem(position: Int) = placePrefList[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getCount() = placePrefList.size

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var itemView = convertView
        val thisPref = placePrefList[position]

        parent?.run {
            if (itemView == null) {
                val li = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                itemView = li.inflate(R.layout.layout_place_prefs, parent, false)
                itemView?.run {
                    setTag(
                            R.id.HEAD_KEY,
                            HeadHolder(this)
                    )
                    setTag(
                            R.id.BODY_KEY,
                            BodyHolder(this)
                    )
                }
            }
            manageView(itemView, thisPref, position)
            return itemView
        }
        return null

    }

    fun refreshList(list: List<PlacePrefs>, newAdded: Boolean) {
        placePrefList = list

        notifyDataSetChanged()
        if (placePrefList.isEmpty()) {
            onListEmpty(true)
        }
        if (newAdded) {
            placePrefList[placePrefList.size - 1].isExpanded = true
            listView.setSelection(placePrefList.size - 1)
            geoWorkerUtil.addPlaceForMonitoring(placePrefs = placePrefList[placePrefList.size - 1])
                    .addOnFailureListener {
                        placePrefList[placePrefList.size - 1].active = 0

                        Toast.makeText(context, "Oops !! GPS lost, Please check location settings", Toast.LENGTH_LONG).show()
                        repository.updatePref(placePrefList[placePrefList.size - 1].copy(isExpanded = false, active = 0))
                    }
                    .addOnSuccessListener {
                        Toast.makeText(context, "Service turned on for ${placePrefList[placePrefList.size - 1].name}", Toast.LENGTH_SHORT).show()
                    }
        }

    }

    private fun revertPrefsForActivePlace(placePref: PlacePrefs) {

        val sharedPrefs = context.getSharedPreferences(context.getString(R.string.SHARED_PREF_KEY), Context.MODE_PRIVATE)
        val lat = sharedPrefs.getString(context.getString(R.string.ACTIVE_LAT), "")
        val lng = sharedPrefs.getString(context.getString(R.string.ACTIVE_LNG), "")

        if (lat == placePref.latitude.toString() && lng == placePref.longitude.toString()) {
            val notificationManagerCompat = NotificationManagerCompat.from(context)
            notificationManagerCompat.cancel(0)
            sendGeofenceNotification("Service Stopped for ${placePref.name}", -1, context)

            val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                am.setStreamVolume(AudioManager.STREAM_RING, am.getStreamMaxVolume(AudioManager.STREAM_RING), AudioManager.FLAG_PLAY_SOUND)
            with(sharedPrefs.edit()) {
                putString(context.getString(R.string.ACTIVE_NAME), "")
                putString(context.getString(R.string.GEO_ACTIVE_GROUP), "")
                putBoolean(context.getString(R.string.GEO_STATUS), false)
                putString(context.getString(R.string.ACTIVE_LAT), "")
                putString(context.getString(R.string.ACTIVE_LNG), "")
            }.commit()
        }
    }

    private fun manageView(itemView: View?, placePrefs: PlacePrefs, position: Int) {

        itemView?.run {

            if (placePrefs.isExpanded)
                doWorkInExpand(this, placePrefs, position)
            else
                doWorkInCollapse(this, placePrefs, position)

            setOnClickListener {
                if (placePrefs.isExpanded) {

                    viewManager.collapse(this) {
                        doWorkInCollapse(this, placePrefs, position)
                    }

                } else {
                    var viewToCollapse: View? = null
                    val prevPrefs = if (lastExpandPos != -1) placePrefList[lastExpandPos] else null

                    /*
                     Checking whether previous expanded card is currently on screen or not
                     if present then do the collapse work else just make placePref expand state false
                      */
                    if (lastExpandName != "") {
                        for (i in 0 until listView.childCount) {
                            val v = listView.getChildAt(i)
                            val thisHead = v.getTag(R.id.HEAD_KEY) as HeadHolder
                            if (thisHead.getPlaceTitle() == lastExpandName) {
                                viewToCollapse = v
                                break
                            }
                        }
                    }
                    prevPrefs?.isExpanded = false

                    if (viewToCollapse != null && prevPrefs != null)
                        viewManager.expand(
                                this,
                                { doWorkInExpand(this, placePrefs, position) },
                                { doWorkInCollapse(viewToCollapse, prevPrefs, lastExpandPos) }
                        )
                    else
                        viewManager.expand(
                                this,
                                { doWorkInExpand(this, placePrefs, position) },
                                null
                        )

                    // putting current prefs details to expanded state

                    lastExpandName = placePrefs.name
                    lastExpandPos = position
                }
            }
        }
    }

    private fun doWorkInExpand(itemView: View, placePrefs: PlacePrefs, position: Int) {

        val headHolder = itemView.getTag(R.id.HEAD_KEY) as HeadHolder
        val bodyHolder = itemView.getTag(R.id.BODY_KEY) as BodyHolder

        placePrefs.isExpanded = true

        with(headHolder) {
            actualCard.background = ContextCompat.getDrawable(context, R.drawable.reg_background)
            actualCard.elevation = 20.0f
            tvPlaceTitle.setTextColor(ContextCompat.getColor(context, android.R.color.white))
            tvDetails.visibility = View.GONE
            ivExpand.visibility = View.GONE
            viewDivider.visibility = View.GONE
        }
        bodyHolder.extraContent.visibility = View.VISIBLE




        headHolder.bindData(placePrefs, position)
        bodyHolder.bindData(placePrefs)

        setPrefsListeners(headHolder, placePrefs, true)

        (context as FragmentActivity)

        bodyHolder.setOnContactGroupPickListener(placePrefs, context.supportFragmentManager) {
            placePrefs.contactGroup = it

            val prefsTBU = placePrefs.copy(isExpanded = false)

            val sharedPrefs = context.getSharedPreferences(context.getString(R.string.SHARED_PREF_KEY), Context.MODE_PRIVATE)
            val lat = sharedPrefs.getString(context.getString(R.string.ACTIVE_LAT), "")
            val lng = sharedPrefs.getString(context.getString(R.string.ACTIVE_LNG), "")
            if (lat == placePrefs.latitude.toString() && lng == placePrefs.longitude.toString()) {
                sharedPrefs.edit().putString(context.getString(R.string.GEO_ACTIVE_GROUP), it).commit()
            }
            repository.updatePref(prefsTBU)
        }

        bodyHolder.setOnRadiusPickListeners(placePrefs, context.supportFragmentManager) { radiusString, radiusInt ->

            geoWorkerUtil.updatePlacePrefsForMonitoring(
                    placePrefs,
                    {
                        Toast.makeText(context, "Radius Updated", Toast.LENGTH_SHORT).show()
                        placePrefs.radius = radiusInt
                        bodyHolder.tvRadius.text = radiusString
                        val prefsTBU = placePrefs.copy(isExpanded = false)
                        repository.updatePref(prefsTBU)
                    },
                    {
                        showFailDialog(context, isGPSUnabled())
                    }
            )

        }

        bodyHolder.setWorkOnDelete(placePrefs) {
            repository.deletePref(it)
            lastExpandPos = -1
            lastExpandName = ""
            revertPrefsForActivePlace(placePrefs)
            val list = repository.getAllPlacePrefs()
            refreshList(list, false)
        }


    }

    private fun doWorkInCollapse(itemView: View, placePrefs: PlacePrefs, position: Int) {

        val headHolder = itemView.getTag(R.id.HEAD_KEY) as HeadHolder
        val bodyHolder = itemView.getTag(R.id.BODY_KEY) as BodyHolder

        with(headHolder) {

            prefView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
            actualCard.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
            tvDetails.visibility = View.VISIBLE
            ivExpand.visibility = View.VISIBLE
            viewDivider.visibility = View.VISIBLE
            showEnabled(placePrefs.active == 1, false, context)
        }

        placePrefs.isExpanded = false
        bodyHolder.extraContent.visibility = View.GONE
        headHolder.bindData(placePrefs, position)

        setPrefsListeners(headHolder, placePrefs, false)
    }

    private fun isGPSUnabled(): Boolean {
        return (context.getSystemService(Context.LOCATION_SERVICE) as LocationManager).isProviderEnabled(LocationManager.GPS_PROVIDER)
    }


    private fun setPrefsListeners(headHolder: HeadHolder, placePrefs: PlacePrefs, isExpanding: Boolean) {

        (context as FragmentActivity)

        headHolder.setOnNameChangeListener(
                placePref = placePrefs,
                fm = context.supportFragmentManager,
                onNameChange = {
                    val sharedPrefs = context.getSharedPreferences(context.getString(R.string.SHARED_PREF_KEY), Context.MODE_PRIVATE)
                    val lat = sharedPrefs.getString(context.getString(R.string.ACTIVE_LAT), "")
                    val lng = sharedPrefs.getString(context.getString(R.string.ACTIVE_LNG), "")
                    repository.updatePref(placePrefs.copy(name = placePrefs.name))
                    if (lat == placePrefs.latitude.toString() && lng == placePrefs.longitude.toString()) {
                        sharedPrefs.edit().putString(context.getString(R.string.ACTIVE_NAME), placePrefs.name).commit()
                    }
                }
        )

        headHolder.setOnSwitchChangeListener(
                placePref = placePrefs,
                onSwitchChange = { isOn, placePrefs ->

                    if (isOn && toExecuteGeo) {
                        geoWorkerUtil.addPlaceForMonitoring(placePrefs)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Service turned on for ${placePrefs.name}", Toast.LENGTH_SHORT).show()
                                    placePrefs.active = 1
                                    if (!isExpanding)
                                        headHolder.showEnabled(true, false, context)
                                    repository.updatePref(placePrefs.copy(isExpanded = false))
                                }.addOnFailureListener {
                                    Crashlytics.logException(it)
                                    showFailDialog(context, isGPSUnabled())
                                    toExecuteGeo = false
                                    headHolder.serviceSwitch.isChecked = false
                                }

                    } else if (toExecuteGeo) {
                        geoWorkerUtil.removePlaceFromMonitoring(placePrefs)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Service turned off for ${placePrefs.name}", Toast.LENGTH_SHORT).show()
                                    placePrefs.active = 0
                                    if (!isExpanding)
                                        headHolder.showEnabled(false, false, context)
                                    revertPrefsForActivePlace(placePrefs)
                                    repository.updatePref(placePrefs.copy(active = 0, isExpanded = false))
                                }
                                .addOnFailureListener {
                                    Crashlytics.logException(it)
                                    showFailDialog(context, isGPSUnabled())
                                    toExecuteGeo = false
                                    headHolder.serviceSwitch.isChecked = false
                                }
                    }
                    toExecuteGeo = true
                }
        )
    }

    private fun showFailDialog(context: Context, isGpsEnabled: Boolean) {
        val contextThemeWrapper = ContextThemeWrapper(context, R.style.DialogStyle)
        val builder = AlertDialog.Builder(contextThemeWrapper)
                .setTitle("Oops")
        if (!isGpsEnabled) {
            builder.setMessage("Please Turn on location ")
                    .setPositiveButton("Turn On") { dialog, _ ->
                        dialog.dismiss()
                        context.startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
        } else {
            builder.setMessage("GPS Not working. Please try again later")
                    .setPositiveButton("Ok") { dialog, _ ->
                        dialog.dismiss()
                    }
        }
        val dialog = builder.create()
        dialog?.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        dialog.show()
    }

    private class HeadHolder(itemView: View) {
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

        fun showEnabled(isEnabled: Boolean, isExpanded: Boolean, context: Context) {
            if (!isExpanded) {
                if (isEnabled) {
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
        }

        fun getPlaceTitle(): String = tvPlaceTitle.text.toString()

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
        }

        fun setOnSwitchChangeListener(placePref: PlacePrefs, onSwitchChange: (Boolean, PlacePrefs) -> Unit) {

            serviceSwitch.setOnCheckedChangeListener { _, isChecked ->
                onSwitchChange(isChecked, placePref)
            }
        }

        fun setOnNameChangeListener(placePref: PlacePrefs, fm: FragmentManager, onNameChange: () -> Unit) {

            tvPlaceTitle.setOnClickListener {
                val dialog = EditPlaceNameDialog()
                dialog.iniName = placePref.name
                dialog.setOnNameConfirm { name: String ->
                    tvPlaceTitle.text = name
                    placePref.name = name
                    onNameChange()
                }
                dialog.show(fm, "EditPlaceName")
            }
        }
    }

    private class BodyHolder(itemView: View) {
        val tvAddress = itemView.findViewById<TextView>(R.id.tvAddress)
        val tvRadius = itemView.findViewById<TextView>(R.id.tvRadius)
        val tvContactGroup = itemView.findViewById<TextView>(R.id.tvContactGroup)
        val tvDelete = itemView.findViewById<TextView>(R.id.tvDelete)
        val extraContent = itemView.findViewById<RelativeLayout>(R.id.extraContent)

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

        fun setOnRadiusPickListeners(placePref: PlacePrefs, fm: FragmentManager, onRadiusChange: (String, Int) -> Unit) {
            tvRadius.setOnClickListener {

                val dialog = RadiusPickDialog()
                dialog.setIniCheckedItem(
                        iniCheckedItem = when (placePref.radius) {
                            500 -> 0
                            1000 -> 1
                            2000 -> 2
                            5000 -> 3
                            else -> -1
                        }
                )
                dialog.setOnRadiusPickListener { radiusString, radiusInt ->
                    onRadiusChange(radiusString, radiusInt)
                }
                dialog.show(fm, "RadiusPick")
            }
        }

        fun setOnContactGroupPickListener(placePref: PlacePrefs, fm: FragmentManager, onGroupChange: (String) -> Unit) {
            tvContactGroup.setOnClickListener {
                val dialog = ContactGroupPickDialog()
                dialog.setIniCheckedItem(
                        iniCheckedItem = when (placePref.contactGroup) {
                            "All Contacts" -> 0
                            "Priority Contacts" -> 1
                            "None" -> 2
                            else -> -1
                        }
                )
                dialog.setOnContactGroupPickListener { group ->
                    tvContactGroup.text = group
                    placePref.contactGroup = group
                    onGroupChange(group)
                }
                dialog.show(fm, "GroupPick")
            }
        }

        fun setWorkOnDelete(placePref: PlacePrefs, workOnDelete: (PlacePrefs) -> Unit) {
            tvDelete.setOnClickListener {
                workOnDelete(placePref)
            }
        }
    }
}