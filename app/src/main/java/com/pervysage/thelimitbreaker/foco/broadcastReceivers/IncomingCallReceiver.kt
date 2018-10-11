package com.pervysage.thelimitbreaker.foco.broadcastReceivers

import android.annotation.SuppressLint
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.provider.CallLog
import android.provider.ContactsContract
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.telecom.Call
import android.telephony.PhoneStateListener
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import com.pervysage.thelimitbreaker.foco.utils.DeviceMotionUtil
import com.pervysage.thelimitbreaker.foco.R
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

        private var focusRequest: AudioFocusRequest? = null

        private var tts: TextToSpeech = TextToSpeech(context.applicationContext, TextToSpeech.OnInitListener {
            if (it == TextToSpeech.SUCCESS) {
                Log.d(TAG, "tts init")
                speak()
            }
        })

        private val motionUtil = DeviceMotionUtil(context)

        private var name = ""
        private var toSay = ""

        private fun gainAudioFocus() {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                am.requestAudioFocus(focusRequest!!)
            } else {
                am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            }

        }

        private fun removeAudioFocus() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                am.abandonAudioFocusRequest(focusRequest!!)
            } else {
                am.abandonAudioFocus(null)
            }
        }

        private fun speak() {
            Log.d(TAG, "tts speak $name")

            tts.apply {
                language = Locale.US
                setSpeechRate(0.70f)
            }
            gainAudioFocus()

            tts.speak(toSay , TextToSpeech.QUEUE_FLUSH, null, "Pui")
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onError(utteranceId: String?) {
                    // TODO Not Implemented
                }

                override fun onStart(utteranceId: String?) {
                    // TODO Not Implemented
                }

                override fun onDone(utteranceId: String?) {
                    tts.speak(toSay , TextToSpeech.QUEUE_FLUSH, null, "Pui")
                }
            })
        }

        init {
            method1.isAccessible = true
            iTelephony = method1.invoke(tm)
            methodEndCall = iTelephony.javaClass.getDeclaredMethod("endCall")
            silenceRinger = iTelephony.javaClass.getDeclaredMethod("silenceRinger")
            am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK).build()
            }
        }


        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                Log.d(TAG, "incomingNumber : $phoneNumber")

                handleIncomingCall(phoneNumber)
            }
            if (state == TelephonyManager.CALL_STATE_IDLE) {
                Log.d(TAG, "state Idle")
                motionUtil.stopMotionListener()
                tts.stop()
                tts.shutdown()
                tts.setOnUtteranceProgressListener(null)
            }
        }

        private fun sendSMS(phoneNumber: String) {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(
                    phoneNumber,
                    null,
                    "Hello, this is Foco , Rohan is quite busy at the moment. Please Call him later if not important",
                    null,
                    null)
        }

        private fun handleIncomingCall(phoneNumber: String?) {
            val sharedPref = context.getSharedPreferences(
                    context.getString(R.string.SHARED_PREF_KEY),
                    Context.MODE_PRIVATE
            )

            Log.d(TAG, "Checking number ")
            name = checkNumber(phoneNumber!!)
            Log.d(TAG, "name $name")
            // exist in contacts or not
            if (name.isEmpty()) {
                Log.d(TAG, "Unknown Number")
                if (!isUnder15Min(phoneNumber)) {
                    methodEndCall.invoke(iTelephony)
                    sendSMS(phoneNumber)
                }else{
                    motionUtil.setAction {
                        Log.d(TAG, "end on flip")
                        methodEndCall.invoke(iTelephony)
                        sendSMS(phoneNumber)
                    }
                    motionUtil.setShakeACtion {
                        if (tts.isSpeaking) tts.stop()
                    }
                    motionUtil.startMotionListener()
                    toSay="This might be important call"
                    speak()
                }
            } else {
                // check contact group
                val activeContactGroup = sharedPref.getString(context.getString(R.string.ACTIVE_CONTACT_GROUP), "")
                when (activeContactGroup) {
                    "All Contacts" -> {
                        Log.d(TAG, "All Contacts")
                        motionUtil.setAction {
                            Log.d(TAG, "end on flip")
                            methodEndCall.invoke(iTelephony)
                            sendSMS(phoneNumber)
                        }
                        motionUtil.setShakeACtion {
                            if (tts.isSpeaking) tts.stop()
                        }
                        motionUtil.startMotionListener()
                        toSay="Call from $name"
                        speak()
                    }
                    "Priority Contacts" -> {
                        val repo = Repository.getInstance((context.applicationContext) as Application)
                        val numberParam = "%${phoneNumber.substring(3)}"
                        val contactInfo = repo.getInfoFromNumber(numberParam)
                        if (contactInfo != null) {
                            this@MyPhoneStateListener.name = name
                            Log.d(TAG, "in Priority")
                            motionUtil.setAction {
                                Log.d(TAG, "end on flip")
                                methodEndCall.invoke(iTelephony)
                                sendSMS(phoneNumber)
                            }
                            motionUtil.setShakeACtion {
                                if (tts.isSpeaking) tts.stop()
                            }
                            motionUtil.startMotionListener()
                            toSay = "Call from $name"
                            speak()

                        } else {
                            if (!isUnder15Min(phoneNumber)) {
                                methodEndCall.invoke(iTelephony)
                                sendSMS(phoneNumber)
                            }else {
                                motionUtil.setAction {
                                    Log.d(TAG, "end on flip")
                                    methodEndCall.invoke(iTelephony)
                                    sendSMS(phoneNumber)
                                }
                                motionUtil.setShakeACtion {
                                    if (tts.isSpeaking) tts.stop()
                                }
                                motionUtil.startMotionListener()
                                toSay="This might be important call. Call from $name"
                                speak()
                            }
                        }

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

        private fun checkNumber(phoneNumber: String): String {

            var number = phoneNumber
            if (phoneNumber.contains("+91"))
                number = phoneNumber.substring(3)

            Log.d(TAG, "checking ${phoneNumber.substring(3)}")
            val cursor = context.contentResolver.query(
                    ContactsContract.Data.CONTENT_URI,
                    arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER),
                    "${ContactsContract.Data.MIMETYPE} = ? " +
                            "AND ${ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER} LIKE ?",
                    arrayOf(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE, "%$number"),
                    null
            )

            cursor?.also {
                if (cursor.count > 0) {
                    cursor.moveToNext()
                    return cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                            ))
                }
            }
            return ""

        }


        @SuppressLint("MissingPermission")
        private fun isUnder15Min(phoneNumber: String): Boolean {
            val cursor = context.contentResolver.query(
                    CallLog.Calls.CONTENT_URI,
                    arrayOf(
                            CallLog.Calls.NUMBER,
                            CallLog.Calls.DATE
                    ),
                    "${CallLog.Calls.TYPE} LIKE ? OR ${CallLog.Calls.TYPE} LIKE ?  AND ${CallLog.Calls.NUMBER} LIKE ?",
                    arrayOf("${CallLog.Calls.REJECTED_TYPE}","${CallLog.Calls.MISSED_TYPE}", phoneNumber),
                    CallLog.Calls.DEFAULT_SORT_ORDER
            )

            Log.d(TAG, "call count ${cursor?.count}")


            cursor?.run {
                if (count > 0) {
                    val currentTime = System.currentTimeMillis()
                    while (moveToNext()) {
                        val time = getLong(getColumnIndexOrThrow(CallLog.Calls.DATE))
                        Log.d(TAG, "$currentTime - $time = ${currentTime - time} | ${15 * 60 * 1000}")
                        if (currentTime - time <= (15 * 60 * 1000)) return true
                    }
                }
                close()
            }

            return false
        }
    }


}