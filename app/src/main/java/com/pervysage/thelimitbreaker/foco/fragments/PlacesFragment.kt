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
import com.pervysage.thelimitbreaker.foco.database.entities.PlacePrefs
import com.pervysage.thelimitbreaker.foco.database.Repository
import kotlinx.android.synthetic.main.fragment_places.*

class PlacesFragment : Fragment() {
    private var lastExpandedPos=-1
    private lateinit var repo:Repository
    private var isNew = false
    private var isPlaceListEmpty=false
    private val PLACE_PICK_REQUEST = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_places, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repo=Repository.getInstance(activity!!.application)


        val places =repo.getAllPlacePrefs()
        var listSize = -1
        places.observe(activity!!,object :Observer<List<PlacePrefs>>{
            override fun onChanged(t: List<PlacePrefs>?) {
                listSize=t?.size?:-1
                places.removeObserver(this)
            }

        })
        isPlaceListEmpty=listSize==0
        val adapter=PlaceAdapter(activity!!, ArrayList(),lvPlaces,repo)
        lvPlaces.adapter=adapter
        lvPlaces.setFriction(ViewConfiguration.getScrollFriction() * 2)
        places.observe(activity!!, Observer<List<PlacePrefs>>{
            if(listSize<it!!.size && listSize!=-1)
                isNew=true
            adapter.updateList(it,isNew)
            listSize=it.size
            isPlaceListEmpty=listSize==0
            toggleViews()
            isNew=false
        })
    }

    private fun toggleViews(){
        if (isPlaceListEmpty){
            lvPlaces.visibility=View.GONE
            noPlaceView.visibility=View.VISIBLE
        }else{
            lvPlaces.visibility=View.VISIBLE
            noPlaceView.visibility=View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        toggleViews()
    }
}
