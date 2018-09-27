//package com.pervysage.thelimitbreaker.foco;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.media.AudioManager;
//import android.telephony.PhoneStateListener;
//import android.telephony.TelephonyManager;
//import android.util.Log;
//import android.widget.Toast;
//
//
//import java.lang.reflect.Method;
//
//
//public class CallReceiveBroadcastReceiver extends BroadcastReceiver {
//
//
//    private TelephonyManager m_telephonyManager;
//
//    private AudioManager m_audioManager;
//    Context context;
//
//    public void onReceive(Context context, Intent intent) {
//        this.context = context;
//        m_telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//        try {
////            Class c = null;
////            c = Class.forName(m_telephonyManager.getClass().getName());
////            Method m = null;
////            m = c.getDeclaredMethod("getITelephony");
////            m.setAccessible(true);
////            m_audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//            m_telephonyManager.listen(new MyPhoneStateListener(), PhoneStateListener.LISTEN_CALL_STATE);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    class MyPhoneStateListener extends PhoneStateListener {
//        public void onCallStateChanged(int state, String incomingNumber) {
//            Log.d("CallReceiver","incoming number "+incomingNumber);
//            Toast.makeText(context, incomingNumber, Toast.LENGTH_LONG).show();
//            switch (state) {
//                case TelephonyManager.CALL_STATE_RINGING:
//
//                    break;
//                default:
//                    break;
//            }
//        }
//
//
//    }
//}
