package com.pervysage.thelimitbreaker.foco.actvities

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.SeekBar
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
        val allowCallerStatus = sharedPrefs.getBoolean(getString(R.string.ALLOW_CALLER_STATUS), true)
        switchAllowCaller.isChecked = allowCallerStatus
        switchAllowCaller.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean(getString(R.string.ALLOW_CALLER_STATUS), isChecked).apply()
        }


        val smsToCallerStatus = sharedPrefs.getBoolean(getString(R.string.SMS_TO_CALLER), false)
        switchSMSToCaller.isChecked = smsToCallerStatus
        switchSMSToCaller.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean(getString(R.string.SMS_TO_CALLER), isChecked).apply()
        }

        if (Build.VERSION.SDK_INT<=Build.VERSION_CODES.O_MR1) {
            val flipToEnd = sharedPrefs.getBoolean(getString(R.string.FLIP_TO_END_STATUS), true)
            switchFlipToEnd.isChecked = flipToEnd
            switchFlipToEnd.setOnCheckedChangeListener { _, isChecked ->
                sharedPrefs.edit().putBoolean(getString(R.string.FLIP_TO_END_STATUS), isChecked).apply()
            }
        }else{
            flipToEndContainer.visibility= View.GONE
            shakeToMuteContainer.visibility=View.GONE
            sharedPrefs.edit().putBoolean(getString(R.string.FLIP_TO_END_STATUS),false).apply()
        }
    }
}
