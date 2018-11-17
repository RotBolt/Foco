package com.pervysage.thelimitbreaker.foco.actvities

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.pervysage.thelimitbreaker.foco.R
import kotlinx.android.synthetic.main.activity_settings.*


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        ivBack.setOnClickListener {
            finish()
        }

        val sharedPrefs = getSharedPreferences(getString(R.string.SHARED_PREF_KEY), Context.MODE_PRIVATE)

        setAllowCallerStatus(sharedPrefs)
        setSMSToCallerStatus(sharedPrefs)
        setSayCallerNameStatus(sharedPrefs)
        setAutoStart()
        setPrivacyPolicy()
        setExperimentals(sharedPrefs)
    }

    private fun setExperimentals(sharedPrefs: SharedPreferences) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
            val flipToEnd = sharedPrefs.getBoolean(getString(R.string.FLIP_TO_END_STATUS), false)
            switchFlipToEnd.isChecked = flipToEnd
            switchFlipToEnd.setOnCheckedChangeListener { _, isChecked ->
                sharedPrefs.edit().putBoolean(getString(R.string.FLIP_TO_END_STATUS), isChecked).commit()
            }

            val shakeToMuteStatus = sharedPrefs.getBoolean(getString(R.string.SHAKE_STATUS), false)
            switchShakeToMute.isChecked = shakeToMuteStatus
            switchShakeToMute.setOnCheckedChangeListener { _, isChecked ->
                sharedPrefs.edit().putBoolean(getString(R.string.SHAKE_STATUS), isChecked).commit()
            }
        } else {
            flipToEndContainer.visibility = View.GONE
            shakeToMuteContainer.visibility = View.GONE
            sharedPrefs.edit().putBoolean(getString(R.string.FLIP_TO_END_STATUS), false).commit()
            sharedPrefs.edit().putBoolean(getString(R.string.SHAKE_STATUS), false).commit()
        }
    }

    private fun setAllowCallerStatus(sharedPrefs: SharedPreferences) {
        val allowCallerStatus = sharedPrefs.getBoolean(getString(R.string.ALLOW_CALLER_STATUS), true)
        switchAllowCaller.isChecked = allowCallerStatus
        switchAllowCaller.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean(getString(R.string.ALLOW_CALLER_STATUS), isChecked).commit()
        }
    }

    private fun setSMSToCallerStatus(sharedPrefs: SharedPreferences) {
        val smsToCallerStatus = sharedPrefs.getBoolean(getString(R.string.SMS_TO_CALLER), false)
        switchSMSToCaller.isChecked = smsToCallerStatus
        setCustomizeMessage(smsToCallerStatus,sharedPrefs)
        switchSMSToCaller.setOnCheckedChangeListener { _, isChecked ->
            setCustomizeMessage(isChecked,sharedPrefs)
            sharedPrefs.edit().putBoolean(getString(R.string.SMS_TO_CALLER), isChecked).commit()
        }
    }

    private fun setSayCallerNameStatus(sharedPrefs: SharedPreferences) {
        val sayCallerNameStatus = sharedPrefs.getBoolean(getString(R.string.SAY_CALLER_NAME), true)
        switchSayCallerName.isChecked = sayCallerNameStatus
        fun changeShakeToMuteStatus(sayCallerNameStatus: Boolean) {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
                if (!sayCallerNameStatus) {
                    tvShakeToMute.setTextColor(ContextCompat.getColor(this, R.color.colorTextDisable))
                    switchShakeToMute.isChecked = false
                    switchShakeToMute.isEnabled = false
                    sharedPrefs.edit().putBoolean(getString(R.string.SHAKE_STATUS), false).commit()
                } else {
                    tvShakeToMute.setTextColor(ContextCompat.getColor(this, R.color.colorGenDark))
                    switchShakeToMute.isEnabled = true
                }
            }
        }
        changeShakeToMuteStatus(sayCallerNameStatus)
        switchSayCallerName.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean(getString(R.string.SAY_CALLER_NAME), isChecked).commit()
            changeShakeToMuteStatus(isChecked)
        }


    }

    private fun setAutoStart() {
        try {
            val autoStartIntent = Intent()
            val manufacturer = android.os.Build.MANUFACTURER
            when {
                "xiaomi".equals(manufacturer, ignoreCase = true) -> autoStartIntent.component = ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
                "oppo".equals(manufacturer, ignoreCase = true) -> autoStartIntent.component = ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")
                "vivo".equals(manufacturer, ignoreCase = true) -> autoStartIntent.component = ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")
                "Letv".equals(manufacturer, ignoreCase = true) -> autoStartIntent.component = ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")
                "Honor".equals(manufacturer, ignoreCase = true) -> autoStartIntent.component = ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")
            }

            val list = packageManager.queryIntentActivities(autoStartIntent, PackageManager.MATCH_DEFAULT_ONLY)
            if (list.size > 0) {
                enableAutoStart.setOnClickListener { startActivity(autoStartIntent) }
            } else {
                enableAutoStart.visibility = View.GONE
            }
        } catch (e: Exception) {
            enableAutoStart.visibility = View.GONE
        }

    }

    private fun setPrivacyPolicy() {
        privacyPolicy.setOnClickListener {
            startActivity(Intent(this, PrivacyPolicyActivity::class.java))
        }
    }

    private fun setCustomizeMessage(isEnabled:Boolean,sharedPrefs: SharedPreferences){
        if (isEnabled){
            customizeMsg.setOnClickListener {
                showDialogCustomizeMessage(sharedPrefs)
            }
            tvCustomSms.setTextColor(ContextCompat.getColor(this,R.color.colorGenDark))
            ivNextSms.colorFilter=PorterDuffColorFilter(ContextCompat.getColor(this,R.color.colorGenDark),PorterDuff.Mode.SRC_ATOP)
        }else{
            customizeMsg.setOnClickListener(null)
            tvCustomSms.setTextColor(ContextCompat.getColor(this,R.color.colorTextDisable))
            ivNextSms.colorFilter=PorterDuffColorFilter(ContextCompat.getColor(this,R.color.colorTextDisable),PorterDuff.Mode.SRC_ATOP)
        }
    }
    private fun showDialogCustomizeMessage(sharedPrefs: SharedPreferences){
        val theme = ContextThemeWrapper(this,R.style.DialogStyle)
        val li = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val itemView = li.inflate(R.layout.layout_custom_message,null)
        val etDriveModeMsg = itemView.findViewById<EditText>(R.id.etDriveModeMsg)
        val etWorkPlaceMsg = itemView.findViewById<EditText>(R.id.etWorkPlaceMsg)
        val defaultDmMsg = sharedPrefs.getString(getString(R.string.DRIVE_MODE_MSG),getString(R.string.DEFAULT_DM_MSG))?:getString(R.string.DEFAULT_DM_MSG)
        val defaultWpMsg = sharedPrefs.getString(getString(R.string.WORK_PLACE_MSG),getString(R.string.DEFAULT_WP_MSG))?:getString(R.string.DEFAULT_WP_MSG)

        etDriveModeMsg.setText(defaultDmMsg,TextView.BufferType.EDITABLE)
        etWorkPlaceMsg.setText(defaultWpMsg,TextView.BufferType.EDITABLE)


        val builder = AlertDialog.Builder(theme)
                .setView(itemView)
                .setPositiveButton("Ok"){ dialog, _ ->
                   val dmMsg = etDriveModeMsg.text
                    val wpMsg = etWorkPlaceMsg.text
                    when {
                        dmMsg.isEmpty() -> etDriveModeMsg.hint="Message Cannot be empty"
                        wpMsg.isEmpty() -> etDriveModeMsg.hint="Message Cannot be empty"
                        else -> {
                            sharedPrefs.edit().apply {
                                putString(getString(R.string.DRIVE_MODE_MSG),dmMsg.toString())
                                putString(getString(R.string.WORK_PLACE_MSG),wpMsg.toString())
                            }.commit()
                            dialog.dismiss()
                        }
                    }
                }.setNegativeButton("Cancel"){ dialog, _ ->
                    dialog.dismiss()
                }
        val dialog = builder.create()
        dialog.window.setBackgroundDrawableResource(R.drawable.dialog_background)
        dialog.show()
    }
}
