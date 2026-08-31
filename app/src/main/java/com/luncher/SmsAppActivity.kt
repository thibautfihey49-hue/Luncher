package com.luncher

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.Telephony
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.luncher.util.ThemeUtil

class SmsAppActivity: AppCompatActivity(){
    override fun onCreate(b:Bundle?){
        super.onCreate(b)
        val t=ThemeUtil.get(this)
        val root=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; background=ThemeUtil.drawable(t); setPadding(30,70,30,20)}
        root.addView(TextView(this).apply{ text="SMS"; textSize=28f; setTextColor(android.graphics.Color.parseColor(t.textColor))})

        val list=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL}
        val scroll=ScrollView(this); scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(-1,0,1f))
        setContentView(root)

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_SMS),2)
            return
        }

        try{
            val cr:Cursor? = contentResolver.query(Telephony.Sms.Inbox.CONTENT_URI, null, null, null, Telephony.Sms.DATE+" DESC")
            cr?.use{
                val addrIdx=it.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIdx=it.getColumnIndex(Telephony.Sms.BODY)
                val dateIdx=it.getColumnIndex(Telephony.Sms.DATE)
                var count=0
                while(it.moveToNext() && count<100){
                    val addr=it.getString(addrIdx)?: "Inconnu"
                    val body=it.getString(bodyIdx)?: ""
                    val row=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; setPadding(20,20,20,20)}
                    row.addView(TextView(this).apply{ text=addr; textSize=16f; typeface=android.graphics.Typeface.DEFAULT_BOLD})
                    row.addView(TextView(this).apply{ text=body.take(100); textSize=14f})
                    list.addView(row); count++
                }
            }
        }catch(e:Exception){ list.addView(TextView(this).apply{ text="Erreur SMS: ${e.message}"})}
    }
}
