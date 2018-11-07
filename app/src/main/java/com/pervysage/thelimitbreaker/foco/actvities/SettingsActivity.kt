package com.pervysage.thelimitbreaker.foco.actvities

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
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
        switchSMSToCaller.setOnCheckedChangeListener { _, isChecked ->
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
}
