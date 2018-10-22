package com.pervysage.thelimitbreaker.foco.actvities

import android.content.Context
import android.media.AudioManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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

        var volumeLevel = sharedPrefs.getInt(getString(R.string.RINGER_VOLUME), 90)

        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager


        ringerVolumeController.progress = volumeLevel

        ringerVolumeController.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                volumeLevel = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                Log.d("PUI","volume $volumeLevel")
                sharedPrefs.edit().putInt(getString(R.string.RINGER_VOLUME), volumeLevel).apply()
                val setVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)*0.01*volumeLevel
                am.setStreamVolume(AudioManager.STREAM_MUSIC,setVolume.toInt(),AudioManager.FLAG_PLAY_SOUND)
            }

        })


    }
}
