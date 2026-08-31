package com.luncher.sms

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.media.MediaRecorder
import android.os.Bundle
import android.provider.Telephony
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.luncher.util.GlassUtil
import java.io.File

class SmsActivity: AppCompatActivity(){
    private var recorder:MediaRecorder?=null
    private var isRecording=false
    private lateinit var list:LinearLayout

    override fun onCreate(b:Bundle?){
        super.onCreate(b)
        val t=GlassUtil.get(this)
        val root=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; background=GlassUtil.bg(t); setPadding(24,80,24,20)}

        root.addView(TextView(this).apply{ text="Messages"; textSize=28f; setTextColor(Color.parseColor(t.text)); typeface=android.graphics.Typeface.DEFAULT_BOLD})

        val inputRow=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL; setPadding(0,16,0,0)}
        val numInput=EditText(this).apply{ hint="Numéro"; background=GlassUtil.card(16f); setPadding(20,16,20,16); layoutParams=LinearLayout.LayoutParams(0,-2,1f).apply{ setMargins(0,0,8,0)}}
        val msgInput=EditText(this).apply{ hint="Message"; background=GlassUtil.card(16f); setPadding(20,16,20,16); layoutParams=LinearLayout.LayoutParams(0,-2,2f)}
        inputRow.addView(numInput); inputRow.addView(msgInput)
        root.addView(inputRow)

        val sendRow=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL; setPadding(0,12,0,0)}
        sendRow.addView(Button(this).apply{
            text="Envoyer SMS"; background=GlassUtil.pill(t.accent); setTextColor(Color.WHITE)
            setOnClickListener{
                try{ val sms=android.telephony.SmsManager.getDefault(); sms.sendTextMessage(numInput.text.toString(), null, msgInput.text.toString(), null, null); Toast.makeText(this@SmsActivity,"Envoyé",Toast.LENGTH_SHORT).show()}catch(e:Exception){ Toast.makeText(this@SmsActivity,e.message,Toast.LENGTH_LONG).show()}
            }
        })
        val voiceBtn=Button(this).apply{ text="🎙️ Vocal"; layoutParams=LinearLayout.LayoutParams(-2,-2).apply{ setMargins(12,0,0,0)}}
        voiceBtn.setOnClickListener{
            if(!isRecording) startVoice() else stopVoice()
            voiceBtn.text=if(isRecording) "⏹️ Stop" else "🎙️ Vocal"
        }
        sendRow.addView(voiceBtn)
        root.addView(sendRow)

        val scroll=ScrollView(this); list=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; setPadding(0,20,0,0)}; scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(-1,0,1f))

        setContentView(root)
        checkPerms()
    }

    private fun checkPerms(){
        val perms=arrayOf(Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS, Manifest.permission.RECORD_AUDIO)
        if(perms.any{ ContextCompat.checkSelfPermission(this,it)!=PackageManager.PERMISSION_GRANTED}){
            ActivityCompat.requestPermissions(this, perms, 2)
        } else loadSms()
    }

    override fun onRequestPermissionsResult(c:Int, p:Array<out String>, r:IntArray){
        super.onRequestPermissionsResult(c,p,r)
        if(r.isNotEmpty() && r[0]==PackageManager.PERMISSION_GRANTED) loadSms()
    }

    private fun loadSms(){
        list.removeAllViews()
        try{
            val c:Cursor? = contentResolver.query(Telephony.Sms.CONTENT_URI, null, null, null, Telephony.Sms.DATE+" DESC")
            c?.use{
                val addr=it.getColumnIndex(Telephony.Sms.ADDRESS)
                val body=it.getColumnIndex(Telephony.Sms.BODY)
                val type=it.getColumnIndex(Telephony.Sms.TYPE)
                var cnt=0
                while(it.moveToNext() && cnt<150){
                    val a=it.getString(addr)?: "?"
                    val b=it.getString(body)?: ""
                    val t=if(it.getInt(type)==1) "Reçu" else "Envoyé"
                    val row=LinearLayout(this).apply{
                        orientation=LinearLayout.VERTICAL; setPadding(20,16,20,16)
                        background=GlassUtil.cardSolid()
                        layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,0,0,10)}
                    }
                    row.addView(TextView(this).apply{ text="$t - $a"; setTextColor(Color.WHITE); typeface=android.graphics.Typeface.DEFAULT_BOLD})
                    row.addView(TextView(this).apply{ text=b; setTextColor(Color.parseColor("#CCCCCC"))})
                    list.addView(row); cnt++
                }
                if(cnt==0) list.addView(TextView(this).apply{ text="Aucun SMS"; setTextColor(Color.GRAY)})
            }
        }catch(e:Exception){ list.addView(TextView(this).apply{ text="Err: ${e.message}"})}
    }

    private fun startVoice(){
        try{
            val file=File(filesDir, "voice_${System.currentTimeMillis()}.3gp")
            recorder=MediaRecorder().apply{
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(file.absolutePath)
                prepare(); start()
            }
            isRecording=true; Toast.makeText(this,"Rec...",Toast.LENGTH_SHORT).show()
        }catch(e:Exception){ Toast.makeText(this,e.message,Toast.LENGTH_LONG).show()}
    }
    private fun stopVoice(){ try{ recorder?.stop(); recorder?.release(); recorder=null; isRecording=false; Toast.makeText(this,"Vocal sauvé",Toast.LENGTH_SHORT).show()}catch(_:Exception){}}
}
