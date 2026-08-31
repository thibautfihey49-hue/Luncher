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

class PhoneActivity: AppCompatActivity(){
    private lateinit var contactsList:LinearLayout
    private lateinit var historyList:LinearLayout
    private lateinit var dialInput:EditText
    private var showingContacts=true

    override fun onCreate(b:Bundle?){
        super.onCreate(b)
        val t=GlassUtil.get(this)
        val root=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; background=GlassUtil.bg(t); setPadding(24,80,24,20)}

        dialInput=EditText(this).apply{
            hint="Numéro..."; textSize=28f; gravity=Gravity.CENTER; setTextColor(Color.parseColor(t.text))
            background=GlassUtil.card(20f); setPadding(20,24,20,24)
            layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,0,0,16)}
        }
        root.addView(dialInput)

        val dialPad=GridLayout(this).apply{ columnCount=3; rowCount=4; useDefaultMargins=true}
        listOf("1","2","3","4","5","6","7","8","9","*","0","#").forEach{ d ->
            dialPad.addView(Button(this).apply{
                text=d; textSize=20f; setBackgroundColor(Color.parseColor("#1AFFFFFF")); setTextColor(Color.WHITE)
                layoutParams=GridLayout.LayoutParams().apply{ width=0; columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f); setMargins(8,8,8,8)}
                setOnClickListener{ dialInput.append(d)}
            })
        }
        root.addView(dialPad)

        root.addView(Button(this).apply{
            text="📞 APPELER"; background=GlassUtil.pill(t.accent); setTextColor(Color.WHITE)
            setOnClickListener{ call(dialInput.text.toString())}
        })

        val tabs=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL; setPadding(0,20,0,0)}
        val btnContacts=Button(this).apply{ text="CONTACTS"; layoutParams=LinearLayout.LayoutParams(0,-2,1f).apply{ setMargins(0,0,8,0)}}
        val btnHistory=Button(this).apply{ text="HISTORIQUE"; layoutParams=LinearLayout.LayoutParams(0,-2,1f).apply{ setMargins(8,0,0,0)}}
        tabs.addView(btnContacts); tabs.addView(btnHistory)
        root.addView(tabs)

        val searchContact=EditText(this).apply{ hint="Rechercher contact..."; setPadding(24,16,24,16); background=GlassUtil.card(16f); layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,16,0,0)}}
        root.addView(searchContact)

        val scroll=ScrollView(this)
        val container=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL}
        contactsList=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL}
        historyList=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; visibility=android.view.View.GONE}
        container.addView(contactsList); container.addView(historyList)
        scroll.addView(container)
        root.addView(scroll, LinearLayout.LayoutParams(-1,0,1f))

        btnContacts.setOnClickListener{ showingContacts=true; contactsList.visibility=android.view.View.VISIBLE; historyList.visibility=android.view.View.GONE}
        btnHistory.setOnClickListener{ showingContacts=false; contactsList.visibility=android.view.View.GONE; historyList.visibility=android.view.View.VISIBLE; loadHistory()}

        searchContact.addTextChangedListener(object:android.text.TextWatcher{
            override fun afterTextChanged(s:android.text.Editable?){ if(showingContacts) loadContacts(s.toString())}
            override fun beforeTextChanged(a:CharSequence?,b:Int,c:Int,d:Int){}
            override fun onTextChanged(a:CharSequence?,b:Int,c:Int,d:Int){}
        })

        setContentView(root)
        checkPerms()
    }

    private fun checkPerms(){
        val perms=arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.READ_CALL_LOG, Manifest.permission.CALL_PHONE)
        if(perms.any{ ContextCompat.checkSelfPermission(this,it)!=PackageManager.PERMISSION_GRANTED}){
            ActivityCompat.requestPermissions(this, perms, 1)
        } else { loadContacts(); loadHistory() }
    }

    override fun onRequestPermissionsResult(c:Int, p:Array<out String>, r:IntArray){
        super.onRequestPermissionsResult(c,p,r)
        if(r.isNotEmpty() && r[0]==PackageManager.PERMISSION_GRANTED){ loadContacts(); loadHistory() }
    }

    private fun call(num:String){ if(num.isBlank()) return; try{ startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$num")))}catch(_:Exception){ startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$num")))} }

    private fun loadContacts(filter:String=""){
        contactsList.removeAllViews()
        try{
            val cr:Cursor? = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC")
            var count=0
            cr?.use{
                val nIdx=it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numIdx=it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while(it.moveToNext() && count<300){
                    val name=it.getString(nIdx)?:continue
                    val num=it.getString(numIdx)?:continue
                    if(filter.isNotEmpty() &&!name.contains(filter,true) &&!num.contains(filter)) continue
                    val row=LinearLayout(this).apply{
                        orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL
                        background=GlassUtil.cardSolid(); setPadding(20,18,20,18)
                        layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,0,0,10)}
                    }
                    row.addView(TextView(this).apply{
                        text="$name\n$num"; textSize=15f; setTextColor(Color.WHITE)
                        typeface=android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
                        layoutParams=LinearLayout.LayoutParams(0,-2,1f)
                    })
                    row.addView(Button(this).apply{ text="📞"; setOnClickListener{ call(num)}})
                    contactsList.addView(row); count++
                }
            }
            if(count==0) contactsList.addView(TextView(this).apply{ text="Aucun contact trouvé (vérifie permissions)"; setTextColor(Color.GRAY); setPadding(20,20,20,20)})
        }catch(e:Exception){ contactsList.addView(TextView(this).apply{ text="Erreur: ${e.message}"})}
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
                    val num=it.getString(numIdx)?: "Inconnu"
                    val name=it.getString(nameIdx)?: num
                    val type=it.getInt(typeIdx)
                    val typeStr=when(type){ CallLog.Calls.INCOMING_TYPE->"↙ Entrant"; CallLog.Calls.OUTGOING_TYPE->"↗ Sortant"; CallLog.Calls.MISSED_TYPE->"✕ Manqué"; else->"•"}
                    val row=LinearLayout(this).apply{
                        orientation=LinearLayout.HORIZONTAL; background=GlassUtil.cardSolid(); setPadding(20,16,20,16)
                        layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,0,0,8)}
                    }
                    row.addView(TextView(this).apply{ text="$typeStr $name\n$num"; setTextColor(Color.WHITE); layoutParams=LinearLayout.LayoutParams(0,-2,1f)})
                    row.addView(Button(this).apply{ text="📞"; setOnClickListener{ call(num)}})
                    historyList.addView(row); cnt++
                }
            }
        }catch(e:Exception){ historyList.addView(TextView(this).apply{ text="History: ${e.message}"})}
    }
}
