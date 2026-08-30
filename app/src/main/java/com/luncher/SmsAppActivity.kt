
package com.luncher
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.telephony.SmsManager

class SmsAppActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try{
            setContentView(R.layout.activity_sms)
            val num = findViewById<EditText>(R.id.smsNumber)
            val msg = findViewById<EditText>(R.id.smsBody)
            findViewById<Button>(R.id.btnSendSms).setOnClickListener{
                try{
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)!=PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS), 2)
                        return@setOnClickListener
                    }
                    val smsManager = SmsManager.getDefault()
                    smsManager.sendTextMessage(num.text.toString(), null, msg.text.toString(), null, null)
                    Toast.makeText(this, "SMS envoye", Toast.LENGTH_SHORT).show()
                    msg.text.clear()
                }catch(e:Exception){
                    Toast.makeText(this, "Erreur: "+e.message, Toast.LENGTH_LONG).show()
                }
            }
        }catch(e:Exception){
            val tv = TextView(this)
            tv.text = "SMS erreur: "+e.message
            setContentView(tv)
        }
    }
}
