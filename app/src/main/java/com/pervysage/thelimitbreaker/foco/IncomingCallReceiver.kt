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
import com.pervysage.thelimitbreaker.foco.geofence.GeoActionsIntentService
import java.lang.reflect.Method
import java.util.*

class IncomingCallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        try {

            tm.listen(MyPhoneStateListner(context, tm), PhoneStateListener.LISTEN_CALL_STATE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    class MyPhoneStateListner(private val context: Context, tm: TelephonyManager) : PhoneStateListener() {
        private var method1: Method = tm.javaClass.getDeclaredMethod("getITelephony")
        private var iTelephony: Any
        private var methodEndCall: Method
        private var silenceRinger:Method
        private var am: AudioManager
        private var tts : TextToSpeech

        private var name=""


        private fun speak(){
            Log.d("CallReceiver","speak ")

                tts.language= Locale.US
                tts.setOnUtteranceProgressListener(object :UtteranceProgressListener(){
                    override fun onError(utteranceId: String?) {
                        // TODO Not Implemented
                    }

                    override fun onStart(utteranceId: String?) {
                        // TODO Not Implemented
                    }

                    override fun onDone(utteranceId: String?) {
                        if(name.isNotEmpty()){
                            tts.speak("Call from $name",TextToSpeech.QUEUE_ADD,null,"Pui")
                        }else{
                            tts.setOnUtteranceProgressListener(null)
                            if (tts.isSpeaking){
                                tts.stop()
                            }
                        }
                    }

                })
                tts.speak("Call from $name",TextToSpeech.QUEUE_ADD,null,"Pui")
        }

        init {
            tts= TextToSpeech(context.applicationContext, TextToSpeech.OnInitListener {
                if(it==TextToSpeech.SUCCESS){
                    Log.d("CallReceiver","Yuppaaaa")
                    speak()
                }
            })
            method1.isAccessible = true
            iTelephony = method1.invoke(tm)
            methodEndCall = iTelephony.javaClass.getDeclaredMethod("endCall")
            silenceRinger=iTelephony.javaClass.getDeclaredMethod("silenceRinger")
            am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        }


        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                Log.d("CallReceiver", "incomingNumber : $phoneNumber MonitorStatus ${GeoActionsIntentService.isMonitorOn}")

                if (GeoActionsIntentService.isMonitorOn) {
                    Log.d("CallReceiver","Checking ")
                    val cursor = context.contentResolver.query(
                            ContactsContract.Data.CONTENT_URI,
                            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                            "${ContactsContract.CommonDataKinds.Phone.NUMBER} = ?",
                            arrayOf(phoneNumber),
                            null
                    )
                    // exist in contacts or not
                    if (cursor.count < 1) {
                        Log.d("CallReceiver","Unknown Number")
                        methodEndCall.invoke(iTelephony)
                    } else {
                        // check contact group
                        Log.d("CallReceiver","contactgroup ${GeoActionsIntentService.contactGroup}")
                        when (GeoActionsIntentService.contactGroup) {
                            "All Contacts" -> {
                                Log.d("CallReceiver","All Contacts")

                            }
                            "Priority Contacts" -> {
                                val repo = Repository.getInstance((context.applicationContext) as Application)
                                val contactInfo = repo.getInfoFromNumber(phoneNumber!!)
                                contactInfo?.run {
                                    this@MyPhoneStateListner.name=name
                                    speak()

                                }?:methodEndCall.invoke(iTelephony)

                            }
                            "None" -> {
                                methodEndCall.invoke(iTelephony)
                            }
                        }
                    }
                }
            }
            if (state == TelephonyManager.CALL_STATE_IDLE) {
                Log.d("CallReceiver","state Idle monitor ${GeoActionsIntentService.isMonitorOn}")
                tts.setOnUtteranceProgressListener(null)
                if (tts.isSpeaking)tts.stop()
                if (GeoActionsIntentService.isMonitorOn) {
                }
            }

        }
    }

}