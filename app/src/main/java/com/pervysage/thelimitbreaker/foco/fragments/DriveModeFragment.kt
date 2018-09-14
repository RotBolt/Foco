package com.pervysage.thelimitbreaker.foco.fragments


import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.pervysage.thelimitbreaker.foco.MainActivity

import com.pervysage.thelimitbreaker.foco.R
import com.pervysage.thelimitbreaker.foco.adapters.DMPriorityContactsAdapter
import kotlinx.android.synthetic.main.fragment_drive_mode.*


class DriveModeFragment : Fragment() {

    private var isFragEnabled = -1
    private var isGroupPriority = false
    private var isContactListEmpty=false
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_drive_mode, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        Log.d("PUI","onViewCreated")

        val contactList = listOf("Rushi-Chan", "ChunnuMati", "Pooja Di", "Rituka-Chan",
                "Ammi-jaan", "Boni-di", "Tou-San", "Varun", "Garvit", "Rajneesjkanth",
                "Mayank (MAIT) Garg", "Abhishek Verma (MAIT)",
                "Rin Nohara", "Obito Uchiha", "Kakashi Hatake"
        )
        val  listSize = contactList.size

        isContactListEmpty=listSize==0

        val contactAdapter = DMPriorityContactsAdapter(
                contactList,
                context!!
        )
        rvPriorityContacts.layoutManager = GridLayoutManager(context!!, 2)
        rvPriorityContacts.adapter = contactAdapter
        rvPriorityContacts.setHasFixedSize(true)

        rvPriorityContacts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (recyclerView.canScrollVertically(-1)) {
                    dmContainer.elevation = 16f
                } else {
                    dmContainer.elevation = 0f
                }
            }

        })

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
                            isGroupPriority = false
                            tvInfoBox.visibility = View.VISIBLE
                            btnAddContacts.visibility = View.GONE
                            tvInfoBox.text = "Receive calls from all contacts in your contact list"
                            rvPriorityContacts.visibility = View.GONE
                            group = "All Contacts"
                        }
                        1 -> {
                            isGroupPriority = true
                            tvInfoBox.visibility = View.GONE
                            btnAddContacts.visibility = View.VISIBLE
                            toggleContactListView()
                            group = "Priority Contacts"
                        }
                        2 -> {
                            isGroupPriority = false
                            tvInfoBox.text = "Total Silence !"
                            btnAddContacts.visibility = View.GONE
                            tvInfoBox.visibility = View.VISIBLE
                            rvPriorityContacts.visibility = View.GONE
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

            toggleContactListView()

            btnAddContacts.isEnabled = true
            btnAddContacts.setTextColor(ContextCompat.getColor(context!!, R.color.colorTextDark))
            val drawable = ContextCompat.getDrawable(context!!, R.drawable.ic_person_add)!!.mutate()
            btnAddContacts.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        } else {
            isFragEnabled = 0

            ivDriveMode.setImageDrawable(ContextCompat.getDrawable(context!!, R.drawable.ic_scooter_disabled))

            tvContactGroupLabel.setTextColor(ContextCompat.getColor(context!!, R.color.colorTextDisable))

            tvInfoBox.setTextColor(ContextCompat.getColor(context!!, R.color.colorTextDisable))

            groupChooser.isEnabled = false

            tvNoContacts.visibility=View.GONE

            rvPriorityContacts.visibility = View.GONE

            btnAddContacts.isEnabled = false
            btnAddContacts.setTextColor(ContextCompat.getColor(context!!, R.color.colorTextDisable))
            val drawable = ContextCompat.getDrawable(context!!, R.drawable.ic_person_add)!!.mutate()
            drawable.colorFilter = PorterDuffColorFilter(
                    ContextCompat.getColor(context!!, R.color.colorTextDisable),
                    PorterDuff.Mode.SRC_ATOP
            )
            btnAddContacts.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        }

    }

    private fun toggleContactListView(){
        if (!isContactListEmpty && isGroupPriority && isFragEnabled==1){
            rvPriorityContacts.visibility=View.VISIBLE
            tvNoContacts.visibility=View.GONE
        }else if (isContactListEmpty && isGroupPriority && isFragEnabled==1){
            rvPriorityContacts.visibility=View.GONE
            tvNoContacts.visibility=View.VISIBLE
        }
    }

    override fun onResume() {
        Log.d("PUI","onResume")
        super.onResume()
        toggleContactListView()

    }


}


