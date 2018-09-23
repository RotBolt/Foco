package com.pervysage.thelimitbreaker.foco

import android.app.Activity
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
import com.pervysage.thelimitbreaker.foco.database.entities.PlacePrefs
import com.pervysage.thelimitbreaker.foco.database.Repository


class MainActivity : AppCompatActivity() {

    private var currTabPos = 0
    private val PLACE_PICK_REQUEST = 1
    private val LOCATION_PERMISSION = 1
    private val READ_CONTACTS_PERMISSION = 2
    private val TAG = "MainActivity"

    private val pickPlace = {
        val intentBuilder = PlacePicker.IntentBuilder()
        startActivityForResult(intentBuilder.build(this), PLACE_PICK_REQUEST)
    }


    private var onDMStatusChanged: ((Boolean) -> Unit)? = null

    fun setOnDMStatusChangeListener(l: (Boolean) -> Unit) {
        onDMStatusChanged = l
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        ivContacts.setOnClickListener {
            startActivity(Intent(this@MainActivity, MyContactsActivity::class.java))
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
        ivAction.setOnTouchListener(null)
        if (requestCode == PLACE_PICK_REQUEST && resultCode == Activity.RESULT_OK) {
            val place = PlacePicker.getPlace(this, data)
            val lat = place.latLng.latitude
            val lon = place.latLng.longitude
            val name = place.name
            val addr = place.address
            val radius = 500
            val isOn = 1
            val placePref = PlacePrefs(
                    name = name.toString(),
                    address = addr.toString(),
                    latitude = lat,
                    longitude = lon,
                    radius = radius,
                    active = isOn,
                    contactGroup = "All Contacts"
            )
            val repo = Repository.getInstance(application)
            repo.insertPref(prefs = placePref)

        }
    }
}
