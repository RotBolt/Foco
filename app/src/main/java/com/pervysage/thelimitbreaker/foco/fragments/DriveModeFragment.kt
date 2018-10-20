package com.pervysage.thelimitbreaker.foco.fragments


import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.pervysage.thelimitbreaker.foco.actvities.MainActivity

import com.pervysage.thelimitbreaker.foco.R
import com.pervysage.thelimitbreaker.foco.broadcastReceivers.DriveModeRecogReceiver
import com.pervysage.thelimitbreaker.foco.utils.DriveActivityRecogUtil
import com.pervysage.thelimitbreaker.foco.utils.sendDriveModeNotification
import kotlinx.android.synthetic.main.fragment_drive_mode.*


class DriveModeFragment : Fragment() {

    private var isFragEnabled = -1
    private lateinit var driveActivityRecogUtil: DriveActivityRecogUtil

    private var dmActiveGroup = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        context?.run {
            driveActivityRecogUtil = DriveActivityRecogUtil(this)
        }
        return inflater.inflate(R.layout.fragment_drive_mode, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        val sharedPrefs = context?.getSharedPreferences(context?.getString(R.string.SHARED_PREF_KEY), Context.MODE_PRIVATE)

        val contactGroup = sharedPrefs?.getString(activity!!.resources.getString(R.string.CONTACT_GROUP_KEY), "All Contacts")

        val groupAdapter = ArrayAdapter(context!!,
                R.layout.layout_spinner_item,
                arrayOf("All Contacts", "Priority Contacts", "None")
        )
        groupAdapter.setDropDownViewResource(R.layout.layout_spinner_dropdown_item)
        groupChooser.apply {
            adapter = groupAdapter

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                    var group = "All Contacts"
                    when (position) {
                        0 -> {
                            tvInfoBox.text = "Receive calls from all contacts in your contact list"
                            group = "All Contacts"
                        }
                        1 -> {
                            tvInfoBox.text = "Receive calls from Priority People only"
                            group = "Priority Contacts"
                        }
                        2 -> {
                            tvInfoBox.text = "Total Silence !"
                            tvInfoBox.visibility = View.VISIBLE
                            group = "None"
                        }
                    }
                    dmActiveGroup = group
                    sharedPrefs?.edit()?.putString(context.getString(R.string.DM_ACTIVE_GROUP), group)?.apply()
                }
            }

            when (contactGroup) {
                "All Contacts" -> setSelection(0)
                "Priority Contacts" -> setSelection(1)
                "None" -> setSelection(2)
                else -> setSelection(0)
            }
        }

        isFragEnabled = sharedPrefs?.getInt(activity!!.resources.getString(R.string.DRIVE_MODE_ENABLED), -1) ?: 0
        if (isFragEnabled == 1) {
            context?.run {
                ivDriveMode.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_scooter))
            }
        } else {
            applyState(false)

        }

        activity?.run {
            if (this is MainActivity) {
                setOnDMStatusChangeListener { applyState(it) }
            }
        }

        super.onViewCreated(view, savedInstanceState)
    }


    private fun applyState(state: Boolean) {

        if (state) {
            isFragEnabled = 1

            context?.run {

                val sharedPrefs = this.getSharedPreferences(this.getString(R.string.SHARED_PREF_KEY), Context.MODE_PRIVATE)

                sharedPrefs.edit().putString(this.getString(R.string.DM_ACTIVE_GROUP), dmActiveGroup).apply()

                ivDriveMode.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_scooter))

                tvContactGroupLabel.setTextColor(ContextCompat.getColor(this, R.color.colorTextDark))

                tvInfoBox.setTextColor(ContextCompat.getColor(this, R.color.colorTextDark))
            }

            driveActivityRecogUtil.startDriveModeRecog()

            groupChooser.isEnabled = true

        } else {
            isFragEnabled = 0

            driveActivityRecogUtil.stopDriveModeRecog()

            context?.run {

                val sharedPrefs = this.getSharedPreferences(this.getString(R.string.SHARED_PREF_KEY), Context.MODE_PRIVATE)

                sharedPrefs.edit().putString(this.getString(R.string.DM_ACTIVE_GROUP), "").apply()

                val am = this.getSystemService(Context.AUDIO_SERVICE) as AudioManager

                val dmStatus = sharedPrefs.getBoolean(this.getString(R.string.DM_STATUS), false)

                if (dmStatus) {
                    sendDriveModeNotification("Drive Mode Stopped", "Service Stopped",false, this)
                    am.ringerMode = AudioManager.RINGER_MODE_NORMAL
                    val maxVolume = (am.getStreamMaxVolume(AudioManager.STREAM_RING) * 0.90).toInt()
                    am.setStreamVolume(AudioManager.STREAM_RING, maxVolume, AudioManager.FLAG_PLAY_SOUND)
                }


                ivDriveMode.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_scooter_disabled))

                tvContactGroupLabel.setTextColor(ContextCompat.getColor(this, R.color.colorTextDisable))

                tvInfoBox.setTextColor(ContextCompat.getColor(this, R.color.colorTextDisable))
            }

            groupChooser.isEnabled = false

        }

    }

}


