package com.luncher

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.luncher.util.ThemeUtil

class PhoneAppActivity: AppCompatActivity(){
    private lateinit var list:LinearLayout
    override fun onCreate(b:Bundle?){
        super.onCreate(b)
        val t=ThemeUtil.get(this)
        val root=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; background=ThemeUtil.drawable(t); setPadding(30,70,30,20)}

        root.addView(LinearLayout(this).apply{
            orientation=LinearLayout.HORIZONTAL
            addView(TextView(this@PhoneAppActivity).apply{ text="Phone"; textSize=28f; setTextColor(android.graphics.Color.parseColor(t.textColor)); layoutParams=LinearLayout.LayoutParams(0,-2,1f)})
            addView(Button(this@PhoneAppActivity).apply{ text="Clavier"; setOnClickListener{ startActivity(Intent(Intent.ACTION_DIAL))}})
        })

        val search=EditText(this).apply{ hint="Rechercher contact..."; setPadding(30,20,30,20)}
        root.addView(search)

        val scroll=ScrollView(this); list=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL}; scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(-1,0,1f))

        setContentView(root)

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_CONTACTS), 1)
        } else loadContacts("")

        search.addTextChangedListener(object:android.text.TextWatcher{
            override fun afterTextChanged(s:android.text.Editable?){ loadContacts(s.toString())}
            override fun beforeTextChanged(a:CharSequence?,b:Int,c:Int,d:Int){}
            override fun onTextChanged(a:CharSequence?,b:Int,c:Int,d:Int){}
        })
    }

    private fun loadContacts(filter:String){
        list.removeAllViews()
        try{
            val cr:Cursor? = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC")
            cr?.use{
                val nameIdx=it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numIdx=it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while(it.moveToNext()){
                    val name=it.getString(nameIdx)?:continue
                    val num=it.getString(numIdx)?:continue
                    if(filter.isNotEmpty() &&!name.contains(filter,true) &&!num.contains(filter)) continue
                    val row=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL; setPadding(20,20,20,20); gravity=Gravity.CENTER_VERTICAL}
                    row.addView(TextView(this).apply{ text="$name\n$num"; textSize=16f; layoutParams=LinearLayout.LayoutParams(0,-2,1f)})
                    row.addView(Button(this).apply{ text="Appeler"; setOnClickListener{
                        try{ startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$num")))}catch(_:Exception){ startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$num")))}
                    }})
                    list.addView(row)
                }
            }
        }catch(e:Exception){ list.addView(TextView(this).apply{ text="Erreur contacts: ${e.message}"})}
    }
}
