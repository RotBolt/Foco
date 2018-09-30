package com.pervysage.thelimitbreaker.foco

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.provider.ContactsContract
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import com.pervysage.thelimitbreaker.foco.database.Repository
import java.lang.reflect.Method
import java.util.*

class IncomingCallReceiver : BroadcastReceiver() {
    private val TAG = "IncomingCallReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        val sharedPref = context.getSharedPreferences(
                context.getString(R.string.SHARED_PREF_KEY),
                Context.MODE_PRIVATE
        )
        val serviceStatus = sharedPref.getBoolean(context.getString(R.string.SERVICE_STATUS), false)
        Log.d(TAG, "service status $serviceStatus")
        if (serviceStatus) {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            try {
                tm.listen(MyPhoneStateListener(context, tm), PhoneStateListener.LISTEN_CALL_STATE)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    class MyPhoneStateListener(private val context: Context, tm: TelephonyManager) : PhoneStateListener() {
        private val TAG = "PhoneStateListener"
        private var method1: Method = tm.javaClass.getDeclaredMethod("getITelephony")
        private var iTelephony: Any
        private var methodEndCall: Method
        private var silenceRinger: Method
        private var am: AudioManager
        private var tts: TextToSpeech = TextToSpeech(context.applicationContext, TextToSpeech.OnInitListener {
            if (it == TextToSpeech.SUCCESS) {
                Log.d(TAG, "tts init")
                speak()
            }
        })

        private val motionUtil=DeviceMotionUtil(context)

        private var name = ""


        private fun speak() {
            Log.d(TAG, "tts speak $name")

            tts.language = Locale.US

            if (name.isNotEmpty()) {
                tts.speak("Call from $name", TextToSpeech.QUEUE_FLUSH, null, "Pui")
            }
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onError(utteranceId: String?) {
                    // TODO Not Implemented
                }

                override fun onStart(utteranceId: String?) {
                    // TODO Not Implemented
                }

                override fun onDone(utteranceId: String?) {
                    if (name.isNotEmpty()) {
                        Log.d(TAG, "Caller Name : $name")
                        tts.speak("Call from $name", TextToSpeech.QUEUE_FLUSH, null, "Pui")
                    } else {
                        tts.setOnUtteranceProgressListener(null)
                        if (tts.isSpeaking) {
                            tts.stop()
                        }
                    }
                }

            })
        }

        init {
            method1.isAccessible = true
            iTelephony = method1.invoke(tm)
            methodEndCall = iTelephony.javaClass.getDeclaredMethod("endCall")
            silenceRinger = iTelephony.javaClass.getDeclaredMethod("silenceRinger")
            am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        }


        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                Log.d(TAG, "incomingNumber : $phoneNumber")
                handleIncomingCall(phoneNumber)
            }
            if (state == TelephonyManager.CALL_STATE_IDLE) {
                Log.d(TAG, "state Idle")
                motionUtil.stopFlipListener()
                if (tts.isSpeaking) {
                    tts.stop()
                    tts.setOnUtteranceProgressListener(null)
                }

            }

        }

        private fun handleIncomingCall(phoneNumber: String?) {
            val sharedPref = context.getSharedPreferences(
                    context.getString(R.string.SHARED_PREF_KEY),
                    Context.MODE_PRIVATE
            )

            Log.d(TAG, "Checking number ")
            val cursor = context.contentResolver.query(
                    ContactsContract.Data.CONTENT_URI,
                    arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                    "${ContactsContract.CommonDataKinds.Phone.NUMBER} = ?",
                    arrayOf(phoneNumber),
                    null
            )
            // exist in contacts or not
            if (cursor.count < 1) {
                Log.d(TAG, "Unknown Number")
                methodEndCall.invoke(iTelephony)
            } else {
                // check contact group
                val activeContactGroup = sharedPref.getString(context.getString(R.string.ACTIVE_CONTACT_GROUP), "")
                when (activeContactGroup) {
                    "All Contacts" -> {
                        Log.d(TAG, "All Contacts")
                    }
                    "Priority Contacts" -> {
                        val repo = Repository.getInstance((context.applicationContext) as Application)
                        val contactInfo = repo.getInfoFromNumber(phoneNumber!!)
                        contactInfo?.run {
                            this@MyPhoneStateListener.name = name

                            motionUtil.setAction {
                                Log.d(TAG,"end on flip")
                                methodEndCall.invoke(iTelephony)
                            }
                            motionUtil.startFlipListener()
                            speak()

                        } ?: methodEndCall.invoke(iTelephony)

                    }
                    "None" -> {
                        methodEndCall.invoke(iTelephony)
                    }
                    else -> {
                        Log.e(TAG, "No Contact group set")
                    }
                }
            }

        }
    }

}