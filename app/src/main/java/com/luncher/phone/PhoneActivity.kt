package com.luncher.phone

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.CallLog
import android.provider.ContactsContract
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.luncher.util.GlassUtil

class PhoneActivity: AppCompatActivity(){
    private lateinit var contactsList:LinearLayout
    private lateinit var historyList:LinearLayout
    private lateinit var dialInput:EditText

    override fun onCreate(b:Bundle?){
        super.onCreate(b)
        val t=GlassUtil.get(this)
        val root=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; background=GlassUtil.bg(t); setPadding(20,80,20,20)}

        // Dialer
        dialInput=EditText(this).apply{ hint="Numéro..."; textSize=24f; gravity=Gravity.CENTER; setPadding(20,20,20,20)}
        root.addView(dialInput)

        val dialPad=GridLayout(this).apply{ columnCount=3; rowCount=4}
        listOf("1","2","3","4","5","6","7","8","9","*","0","#").forEach{ d ->
            dialPad.addView(Button(this).apply{ text=d; setOnClickListener{ dialInput.append(d)}})
        }
        root.addView(dialPad)

        val callBtn=Button(this).apply{ text="📞 Appeler"; setOnClickListener{ call(dialInput.text.toString())}}
        root.addView(callBtn)

        // Tabs
        val tabs=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL}
        val btnContacts=Button(this).apply{ text="Contacts"}
        val btnHistory=Button(this).apply{ text="Historique"}
        tabs.addView(btnContacts); tabs.addView(btnHistory)
        root.addView(tabs)

        val scroll=ScrollView(this)
        val container=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL}
        contactsList=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL}
        historyList=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; visibility=android.view.View.GONE}
        container.addView(contactsList); container.addView(historyList)
        scroll.addView(container)
        root.addView(scroll, LinearLayout.LayoutParams(-1,0,1f))

        btnContacts.setOnClickListener{ contactsList.visibility=android.view.View.VISIBLE; historyList.visibility=android.view.View.GONE}
        btnHistory.setOnClickListener{ contactsList.visibility=android.view.View.GONE; historyList.visibility=android.view.View.VISIBLE; loadHistory()}

        setContentView(root)

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.READ_CALL_LOG, Manifest.permission.CALL_PHONE),1)
        } else { loadContacts(); loadHistory() }
    }

    private fun call(num:String){
        if(num.isBlank()) return
        try{ startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$num")))}catch(_:Exception){ startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$num")))}
    }

    private fun loadContacts(){
        contactsList.removeAllViews()
        try{
            val c:Cursor? = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC")
            c?.use{
                val nIdx=it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numIdx=it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val photoIdx=it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
                while(it.moveToNext()){
                    val name=it.getString(nIdx)?:continue
                    val num=it.getString(numIdx)?:continue
                    val row=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL; setPadding(16,16,16,16); background=GlassUtil.glassCard(16f); setOnClickListener{ dialInput.setText(num)}}
                    row.addView(TextView(this).apply{ text="$name\n$num"; layoutParams=LinearLayout.LayoutParams(0,-2,1f)})
                    row.addView(Button(this).apply{ text="📞"; setOnClickListener{ call(num)}})
                    contactsList.addView(row)
                }
            }
        }catch(e:Exception){ contactsList.addView(TextView(this).apply{ text=e.message})}
    }

    private fun loadHistory(){
        historyList.removeAllViews()
        try{
            val c=contentResolver.query(CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE+" DESC")
            c?.use{
                val numIdx=it.getColumnIndex(CallLog.Calls.NUMBER)
                val typeIdx=it.getColumnIndex(CallLog.Calls.TYPE)
                val dateIdx=it.getColumnIndex(CallLog.Calls.DATE)
                var cnt=0
                while(it.moveToNext() && cnt<100){
                    val num=it.getString(numIdx)?: "Inconnu"
                    val type=it.getInt(typeIdx)
                    val typeStr=when(type){ CallLog.Calls.INCOMING_TYPE->"Entrant"; CallLog.Calls.OUTGOING_TYPE->"Sortant"; CallLog.Calls.MISSED_TYPE->"Manqué"; else->"Autre"}
                    val row=TextView(this).apply{ text="$typeStr : $num"; setPadding(20,16,20,16)}
                    historyList.addView(row); cnt++
                }
            }
        }catch(e:Exception){ historyList.addView(TextView(this).apply{ text="History err ${e.message}"})}
    }
}
