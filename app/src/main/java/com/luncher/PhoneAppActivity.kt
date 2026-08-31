
package com.luncher
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.content.Intent
import android.os.Bundle
import com.thibautfihey.luncher.ThemeSettingsActivity
import android.provider.CallLog
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PhoneAppActivity:AppCompatActivity(){
    private lateinit var numberInput:EditText
    override fun onCreate(s:Bundle?){super.onCreate(s); setContentView(R.layout.activity_phone)
 try { window.decorView.post { try { com.thibautfihey.luncher.attachThemeButton(window.decorView.rootView); findViewById<android.view.View>(android.R.id.content)?.setOnClickListener { startActivity(Intent(this, ThemeSettingsActivity::class.java)) } } catch(e:Exception){} } } catch(e:Exception){}; numberInput=findViewById(R.id.numberInput); setupDialer(); findViewById<View>(R.id.tabDial)?.setOnClickListener{switchMode("dial")}; findViewById<View>(R.id.tabRecent)?.setOnClickListener{switchMode("recent")}; findViewById<View>(R.id.tabContacts)?.setOnClickListener{switchMode("contacts")}; switchMode("dial")}
    private fun setupDialer(){val ids=mapOf(R.id.btn1 to "1",R.id.btn2 to "2",R.id.btn3 to "3",R.id.btn4 to "4",R.id.btn5 to "5",R.id.btn6 to "6",R.id.btn7 to "7",R.id.btn8 to "8",R.id.btn9 to "9",R.id.btnStar to "*",R.id.btn0 to "0",R.id.btnHash to "#"); for((id,dig) in ids){findViewById<TextView>(id)?.setOnClickListener{numberInput.append(dig)}}; findViewById<View>(R.id.btnDelete)?.setOnClickListener{val t=numberInput.text.toString(); if(t.isNotEmpty()) numberInput.setText(t.dropLast(1))}; findViewById<View>(R.id.btnCall)?.setOnClickListener{val num=numberInput.text.toString(); if(num.isBlank()) return@setOnClickListener; try{if(ContextCompat.checkSelfPermission(this,Manifest.permission.CALL_PHONE)!=PackageManager.PERMISSION_GRANTED){ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.CALL_PHONE),1); return@setOnClickListener}; startActivity(Intent(Intent.ACTION_CALL,Uri.parse("tel:"+num)))}catch(e:Exception){try{startActivity(Intent(Intent.ACTION_DIAL,Uri.parse("tel:"+num)))}catch(_:Exception){}}}}
    private fun switchMode(m:String){findViewById<View>(R.id.layoutDial)?.visibility=if(m=="dial") View.VISIBLE else View.GONE; findViewById<View>(R.id.recyclerPhone)?.visibility=if(m!="dial") View.VISIBLE else View.GONE; findViewById<TextView>(R.id.tabDial)?.setBackgroundResource(if(m=="dial") R.drawable.bg_glass_purple else R.drawable.bg_glass); findViewById<TextView>(R.id.tabRecent)?.setBackgroundResource(if(m=="recent") R.drawable.bg_glass_purple else R.drawable.bg_glass); findViewById<TextView>(R.id.tabContacts)?.setBackgroundResource(if(m=="contacts") R.drawable.bg_glass_purple else R.drawable.bg_glass); if(m=="recent") loadRecents() else if(m=="contacts") loadContacts()}

    fun getContactName(number:String):String?{
        try{
            val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))
            val cur = contentResolver.query(uri, arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME), null, null, null)
            cur?.use{ if(it.moveToFirst()){ return it.getString(0) } }
        }catch(_:Exception){}
        return null
    }

    private fun loadRecents(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_CALL_LOG)!=PackageManager.PERMISSION_GRANTED){ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.READ_CALL_LOG),2); return}
        val recycler=findViewById<RecyclerView>(R.id.recyclerPhone); recycler.layoutManager=LinearLayoutManager(this)
        try{
            // FIX LIMIT bug Android 11+
            val uri = CallLog.Calls.CONTENT_URI.buildUpon().appendQueryParameter("limit","50").build()
            val cur=contentResolver.query(uri,null,null,null,CallLog.Calls.DATE+" DESC")
            val list=mutableListOf<Pair<String,String>>()
            cur?.use{c-> while(c.moveToNext()){val num=c.getString(c.getColumnIndexOrThrow(CallLog.Calls.NUMBER)); val type=c.getInt(c.getColumnIndexOrThrow(CallLog.Calls.TYPE)); val name = getContactName(num)?: when(type){CallLog.Calls.INCOMING_TYPE->"Entrant"; CallLog.Calls.OUTGOING_TYPE->"Sortant"; else->"Manqué"}; list.add(name to num)}}
            recycler.adapter=ContactAdapter(list.distinctBy{it.second}){num-> numberInput.setText(num); switchMode("dial")}
        }catch(e:Exception){Toast.makeText(this,"Recents: "+e.message,Toast.LENGTH_SHORT).show()}
    }
    private fun loadContacts(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_CONTACTS)!=PackageManager.PERMISSION_GRANTED){ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.READ_CONTACTS),3); return}
        val recycler=findViewById<RecyclerView>(R.id.recyclerPhone); recycler.layoutManager=LinearLayoutManager(this)
        try{
            val cur=contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC")
            val map=LinkedHashMap<String,String>()
            cur?.use{c-> while(c.moveToNext()){val n=c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)); val num=c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)); val norm = num.replace(" ","").replace("-",""); if(!map.containsKey(norm)) map[norm]=n+";"+num}}
            val list = map.values.map{ val parts=it.split(";"); parts[0] to parts[1] }
            recycler.adapter=ContactAdapter(list){num-> numberInput.setText(num); switchMode("dial")}
        }catch(e:Exception){Toast.makeText(this,"Contacts: "+e.message,Toast.LENGTH_SHORT).show()}
    }
}
class SimpleAdapter(private val items:List<String>,private val onClick:(String)->Unit):RecyclerView.Adapter<SimpleAdapter.H>(){class H(v:View):RecyclerView.ViewHolder(v){val t:TextView=v.findViewById(R.id.itemText)} override fun onCreateViewHolder(p:ViewGroup,t:Int):H{val v=LayoutInflater.from(p.context).inflate(R.layout.item_simple,p,false); return H(v)} override fun getItemCount()=items.size; override fun onBindViewHolder(h:H,pos:Int){h.t.text=items[pos]; h.itemView.setOnClickListener{onClick(items[pos])}}}
class ContactAdapter(private val items:List<Pair<String,String>>,private val onClick:(String)->Unit):RecyclerView.Adapter<ContactAdapter.H>(){class H(v:View):RecyclerView.ViewHolder(v){val name:TextView=v.findViewById(R.id.contactName); val num:TextView=v.findViewById(R.id.contactNumber)} override fun onCreateViewHolder(p:ViewGroup,t:Int):H{val v=LayoutInflater.from(p.context).inflate(R.layout.item_contact,p,false); return H(v)} override fun getItemCount()=items.size; override fun onBindViewHolder(h:H,pos:Int){h.name.text=items[pos].first; h.num.text=items[pos].second; h.itemView.setOnClickListener{onClick(items[pos].second)}}}