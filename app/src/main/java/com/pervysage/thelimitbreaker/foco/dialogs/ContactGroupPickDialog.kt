package com.pervysage.thelimitbreaker.foco.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.DialogFragment
import com.pervysage.thelimitbreaker.foco.R

class ContactGroupPickDialog : DialogFragment(), DialogInterface.OnClickListener {
    private var iniCheckedItem = -1
    private lateinit var onContactGroupPick: (String) -> Unit

    fun setIniCheckedItem(iniCheckedItem:Int){
        this.iniCheckedItem=iniCheckedItem
    }

    fun setOnContactGroupPickListener(l: (String) -> Unit) {
        onContactGroupPick = l
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        val group = when (which) {
            0 -> "All Contacts"
            1 -> "Priority Contacts"
            2 -> "None"
            else -> ""
        }
        Handler().postDelayed(
                {
                    onContactGroupPick(group)
                    dialog?.dismiss()
                },
                400)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        builder.setSingleChoiceItems(
                arrayOf("All Contacts", "Priority Contacts", "None"),
                iniCheckedItem,
                this

        )
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background)
        return dialog
    }
}