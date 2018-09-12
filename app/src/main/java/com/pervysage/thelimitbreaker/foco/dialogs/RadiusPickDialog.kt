package com.pervysage.thelimitbreaker.foco.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.DialogFragment
import com.pervysage.thelimitbreaker.foco.R

class RadiusPickDialog:DialogFragment(),DialogInterface.OnClickListener{

    private lateinit var onRadiusPick:(String,Int)->Unit
    private var iniCheckedItem = -1

    fun setOnRadiusPickListener(l:(String,Int)->Unit,iniCheckedItem:Int){
        onRadiusPick=l
        this.iniCheckedItem=iniCheckedItem
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        var radius = ""
        var radiusInt = 0
        when (which) {
            0 -> {
                radius = "500 m"
                radiusInt = 500
            }
            1 -> {
                radius = "1 km"
                radiusInt = 1000
            }
            2 -> {
                radius = "2 km"
                radiusInt = 2000
            }
            3 -> {
                radius = "5 km"
                radiusInt = 5000
            }
        }
        Handler().postDelayed({
            onRadiusPick(radius,radiusInt)
            dialog?.dismiss()
        },400)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        builder.setSingleChoiceItems(
                arrayOf("500 m","1 km","2 km","5 km"),
                iniCheckedItem,
                this

        )
        val dialog = builder.create()
        dialog.window.setBackgroundDrawableResource(R.drawable.dialog_background)
        return dialog
    }
}