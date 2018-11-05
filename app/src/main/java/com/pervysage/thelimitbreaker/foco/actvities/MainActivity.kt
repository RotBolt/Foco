package com.pervysage.thelimitbreaker.foco.actvities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.os.LocaleList
import android.support.design.widget.TabLayout
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.PopupMenu
import com.crashlytics.android.Crashlytics
import com.google.android.gms.location.places.ui.PlacePicker
import com.pervysage.thelimitbreaker.foco.R
import com.pervysage.thelimitbreaker.foco.adapters.PagerAdapter
import com.pervysage.thelimitbreaker.foco.database.Repository
import com.pervysage.thelimitbreaker.foco.database.entities.PlacePrefs
import com.pervysage.thelimitbreaker.foco.database.entities.generateGeoKey
import com.pervysage.thelimitbreaker.foco.dialogs.EditPlaceNameDialog
import io.fabric.sdk.android.Fabric
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {


    private val PLACE_PICK_REQUEST = 1

    private lateinit var repository: Repository

    private val pickPlace = {
        val intentBuilder = PlacePicker.IntentBuilder()
        startActivityForResult(intentBuilder.build(this), PLACE_PICK_REQUEST)
    }


    private var onDMStatusChanged: ((Boolean) -> Unit)? = null
    fun setOnDMStatusChangeListener(l: (Boolean) -> Unit) {
        onDMStatusChanged = l
    }

    private var onAddNewPlace: (() -> Unit)? = null
    fun setOnAddNewPlaceListener(l: () -> Unit) {
        onAddNewPlace = l
    }

    private fun showFailDialog() {
        val contextThemeWrapper = ContextThemeWrapper(this, R.style.DialogStyle)
        val builder = AlertDialog.Builder(contextThemeWrapper)
                .setTitle("Oops")
                .setMessage("Please Turn on location ")
                .setPositiveButton("Turn On") { dialog, _ ->
                    dialog.dismiss()
                    this.startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }

        val dialog = builder.create()
        dialog?.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        dialog.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.i("MainActivity"," smallest screen dpi ${resources.configuration.smallestScreenWidthDp}")
        with(tabLayout) {
            addTab(newTab().setIcon(R.drawable.ic_place))
            addTab(newTab().setIcon(R.drawable.ic_motorcycle))
        }


        Fabric.with(Fabric.Builder(this)
                .kits(Crashlytics())
                .debuggable(false)  // Enables Crashlytics debugger
                .build())

        val pagerAdapter = PagerAdapter(supportFragmentManager, tabLayout.tabCount)


        viewPager.apply {
            adapter = pagerAdapter
            offscreenPageLimit = 1
            addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        }

        ivAction.visibility = View.VISIBLE
        switchDriveMode.visibility = View.GONE

        val sharedPref = getSharedPreferences(
                resources.getString(R.string.SHARED_PREF_KEY),
                Context.MODE_PRIVATE
        )
        val isDriveSwitchEnabled = sharedPref.getInt(
                resources.getString(R.string.DRIVE_MODE_ENABLED),
                -1
        )
        switchDriveMode.isChecked = isDriveSwitchEnabled == 1

        ivAction.setOnClickListener {
            val gpsEnabled = (getSystemService(Context.LOCATION_SERVICE) as LocationManager)
                    .isProviderEnabled(LocationManager.GPS_PROVIDER)
            if (gpsEnabled) {
                it.setOnTouchListener { _, _ -> true }
                pickPlace()
            } else {
                showFailDialog()
            }
        }

        ivOptions.setOnClickListener { _ ->
            val popup = PopupMenu(this, ivOptions)
            val menuInflater = popup.menuInflater
            menuInflater.inflate(R.menu.menu, popup.menu)
            popup.setOnMenuItemClickListener {
                when (it?.itemId) {
                    R.id.action_settings -> {
                        startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                        true
                    }
                    R.id.action_priority -> {
                        startActivity(Intent(this@MainActivity, MyContactsActivity::class.java))
                        true
                    }
                    else -> {
                        false
                    }

                }
            }
            popup.show()
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab?) {
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
            }

            override fun onTabSelected(p0: TabLayout.Tab) {

                viewPager.setCurrentItem(p0.position, true)
                when (p0.position) {
                    0 -> {
                        tvFragTitle.text = "Work Places"
                        ivAction.visibility = View.VISIBLE
                        switchDriveMode.visibility = View.GONE
                    }
                    1 -> {
                        tvFragTitle.text = "Drive Mode"
                        ivAction.visibility = View.INVISIBLE
                        switchDriveMode.visibility = View.VISIBLE

                        switchDriveMode.setOnCheckedChangeListener(null)
                        switchDriveMode.setOnCheckedChangeListener { _, isChecked ->
                            onDMStatusChanged?.invoke(isChecked)
                            with(sharedPref.edit()) {
                                val isEnabled = if (isChecked) 1 else 0
                                putInt(resources.getString(R.string.DRIVE_MODE_ENABLED), isEnabled)
                                apply()
                            }
                        }
                    }
                }
            }
        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        ivAction.setOnTouchListener(null)
        if (requestCode == PLACE_PICK_REQUEST && resultCode == Activity.RESULT_OK) {

            repository = Repository.getInstance(application)

            val contextThemeWrapper = ContextThemeWrapper(this, R.style.DialogStyle)
            val builder = AlertDialog.Builder(contextThemeWrapper)
                    .setTitle("Same Place Exists")
                    .setMessage("Same place address already exists." +
                            " If you want to modify some values please change in that place card only.")
                    .setPositiveButton("Ok") { dialog, _ ->
                        dialog.dismiss()
                    }
            val dialog = builder.create()
            dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
            repository.setExceptionDialog(dialog)

            val place = PlacePicker.getPlace(this, data)

            val nameDialog = EditPlaceNameDialog()
            nameDialog.iniName = place.name.toString()


            nameDialog.setOnNameConfirm {
                val gpsEnabled = (getSystemService(Context.LOCATION_SERVICE) as LocationManager)
                        .isProviderEnabled(LocationManager.GPS_PROVIDER)

                val placePrefs = PlacePrefs(
                        name = it,
                        address = place.address.toString(),
                        latitude = place.latLng.latitude,
                        longitude = place.latLng.longitude,
                        geoKey = generateGeoKey(),
                        radius = 500,
                        active = if (gpsEnabled) 1 else 0,
                        contactGroup = "All Contacts"

                )
                if (repository.insertPref(placePrefs))
                    onAddNewPlace?.invoke()
            }
            nameDialog.show(supportFragmentManager, "EditPlaceName")
        }
    }
}
