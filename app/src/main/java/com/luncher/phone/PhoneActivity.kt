package com.luncher.phone

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.CallLog
import android.provider.ContactsContract
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.luncher.util.GlassUtil
import com.luncher.util.IconUtil

class PhoneActivity: AppCompatActivity(){
    private lateinit var contactsList:LinearLayout
    private lateinit var historyList:LinearLayout
    private lateinit var dialInput:EditText
    private var showingContacts=true

    override fun onCreate(b:Bundle?){
        super.onCreate(b)
        val t=GlassUtil.get(this)
        val root=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; background=GlassUtil.bg(t); setPadding(28,80,28,20)}

        root.addView(TextView(this).apply{ text="Phone"; textSize=32f; setTextColor(Color.WHITE); typeface=android.graphics.Typeface.DEFAULT_BOLD})

        dialInput=EditText(this).apply{
            hint="Numéro..."; textSize=28f; gravity=Gravity.CENTER; setTextColor(Color.WHITE)
            background=GlassUtil.card(20f); setPadding(20,28,20,28)
            layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,20,0,16)}
        }
        root.addView(dialInput)

        val dialPad=GridLayout(this).apply{ columnCount=3}
        listOf("1","2","3","4","5","6","7","8","9","*","0","#").forEach{ d ->
            dialPad.addView(LinearLayout(this).apply{
                background=GlassUtil.card(20f); setPadding(0,26,0,26); gravity=Gravity.CENTER
                layoutParams=GridLayout.LayoutParams().apply{ width=0; columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f); setMargins(8,8,8,8)}
                setOnClickListener{ dialInput.append(d)}
                addView(TextView(this@PhoneActivity).apply{ text=d; textSize=22f; setTextColor(Color.WHITE)})
            })
        }
        root.addView(dialPad)

        root.addView(TextView(this).apply{
            text="📞 APPELER"; gravity=Gravity.CENTER; setTextColor(Color.WHITE); background=GlassUtil.pill(t.accent); setPadding(0,22,0,22)
            layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,16,0,20)}
            setOnClickListener{ call(dialInput.text.toString())}
        })

        val tabs=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL}
        val btnC=TextView(this).apply{ text="CONTACTS"; gravity=Gravity.CENTER; background=GlassUtil.pill(t.accent); setTextColor(Color.WHITE); setPadding(0,16,0,16); layoutParams=LinearLayout.LayoutParams(0,-2,1f).apply{ setMargins(0,0,6,0)}}
        val btnH=TextView(this).apply{ text="HISTORIQUE"; gravity=Gravity.CENTER; background=GlassUtil.card(100f); setTextColor(Color.WHITE); setPadding(0,16,0,16); layoutParams=LinearLayout.LayoutParams(0,-2,1f).apply{ setMargins(6,0,0,0)}}
        tabs.addView(btnC); tabs.addView(btnH); root.addView(tabs)

        val search=EditText(this).apply{ hint="Rechercher..."; background=GlassUtil.card(100f); setPadding(28,16,28,16); setTextColor(Color.WHITE); layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,16,0,0)}}
        root.addView(search)

        val scroll=ScrollView(this)
        val container=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; setPadding(0,16,0,0)}
        contactsList=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL}
        historyList=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; visibility=android.view.View.GONE}
        container.addView(contactsList); container.addView(historyList); scroll.addView(container)
        root.addView(scroll, LinearLayout.LayoutParams(-1,0,1f))

        btnC.setOnClickListener{ showingContacts=true; contactsList.visibility=android.view.View.VISIBLE; historyList.visibility=android.view.View.GONE; btnC.background=GlassUtil.pill(t.accent); btnH.background=GlassUtil.card(100f)}
        btnH.setOnClickListener{ showingContacts=false; contactsList.visibility=android.view.View.GONE; historyList.visibility=android.view.View.VISIBLE; btnH.background=GlassUtil.pill(t.accent); btnC.background=GlassUtil.card(100f); loadHistory()}
        search.addTextChangedListener(object:android.text.TextWatcher{
            override fun afterTextChanged(s:android.text.Editable?){ if(showingContacts) loadContacts(s.toString())}
            override fun beforeTextChanged(a:CharSequence?,b:Int,c:Int,d:Int){}
            override fun onTextChanged(a:CharSequence?,b:Int,c:Int,d:Int){}
        })

        setContentView(root)
        if(listOf(Manifest.permission.READ_CONTACTS, Manifest.permission.READ_CALL_LOG, Manifest.permission.CALL_PHONE).any{ ContextCompat.checkSelfPermission(this,it)!=PackageManager.PERMISSION_GRANTED}) ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.READ_CALL_LOG, Manifest.permission.CALL_PHONE),1) else { loadContacts(); loadHistory()}
    }

    override fun onRequestPermissionsResult(c:Int, p:Array<out String>, r:IntArray){ super.onRequestPermissionsResult(c,p,r); if(r.isNotEmpty() && r[0]==0){ loadContacts(); loadHistory()}}

    private fun call(num:String){ if(num.isBlank()) return; try{ startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$num")))}catch(_:Exception){ startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$num")))} }

    private fun loadContacts(filter:String=""){
        contactsList.removeAllViews()
        try{
            val cr=contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC")
            var cnt=0
            cr?.use{
                val nIdx=it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numIdx=it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while(it.moveToNext() && cnt<500){
                    val name=it.getString(nIdx)?:continue
                    val num=it.getString(numIdx)?:continue
                    if(filter.isNotEmpty() &&!name.contains(filter,true) &&!num.contains(filter)) continue
                    val row=LinearLayout(this).apply{
                        orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL
                        background=GlassUtil.cardSolid(); setPadding(16,16,16,16)
                        layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,0,0,10)}
                    }
                    val avatar=TextView(this).apply{ layoutParams=LinearLayout.LayoutParams(96,96).apply{ setMargins(0,0,16,0)}; gravity=Gravity.CENTER}
                    IconUtil.contactIcon(avatar, name)
                    row.addView(avatar)
                    row.addView(LinearLayout(this).apply{
                        orientation=LinearLayout.VERTICAL; layoutParams=LinearLayout.LayoutParams(0,-2,1f)
                        addView(TextView(this@PhoneActivity).apply{ text=name; setTextColor(Color.WHITE); typeface=android.graphics.Typeface.create("sans-serif-medium",0)})
                        addView(TextView(this@PhoneActivity).apply{ text=num; textSize=12f; setTextColor(Color.parseColor("#9AA0C0"))})
                    })
                    row.addView(TextView(this).apply{ text="📞"; setPadding(20,20,20,20); background=GlassUtil.card(100f); setOnClickListener{ call(num)}})
                    contactsList.addView(row); cnt++
                }
            }
        }catch(_:Exception){}
    }

    private fun loadHistory(){
        historyList.removeAllViews()
        try{
            val c=contentResolver.query(CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE+" DESC")
            c?.use{
                val numIdx=it.getColumnIndex(CallLog.Calls.NUMBER)
                val nameIdx=it.getColumnIndex(CallLog.Calls.CACHED_NAME)
                val typeIdx=it.getColumnIndex(CallLog.Calls.TYPE)
                var cnt=0
                while(it.moveToNext() && cnt<100){
                    val num=it.getString(numIdx)?: "?"
                    val name=it.getString(nameIdx)?: num
                    val type=it.getInt(typeIdx)
                    val (ic,col)=when(type){ CallLog.Calls.INCOMING_TYPE->"↙" to "#4CAF50"; CallLog.Calls.OUTGOING_TYPE->"↗" to "#2196F3"; CallLog.Calls.MISSED_TYPE->"✕" to "#F44336"; else->"•" to "#888"}
                    val row=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL; background=GlassUtil.cardSolid(); setPadding(20,16,20,16); layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,0,0,8)}}
                    row.addView(TextView(this).apply{ text=ic; setTextColor(GlassUtil.safeColor(col)); gravity=Gravity.CENTER; background=GlassUtil.card(100f); layoutParams=LinearLayout.LayoutParams(72,72).apply{ setMargins(0,0,12,0)}})
                    row.addView(LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; layoutParams=LinearLayout.LayoutParams(0,-2,1f); addView(TextView(this@PhoneActivity).apply{ text=name; setTextColor(Color.WHITE)}); addView(TextView(this@PhoneActivity).apply{ text=num; textSize=12f; setTextColor(Color.parseColor("#9AA0C0"))})})
                    row.addView(TextView(this).apply{ text="📞"; setOnClickListener{ call(num)}})
                    historyList.addView(row); cnt++
                }
            }
        }catch(_:Exception){}
    }
}
