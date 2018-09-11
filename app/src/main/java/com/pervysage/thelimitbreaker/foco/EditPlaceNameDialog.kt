package com.pervysage.thelimitbreaker.foco

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView

class EditPlaceNameDialog : DialogFragment() {

    private lateinit var onNameConfirm: (name: String) -> Unit
    private lateinit var etPlaceName: EditText
    var hint = "Place Name"

    fun setOnNameConfirm(l: (String) -> Unit) {
        onNameConfirm = l
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val li = activity?.layoutInflater
        val itemView = li?.inflate(R.layout.layout_place_name_dialog, null)
        etPlaceName = itemView!!.findViewById(R.id.etPlaceName)
        etPlaceName.setText(hint, TextView.BufferType.EDITABLE)
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Enter place name")
                .setView(itemView)
                .setPositiveButton("Ok", null)
                .setNegativeButton("Cancel", null)
        val dialog = builder.create()

        dialog.setOnShowListener {
            val okBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            okBtn.setOnClickListener {
                val name = etPlaceName.text.toString()
                if(name.isBlank())
                    etPlaceName.hint="Place Name cannot be empty"
                else
                    dialog.dismiss()
            }

            val  cancelBtn = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            cancelBtn.setOnClickListener { dialog.dismiss() }
        }
        dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        return dialog
    }



}