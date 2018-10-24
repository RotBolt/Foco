package com.pervysage.thelimitbreaker.foco.fragments


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pervysage.thelimitbreaker.foco.R
import com.pervysage.thelimitbreaker.foco.actvities.MainActivity
import com.pervysage.thelimitbreaker.foco.adapters.PlacePrefsAdapter
import com.pervysage.thelimitbreaker.foco.database.Repository
import kotlinx.android.synthetic.main.fragment_places.*

class PlacesFragment : Fragment() {

    private lateinit var repository: Repository
    private var isPlaceListEmpty = false


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_places, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.run {


            repository = Repository.getInstance(this.application)
            val placePrefList = repository.getAllPlacePrefs()
            isPlaceListEmpty = placePrefList.isEmpty()
            val placePrefAdapter = PlacePrefsAdapter(this, placePrefList = placePrefList, listView = lvPlaces)
            lvPlaces.adapter = placePrefAdapter

            placePrefAdapter.setOnEmptyListListener {
                isPlaceListEmpty=it
                toggleViews()
            }

            if (this is MainActivity) {
                setOnAddNewPlaceListener {

                    val list = repository.getAllPlacePrefs()
                    isPlaceListEmpty = list.isEmpty()
                    toggleViews()
                    placePrefAdapter.refreshList(list, true)
                }
            }

        }
    }

    private fun toggleViews() {
        if (isPlaceListEmpty) {
            lvPlaces.visibility = View.GONE
            noPlaceView.visibility = View.VISIBLE
        } else {
            lvPlaces.visibility = View.VISIBLE
            noPlaceView.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        toggleViews()
    }
}
