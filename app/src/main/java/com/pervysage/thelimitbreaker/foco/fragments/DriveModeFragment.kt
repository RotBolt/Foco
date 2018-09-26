package com.pervysage.thelimitbreaker.foco.fragments


import android.content.Context
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
import kotlinx.android.synthetic.main.fragment_drive_mode.*


class DriveModeFragment : Fragment() {

    private var isFragEnabled = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_drive_mode, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        Log.d("PUI","onViewCreated")

        val sharedPrefs = with(activity!!) {
            getSharedPreferences(resources.getString(R.string.SHARED_PREF_KEY), Context.MODE_PRIVATE)
        }

        val contactGroup = sharedPrefs.getString(activity!!.resources.getString(R.string.CONTACT_GROUP_KEY), "All Contacts")

        val groupAdapter = ArrayAdapter(context!!,
                android.R.layout.simple_spinner_item,
                arrayOf("All Contacts", "Priority Contacts", "None")
        )
        groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
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
                            tvInfoBox.text="Receive calls from Priority People only"
                            group = "Priority Contacts"
                        }
                        2 -> {
                            tvInfoBox.text = "Total Silence !"
                            tvInfoBox.visibility = View.VISIBLE
                            group = "None"
                        }
                    }
                    sharedPrefs.edit().putString(activity!!.resources.getString(R.string.CONTACT_GROUP_KEY), group).apply()
                }
            }

            when (contactGroup) {
                "All Contacts" -> setSelection(0)
                "Priority Contacts" -> setSelection(1)
                "None" -> setSelection(2)
            }
        }

        isFragEnabled = sharedPrefs.getInt(activity!!.resources.getString(R.string.DRIVE_MODE_ENABLED), -1)
        if (isFragEnabled == 1) {
            ivDriveMode.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_scooter))
        } else {
            applyState(false)

        }

        if (activity!! is MainActivity) {
            (activity as MainActivity).setOnDMStatusChangeListener {
                applyState(it)
            }
        }

        super.onViewCreated(view, savedInstanceState)
    }


    private fun applyState(state: Boolean) {
        if (state) {
            isFragEnabled = 1

            ivDriveMode.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_scooter))

            tvContactGroupLabel.setTextColor(ContextCompat.getColor(context!!, R.color.colorTextDark))

            tvInfoBox.setTextColor(ContextCompat.getColor(context!!, R.color.colorTextDark))

            groupChooser.isEnabled = true

        } else {
            isFragEnabled = 0

            ivDriveMode.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_scooter_disabled))

            tvContactGroupLabel.setTextColor(ContextCompat.getColor(context!!, R.color.colorTextDisable))

            tvInfoBox.setTextColor(ContextCompat.getColor(context!!, R.color.colorTextDisable))

            groupChooser.isEnabled = false

        }

    }

}


