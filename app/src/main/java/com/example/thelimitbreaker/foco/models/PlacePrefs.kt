package com.example.thelimitbreaker.foco.models

data class PlacePrefs(
        val name:String,
        val address:String,
        val hour:Int,
        val minutes:Int,
        val radius:Int,
        val isOn:Int,
        val contactGroup:String,
        var isExpanded:Boolean
)