package com.luncher
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
class CallReceiver: BroadcastReceiver(){
    override fun onReceive(c: Context, i: Intent){
        try{
            if(i.action==TelephonyManager.ACTION_PHONE_STATE_CHANGED){
                val state = i.getStringExtra(TelephonyManager.EXTRA_STATE)
                val number = i.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                if(state==TelephonyManager.EXTRA_STATE_RINGING && number!=null){
                    OverlayNotifService.showCall(c, number)
                }
            }
        }catch(_:Exception){}
    }
}
