package com.pervysage.thelimitbreaker.foco

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.DatePicker
import android.widget.TimePicker
import com.pervysage.thelimitbreaker.foco.database.PlacePrefs
import com.pervysage.thelimitbreaker.foco.database.Repository
import com.pervysage.thelimitbreaker.foco.fragments.PlacesFragment


class MainActivity : AppCompatActivity() {

    private var currTabPos = 0
    private val PLACE_PICK_REQUEST = 1
    private val LOCATION_PERMISSION = 1
    private val TAG = "MainActivity"

    private val pickPlace = {
        val intentBuilder = PlacePicker.IntentBuilder()
        startActivityForResult(intentBuilder.build(this), PLACE_PICK_REQUEST)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val iconActions = arrayOf(
                ContextCompat.getDrawable(this, R.drawable.ic_add_place),
                ContextCompat.getDrawable(this, R.drawable.ic_clock_add)
        )

        with(tabLayout) {
            addTab(newTab().setIcon(R.drawable.ic_place))
            addTab(newTab().setIcon(R.drawable.ic_timelapse))
            addTab(newTab().setIcon(R.drawable.ic_motorcycle))
        }

        val pagerAdapter = PagerAdapter(supportFragmentManager, tabLayout.tabCount)

        viewPager.adapter = pagerAdapter
        viewPager.offscreenPageLimit = 2
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab?) {
            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {
            }

            override fun onTabSelected(p0: TabLayout.Tab?) {
                currTabPos = p0!!.position
                if (currTabPos < 2)
                    ivAction.setImageDrawable(iconActions[currTabPos])
                viewPager.setCurrentItem(p0.position, true)
                Log.d(TAG, "tab selected")
            }

        })

        ivAction.setOnClickListener {
            when (currTabPos) {
                0 -> {
                    // Place Picker

                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        pickPlace()
                    } else {
                        ActivityCompat.requestPermissions(
                                this,
                                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                                LOCATION_PERMISSION)
                    }
                }
                1 -> {
                    TimePickerDialog(this,
                            TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute -> },12,0,false).show()
                }
            }
        }


    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == LOCATION_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickPlace()
            } else {
                Toast.makeText(this, "Grant Permission to add place", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PLACE_PICK_REQUEST && resultCode == Activity.RESULT_OK) {
            val place = PlacePicker.getPlace(this, data)
            val lat = place.latLng.latitude
            val lon = place.latLng.longitude
            val name = place.name
            val addr = place.address
            val hour = 5
            val min = 30
            val radius = 500
            val isOn = 1
            val isExpanded = false
            val placePref = PlacePrefs(
                    name = name.toString(),
                    address = addr.toString(),
                    latitude = lat,
                    longitude = lon,
                    hour = hour,
                    minutes = min,
                    radius = radius,
                    active = isOn,
                    contactGroup = "All Contacts",
                    isExpanded = isExpanded
            )
            val repo=Repository.getInstance(application)
            repo.insert(prefs = placePref)

        }
    }
}
