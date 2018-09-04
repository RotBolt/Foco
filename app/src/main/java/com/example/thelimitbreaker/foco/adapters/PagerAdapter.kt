package com.example.thelimitbreaker.foco.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.example.thelimitbreaker.foco.fragments.DriveModeFragment
import com.example.thelimitbreaker.foco.fragments.MeTimeFragment
import com.example.thelimitbreaker.foco.fragments.PlacesFragment

class PagerAdapter(fm: FragmentManager, private val numOfTabs: Int) : FragmentStatePagerAdapter(fm) {
    override fun getItem(p0: Int): Fragment? =
            when (p0) {
                0 -> PlacesFragment()
                1 -> MeTimeFragment()
                2 -> DriveModeFragment()
                else -> null
            }


    override fun getCount() = numOfTabs

}