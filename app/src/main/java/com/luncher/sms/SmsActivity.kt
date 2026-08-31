package com.luncher.sms

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.media.MediaRecorder
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Telephony
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.luncher.util.GlassUtil
import com.luncher.util.IconUtil
import java.io.File

class SmsActivity: AppCompatActivity(){
    private lateinit var contactsRow:LinearLayout
    private lateinit var list:LinearLayout
    private lateinit var numInput:EditText
    private lateinit var msgInput:EditText
    private var recorder:MediaRecorder?=null
    private var isRecording=false

    override fun onCreate(b:Bundle?){
        super.onCreate(b)
        val t=GlassUtil.get(this)
        val root=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; background=GlassUtil.bg(t); setPadding(28,80,28,20)}

        root.addView(TextView(this).apply{ text="Messages"; textSize=32f; setTextColor(Color.WHITE); typeface=android.graphics.Typeface.DEFAULT_BOLD})

        root.addView(TextView(this).apply{ text="CONTACTS"; textSize=11f; setTextColor(Color.parseColor("#9AA0C0")); setPadding(0,24,0,8)})
        val scrollContacts=HorizontalScrollView(this).apply{ isHorizontalScrollBarEnabled=false}
        contactsRow=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL}
        scrollContacts.addView(contactsRow); root.addView(scrollContacts)

        val inputCard=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL; background=GlassUtil.card(24f, "#1AFFFFFF", "#22FFFFFF"); setPadding(20,20,20,20)
            layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,20,0,0)}
        }
        numInput=EditText(this).apply{ hint="Numéro"; setHintTextColor(Color.parseColor("#66FFFFFF")); setTextColor(Color.WHITE); background=GlassUtil.card(16f); setPadding(24,18,24,18)}
        msgInput=EditText(this).apply{ hint="Message..."; setHintTextColor(Color.parseColor("#66FFFFFF")); setTextColor(Color.WHITE); background=GlassUtil.card(16f); setPadding(24,18,24,18); layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,12,0,0)}}
        inputCard.addView(numInput); inputCard.addView(msgInput)
        val btnRow=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL; setPadding(0,16,0,0)}
        btnRow.addView(TextView(this).apply{
            text="ENVOYER"; gravity=Gravity.CENTER; setTextColor(Color.WHITE); background=GlassUtil.pill(t.accent); setPadding(0,20,0,20)
            layoutParams=LinearLayout.LayoutParams(0,-2,1f).apply{ setMargins(0,0,6,0)}
            setOnClickListener{ sendSms()}
        })
        val voiceBtn=TextView(this).apply{ text="🎙️"; gravity=Gravity.CENTER; setPadding(32,20,32,20); background=GlassUtil.card(100f); layoutParams=LinearLayout.LayoutParams(-2,-2).apply{ setMargins(6,0,0,0)}}
        voiceBtn.setOnClickListener{ if(!isRecording) startVoice() else stopVoice(); voiceBtn.text=if(isRecording) "⏹️" else "🎙️"}
        btnRow.addView(voiceBtn); inputCard.addView(btnRow); root.addView(inputCard)

        root.addView(TextView(this).apply{ text="MESSAGES"; textSize=11f; setTextColor(Color.parseColor("#9AA0C0")); setPadding(0,24,0,8)})
        val scroll=ScrollView(this); list=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL}; scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(-1,0,1f))

        setContentView(root)
        val perms=arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS, Manifest.permission.RECORD_AUDIO)
        if(perms.any{ ContextCompat.checkSelfPermission(this,it)!=PackageManager.PERMISSION_GRANTED}) ActivityCompat.requestPermissions(this, perms,2) else { loadContactsBar(); loadSms()}
    }

    override fun onRequestPermissionsResult(c:Int, p:Array<out String>, r:IntArray){ super.onRequestPermissionsResult(c,p,r); loadContactsBar(); loadSms()}

    private fun loadContactsBar(){
        contactsRow.removeAllViews()
        try{
            val cr=contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC")
            var cnt=0
            cr?.use{
                val nIdx=it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numIdx=it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while(it.moveToNext() && cnt<30){
                    val name=it.getString(nIdx)?:continue
                    val num=it.getString(numIdx)?:continue
                    val item=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER; setPadding(10,0,10,0); setOnClickListener{ numInput.setText(num); msgInput.requestFocus()}}
                    val avatar=TextView(this).apply{ layoutParams=LinearLayout.LayoutParams(108,108); gravity=Gravity.CENTER}
                    IconUtil.contactIcon(avatar, name)
                    item.addView(avatar)
                    item.addView(TextView(this).apply{ text=name.take(8); textSize=11f; setTextColor(Color.WHITE); gravity=Gravity.CENTER; setPadding(0,8,0,0)})
                    contactsRow.addView(item); cnt++
                }
            }
        }catch(_:Exception){}
    }

    private fun loadSms(){
        list.removeAllViews()
        try{
            val c:Cursor? = contentResolver.query(Telephony.Sms.CONTENT_URI, null, null, null, Telephony.Sms.DATE+" DESC")
            var cnt=0
            c?.use{
                val addr=it.getColumnIndex(Telephony.Sms.ADDRESS)
                val body=it.getColumnIndex(Telephony.Sms.BODY)
                val type=it.getColumnIndex(Telephony.Sms.TYPE)
                while(it.moveToNext() && cnt<80){
                    val a=it.getString(addr)?: "?"
                    val b=it.getString(body)?: ""
                    val isIncoming=it.getInt(type)==1
                    val row=LinearLayout(this).apply{
                        orientation=LinearLayout.VERTICAL
                        background=if(isIncoming) GlassUtil.card(20f, "#14FFFFFF", "#1AFFFFFF") else GlassUtil.card(20f, "#207C4DFF", "#307C4DFF")
                        setPadding(20,16,20,16); layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,0,0,10)}
                    }
                    row.addView(TextView(this).apply{ text="${if(isIncoming) "↙" else "↗"} $a"; textSize=11f; setTextColor(Color.parseColor("#9AA0C0"))})
                    row.addView(TextView(this).apply{ text=b; textSize=14f; setTextColor(Color.WHITE); setPadding(0,6,0,0)})
                    row.setOnClickListener{ numInput.setText(a)}
                    list.addView(row); cnt++
                }
                if(cnt==0) list.addView(TextView(this).apply{ text="Aucun SMS - Choisis un contact en haut pour envoyer"; setTextColor(Color.parseColor("#666")); gravity=Gravity.CENTER; setPadding(20,20,20,20)})
            }
        }catch(_:Exception){}
    }

    private fun sendSms(){ try{ android.telephony.SmsManager.getDefault().sendTextMessage(numInput.text.toString(), null, msgInput.text.toString(), null, null); Toast.makeText(this,"Envoyé",Toast.LENGTH_SHORT).show(); msgInput.text.clear(); loadSms()}catch(e:Exception){ Toast.makeText(this,e.message,Toast.LENGTH_LONG).show()}}
    private fun startVoice(){ try{ val f=File(filesDir, "voice_${System.currentTimeMillis()}.3gp"); recorder=MediaRecorder().apply{ setAudioSource(MediaRecorder.AudioSource.MIC); setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB); setOutputFile(f.absolutePath); prepare(); start()}; isRecording=true} catch(_:Exception){}}
    private fun stopVoice(){ try{ recorder?.stop(); recorder?.release(); recorder=null; isRecording=false; Toast.makeText(this,"Vocal sauvé",Toast.LENGTH_SHORT).show()}catch(_:Exception){}}
}
