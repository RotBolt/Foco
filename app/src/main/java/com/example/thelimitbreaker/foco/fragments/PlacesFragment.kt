package com.example.thelimitbreaker.foco.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ExpandableListView

import com.example.thelimitbreaker.foco.R
import com.example.thelimitbreaker.foco.adapters.PlaceAdapter

import com.example.thelimitbreaker.foco.models.PlacePrefs
import kotlinx.android.synthetic.main.fragment_places.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class PlacesFragment : Fragment() {
    private var lastExpandedPos=-1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_places, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val places =ArrayList<PlacePrefs>()
        for (i in 0 until 20){
            places.add(PlacePrefs(
                    "Mykonos $i",
                    "Greece",
                    5,
                    30,
                    500,
                    1,
                    "All contacts",
                    false
            ))
        }
        val adapter=PlaceAdapter(context!!,places,lvPlaces)
        lvPlaces.adapter=adapter



    }
}
