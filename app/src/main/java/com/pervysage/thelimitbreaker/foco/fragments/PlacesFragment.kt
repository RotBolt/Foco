package com.pervysage.thelimitbreaker.foco.fragments


import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import com.pervysage.thelimitbreaker.foco.R
import com.pervysage.thelimitbreaker.foco.adapters.PlaceAdapter
import com.pervysage.thelimitbreaker.foco.database.PlacePrefs
import com.pervysage.thelimitbreaker.foco.database.Repository
import kotlinx.android.synthetic.main.fragment_places.*


/**
 * A simple [Fragment] subclass.
 *
 */
class PlacesFragment : Fragment() {
    private var lastExpandedPos=-1
    private lateinit var repo:Repository
    private var isNew = false
    private val PLACE_PICK_REQUEST = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_places, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repo=Repository.getInstance(activity!!.application)



        val places =repo.getAllPlacePrefs()
        var listSize = places.value?.size?:0
        val adapter=PlaceAdapter(activity!!, ArrayList(),lvPlaces,repo)
        lvPlaces.adapter=adapter
        lvPlaces.setFriction(ViewConfiguration.getScrollFriction() * 2)
        places.observe(activity!!, Observer<List<PlacePrefs>>{
            if(listSize<it!!.size && listSize!=0)
                isNew=true
            adapter.updateList(it,isNew)
            listSize=it.size
            isNew=false
        })


    }



    override fun onStop() {
        super.onStop()
        Log.d("PUI","onStop")
    }

    override fun onPause() {
        super.onPause()
        Log.d("PUI","onPause PlaceFrag")
    }


}
