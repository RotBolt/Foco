package com.pervysage.thelimitbreaker.foco.broadcastReceivers

import android.annotation.SuppressLint
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.CallLog
import android.provider.ContactsContract
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.telecom.TelecomManager
import android.telephony.PhoneStateListener
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import com.pervysage.thelimitbreaker.foco.R
import com.pervysage.thelimitbreaker.foco.database.Repository
import com.pervysage.thelimitbreaker.foco.utils.DeviceMotionUtil
import java.lang.reflect.Method
import java.util.*
import kotlin.collections.HashMap

class IncomingCallReceiver : BroadcastReceiver() {

    companion object {
        private var receivedOnce = false
        private lateinit var tm: TelephonyManager
    }

    private lateinit var phoneStateListener: MyPhoneStateListener

    override fun onReceive(context: Context, intent: Intent) {
        if (!receivedOnce) {
            receivedOnce = true
            val sharedPref = context.getSharedPreferences(
                    context.getString(R.string.SHARED_PREF_KEY),
                    Context.MODE_PRIVATE
            )

            val dmStatus = sharedPref.getBoolean(context.getString(R.string.DM_STATUS), false)
            val serviceStatus = sharedPref.getBoolean(context.getString(R.string.GEO_STATUS), false)
            if (serviceStatus || dmStatus) {
                tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                phoneStateListener = MyPhoneStateListener(context, tm, dmStatus)
                tm.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
            }

        }

    }

    inner class MyPhoneStateListener(private val context: Context, tm: TelephonyManager, private val isDriveMode: Boolean) : PhoneStateListener() {

        private var method1: Method = tm.javaClass.getDeclaredMethod("getITelephony")
        private var iTelephony: Any
        private var methodEndCall: Method
        private var am: AudioManager
        private var focusRequest: AudioFocusRequest? = null
        private var teleCom: TelecomManager

        init {
            method1.isAccessible = true
            iTelephony = method1.invoke(tm)
            methodEndCall = iTelephony.javaClass.getDeclaredMethod("endCall")
//
            am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK).build()
            }

            teleCom = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        }

        private var tts: TextToSpeech = TextToSpeech(context.applicationContext, TextToSpeech.OnInitListener {
            if (it == TextToSpeech.SUCCESS) {
                speak()
            }
        })

        private val motionUtil = DeviceMotionUtil(context)

        private var name = ""
        private var toSay = ""

        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                phoneNumber?.run {
                    handleIncomingCall(this)
                }
            }
            if (state == TelephonyManager.CALL_STATE_IDLE || state == TelephonyManager.CALL_STATE_OFFHOOK) {
                tm.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
                receivedOnce = false
                motionUtil.stopMotionListener()
                tts.stop()
                tts.shutdown()
                tts.setOnUtteranceProgressListener(null)
            }

        }

        private fun gainAudioFocus() {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                am.requestAudioFocus(focusRequest)

            } else {
                am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            }


        }


        private fun startMotionUtil(phoneNumber: String, smsToCallerStatus: Boolean, flipToEnd: Boolean) {
            motionUtil.setAction {
                if (flipToEnd) {
                    methodEndCall.invoke(iTelephony)

                    if (smsToCallerStatus) {
                        sendSMS(phoneNumber)
                    }
                }
            }
            motionUtil.setShakeAction {
                if (tts.isSpeaking) tts.stop()
            }
            motionUtil.startMotionListener()
        }

        private fun speak() {
            gainAudioFocus()
            am.setStreamVolume(AudioManager.STREAM_MUSIC,am.getStreamMaxVolume(AudioManager.STREAM_MUSIC),AudioManager.FLAG_VIBRATE)
            tts.apply {
                language = Locale("hin", "IN")

                setSpeechRate(0.70f)
                speak(toSay, TextToSpeech.QUEUE_ADD,null,"pui")
                setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onError(utteranceId: String?) {
                        // TODO Not Implemented
                    }

                    override fun onStart(utteranceId: String?) {
                        // TODO Not Implemented
                    }

                    override fun onDone(utteranceId: String?) {

                        val sec = System.currentTimeMillis()
                        while (System.currentTimeMillis() - sec <= 1500) {
                        }
                        am.setStreamVolume(AudioManager.STREAM_MUSIC,am.getStreamMaxVolume(AudioManager.STREAM_MUSIC),AudioManager.FLAG_VIBRATE)
                        tts.speak(toSay, TextToSpeech.QUEUE_ADD, null,"pui")


                    }
                })
            }
        }


        private fun sendSMS(phoneNumber: String) {
            val smsManager = SmsManager.getDefault()
            val msg = if (isDriveMode)
                "Heya!! I am driving at the moment. Please Call me later if not important"
            else
                "Heya!! I am quite busy at the moment. Please Call me later if not important"

            smsManager.sendTextMessage(
                    phoneNumber,
                    null,
                    msg,
                    null,
                    null)

        }

        private fun handleIncomingCall(phoneNumber: String) {
            val sharedPref = context.getSharedPreferences(
                    context.getString(R.string.SHARED_PREF_KEY),
                    Context.MODE_PRIVATE
            )

            val allowCallerStatus = sharedPref.getBoolean(context.getString(R.string.ALLOW_CALLER_STATUS), true)
            val smsToCallerStatus = sharedPref.getBoolean(context.getString(R.string.SMS_TO_CALLER), false)
            val flipToEnd = sharedPref.getBoolean(context.getString(R.string.FLIP_TO_END_STATUS), true)

            name = checkNumber(phoneNumber)
            // exist in contacts or not
            if (name.isEmpty()) {
                if (allowCallerStatus && isUnder15Min(phoneNumber)) {
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1)
                        startMotionUtil(phoneNumber, smsToCallerStatus, flipToEnd)
                    toSay = "This might be important call"
                    speak()
                } else {
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1)
                        methodEndCall.invoke(iTelephony)
                    else
                        teleCom.endCall()

                    if (smsToCallerStatus)
                        sendSMS(phoneNumber)
                }
            } else {
                // check contact group
                val activeContactGroup = if (!isDriveMode)
                    sharedPref.getString(context.getString(R.string.ACTIVE_CONTACT_GROUP), "All Contacts")
                else
                    sharedPref.getString(context.getString(R.string.DM_ACTIVE_GROUP), "All Contacts")

                when (activeContactGroup) {
                    "All Contacts" -> {
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1)
                            startMotionUtil(phoneNumber, smsToCallerStatus, flipToEnd)
                        toSay = "Call from $name"
                        speak()


                    }
                    "Priority Contacts" -> {
                        val repo = Repository.getInstance((context.applicationContext) as Application)
                        val numberParam = "%${phoneNumber.substring(3)}"
                        val contactInfo = repo.getInfoFromNumber(numberParam)
                        if (contactInfo != null) {
                            this@MyPhoneStateListener.name = name
                            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1)
                                startMotionUtil(phoneNumber, smsToCallerStatus, flipToEnd)
                            toSay = "Call from $name"
                            speak()

                        } else {
                            if (allowCallerStatus && isUnder15Min(phoneNumber)) {
                                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1)
                                    startMotionUtil(phoneNumber, smsToCallerStatus, flipToEnd)
                                toSay = "This might be important call. Call from $name"
                                speak()

                            } else {
                                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1)
                                    methodEndCall.invoke(iTelephony)
                                else
                                    teleCom.endCall()
                                if (smsToCallerStatus)
                                    sendSMS(phoneNumber)
                            }
                        }

                    }
                    "None" -> {
                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1)
                            methodEndCall.invoke(iTelephony)
                        else
                            teleCom.endCall()
                    }
                }
            }
        }

        private fun checkNumber(phoneNumber: String): String {
            var number = phoneNumber
            if (phoneNumber.contains("+91"))
                number = phoneNumber.substring(3)

            val cursor = context.contentResolver.query(
                    ContactsContract.Data.CONTENT_URI,
                    arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER),
                    "${ContactsContract.Data.MIMETYPE} = ? " +
                            "AND ${ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER} LIKE ?",
                    arrayOf(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE, "%$number"),
                    null
            )

            cursor?.run {
                if (count > 0) {
                    moveToNext()
                    return getString(
                            getColumnIndexOrThrow(
                                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                            ))
                }
                close()
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
                    "${CallLog.Calls.TYPE} != ? AND ${CallLog.Calls.NUMBER} LIKE ?",
                    arrayOf("${CallLog.Calls.OUTGOING_TYPE}", phoneNumber),
                    CallLog.Calls.DEFAULT_SORT_ORDER
            )


            cursor?.run {
                if (count > 0) {
                    val currentTime = System.currentTimeMillis()
                    while (moveToNext()) {
                        val time = getLong(getColumnIndexOrThrow(CallLog.Calls.DATE))
                        if (currentTime - time <= (15 * 60 * 1000)) return true
                    }
                }
                close()
            }

            return false
        }
    }


}