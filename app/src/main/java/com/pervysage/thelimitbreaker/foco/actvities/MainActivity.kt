package com.pervysage.thelimitbreaker.foco.actvities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TabLayout
import com.google.android.gms.location.places.ui.PlacePicker
import com.pervysage.thelimitbreaker.foco.adapters.PagerAdapter
import kotlinx.android.synthetic.main.activity_main.*
import android.widget.Toast
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.view.View
import com.pervysage.thelimitbreaker.foco.R
import com.pervysage.thelimitbreaker.foco.database.entities.PlacePrefs
import com.pervysage.thelimitbreaker.foco.database.Repository
import com.pervysage.thelimitbreaker.foco.database.entities.generateGeoKey
import android.os.Build
import android.support.v4.content.ContextCompat.getSystemService
import android.app.NotificationManager
import android.content.DialogInterface
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteException
import android.util.Log
import com.pervysage.thelimitbreaker.foco.services.ContactSyncIntentService


class MainActivity : AppCompatActivity() {


    private val PLACE_PICK_REQUEST = 1
    private val LOCATION_PERMISSION = 1
    private val READ_CALL_PERMISSIONS = 2

    private lateinit var repo:Repository

    private val TAG = "MainActivity"

    private var updateLeftOver: (() -> Unit)? = null

    fun setOnUpdateLeftOver(l: () -> Unit) {
        updateLeftOver = l
    }

    private val pickPlace = {
        val intentBuilder = PlacePicker.IntentBuilder()
        startActivityForResult(intentBuilder.build(this), PLACE_PICK_REQUEST)
    }

    private var onDMStatusChanged: ((Boolean) -> Unit)? = null

    fun setOnDMStatusChangeListener(l: (Boolean) -> Unit) {
        onDMStatusChanged = l
    }

    override fun onPause() {
        super.onPause()
        updateLeftOver?.run {
            this()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        repo= Repository.getInstance(application)

        val builder = AlertDialog.Builder(this)
                .setTitle("Same Place Exists")
                .setMessage("Same place address already exists." +
                        " If you want to modify some values please change in that place card")
                .setPositiveButton("Ok"){dialog, _ ->
                    dialog.dismiss()
                }
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)

        repo.setExceptionDialog(dialog)

        val sharedPref = getSharedPreferences(
                resources.getString(R.string.SHARED_PREF_KEY),
                Context.MODE_PRIVATE
        )
        val isDriveSwitchEnabled = sharedPref.getInt(
                resources.getString(R.string.DRIVE_MODE_ENABLED),
                -1
        )
        switchDriveMode.isChecked = isDriveSwitchEnabled == 1

        with(tabLayout) {
            addTab(newTab().setIcon(R.drawable.ic_place))
            addTab(newTab().setIcon(R.drawable.ic_motorcycle))
        }

        val pagerAdapter = PagerAdapter(supportFragmentManager, tabLayout.tabCount)

        viewPager.apply {
            adapter = pagerAdapter
            offscreenPageLimit = 1
            addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        }

        ivAction.visibility = View.VISIBLE
        switchDriveMode.visibility = View.GONE

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab?) {
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
            }

            override fun onTabSelected(p0: TabLayout.Tab?) {

                viewPager.setCurrentItem(p0!!.position, true)
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

                            if (onDMStatusChanged != null) {
                                onDMStatusChanged?.invoke(isChecked)
                            }

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

        ivAction.setOnClickListener {
            it.setOnTouchListener { _, _ -> true }
            pickPlace()
        }

        ivContacts.setOnClickListener {
            startActivity(Intent(this@MainActivity, MyContactsActivity::class.java))
        }


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        ivAction.setOnTouchListener(null)
        if (requestCode == PLACE_PICK_REQUEST && resultCode == Activity.RESULT_OK) {
            val place = PlacePicker.getPlace(this, data)
            val lat = place.latLng.latitude
            val lon = place.latLng.longitude
            val name = place.name
            val addr = place.address
            val radius = 500
            val isOn = 1
            val geoKey = generateGeoKey()
            val placePref = PlacePrefs(
                    name = name.toString(),
                    address = addr.toString(),
                    latitude = lat,
                    longitude = lon,
                    radius = radius,
                    geoKey = geoKey,
                    active = isOn,
                    contactGroup = "All Contacts"
            )

            repo.insertPref(prefs = placePref)



        }
    }
}
