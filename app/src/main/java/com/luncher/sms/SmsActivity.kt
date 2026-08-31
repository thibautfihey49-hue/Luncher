package com.luncher.sms

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.media.MediaRecorder
import android.os.Bundle
import android.provider.Telephony
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.luncher.util.GlassUtil
import java.io.File

class SmsActivity: AppCompatActivity(){
    private var recorder:MediaRecorder?=null
    private var isRecording=false
    private lateinit var list:LinearLayout

    override fun onCreate(b:Bundle?){
        super.onCreate(b)
        val t=GlassUtil.get(this)
        val root=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; background=GlassUtil.bg(t); setPadding(20,80,20,20)}

        root.addView(TextView(this).apply{ text="Messages + Vocal"; textSize=24f})

        val inputRow=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL}
        val numInput=EditText(this).apply{ hint="Numéro"; layoutParams=LinearLayout.LayoutParams(0,-2,1f)}
        val msgInput=EditText(this).apply{ hint="Message"; layoutParams=LinearLayout.LayoutParams(0,-2,2f)}
        inputRow.addView(numInput); inputRow.addView(msgInput)
        root.addView(inputRow)

        val sendRow=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL}
        sendRow.addView(Button(this).apply{ text="Envoyer SMS"; setOnClickListener{
            try{ val sms=android.telephony.SmsManager.getDefault(); sms.sendTextMessage(numInput.text.toString(), null, msgInput.text.toString(), null, null); Toast.makeText(this@SmsActivity,"SMS envoyé",Toast.LENGTH_SHORT).show()}catch(e:Exception){ Toast.makeText(this@SmsActivity,e.message,Toast.LENGTH_LONG).show()}
        }})
        val voiceBtn=Button(this).apply{ text="🎙️ Vocal"}
        voiceBtn.setOnClickListener{
            if(!isRecording) startVoice() else stopVoice()
            voiceBtn.text=if(isRecording) "⏹️ Stop" else "🎙️ Vocal"
        }
        sendRow.addView(voiceBtn)
        root.addView(sendRow)

        val scroll=ScrollView(this); list=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL}; scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(-1,0,1f))

        setContentView(root)

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS, Manifest.permission.RECORD_AUDIO),2)
        } else loadSms()
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
                    val row=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; setPadding(16,12,16,12); background=GlassUtil.glassCard(16f); layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,6,0,6)}}
                    row.addView(TextView(this).apply{ text="$t - $a"; typeface=android.graphics.Typeface.DEFAULT_BOLD})
                    row.addView(TextView(this).apply{ text=b})
                    list.addView(row); cnt++
                }
            }
        }catch(e:Exception){ list.addView(TextView(this).apply{ text="SMS err ${e.message}"})}
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
            isRecording=true; Toast.makeText(this,"Enregistrement...",Toast.LENGTH_SHORT).show()
        }catch(e:Exception){ Toast.makeText(this,"Mic err ${e.message}",Toast.LENGTH_LONG).show()}
    }

    private fun stopVoice(){
        try{ recorder?.stop(); recorder?.release(); recorder=null; isRecording=false; Toast.makeText(this,"Vocal sauvé dans files/",Toast.LENGTH_SHORT).show()}catch(_:Exception){}
    }
}
