
package com.luncher
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.content.Intent
import com.thibautfihey.luncher.ThemeSettingsActivity
import android.provider.ContactsContract
import android.provider.Telephony
import android.telephony.SmsManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SmsAppActivity:AppCompatActivity(){
    override fun onCreate(s:Bundle?){super.onCreate(s); setContentView(R.layout.activity_sms)
 try { window.decorView.post { try { com.thibautfihey.luncher.attachThemeButton(window.decorView.rootView); findViewById<android.view.View>(android.R.id.content)?.setOnClickListener { startActivity(Intent(this, ThemeSettingsActivity::class.java)) } } catch(e:Exception){} } } catch(e:Exception){}; findViewById<View>(R.id.tabConversations)?.setOnClickListener{switchMode("list")}; findViewById<View>(R.id.tabNew)?.setOnClickListener{switchMode("new")}; findViewById<Button>(R.id.btnSendSms)?.setOnClickListener{sendSms()}; switchMode("list")}
    private fun switchMode(m:String){findViewById<View>(R.id.layoutList)?.visibility=if(m=="list") View.VISIBLE else View.GONE; findViewById<View>(R.id.layoutNew)?.visibility=if(m=="new") View.VISIBLE else View.GONE; findViewById<TextView>(R.id.tabConversations)?.setBackgroundResource(if(m=="list") R.drawable.bg_glass_purple else R.drawable.bg_glass); findViewById<TextView>(R.id.tabNew)?.setBackgroundResource(if(m=="new") R.drawable.bg_glass_purple else R.drawable.bg_glass); if(m=="list") loadConv()}

    fun getContactName(number:String):String{
        try{
            val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))
            val cur = contentResolver.query(uri, arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME), null, null, null)
            cur?.use{ if(it.moveToFirst()){ return it.getString(0) } }
        }catch(_:Exception){}
        return number
    }

    private fun loadConv(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_SMS)!=PackageManager.PERMISSION_GRANTED){ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.READ_SMS,Manifest.permission.READ_CONTACTS),4); return}
        val recycler=findViewById<RecyclerView>(R.id.recyclerSms); recycler.layoutManager=LinearLayoutManager(this)
        try{
            // Groupe par numero - 1 par conversation
            val cur=contentResolver.query(Telephony.Sms.CONTENT_URI,null,null,null,Telephony.Sms.DATE+" DESC")
            val map = LinkedHashMap<String, Pair<String,String>>()
            cur?.use{c-> while(c.moveToNext()){val addr=c.getString(c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))?: continue; val body=c.getString(c.getColumnIndexOrThrow(Telephony.Sms.BODY))?:""; if(!map.containsKey(addr)){val name=getContactName(addr); map[addr]=name to body}}}
            val list = map.entries.map{Triple(it.key, it.value.first, it.value.second)}
            recycler.adapter=ConvAdapter(list){addr-> findViewById<EditText>(R.id.smsNumber)?.setText(addr); switchMode("new")}
        }catch(e:Exception){Toast.makeText(this,"SMS: "+e.message,Toast.LENGTH_SHORT).show()}
    }
    private fun sendSms(){try{if(ContextCompat.checkSelfPermission(this,Manifest.permission.SEND_SMS)!=PackageManager.PERMISSION_GRANTED){ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.SEND_SMS),5); return}; val num=findViewById<EditText>(R.id.smsNumber)?.text.toString(); val body=findViewById<EditText>(R.id.smsBody)?.text.toString(); if(num.isBlank()||body.isBlank()){Toast.makeText(this,"Numero et message requis",Toast.LENGTH_SHORT).show(); return}; SmsManager.getDefault().sendTextMessage(num,null,body,null,null); Toast.makeText(this,"Envoye",Toast.LENGTH_SHORT).show(); findViewById<EditText>(R.id.smsBody)?.text?.clear(); switchMode("list")}catch(e:Exception){Toast.makeText(this,"Erreur: "+e.message,Toast.LENGTH_LONG).show()}}
}
class ConvAdapter(private val items:List<Triple<String,String,String>>,private val onClick:(String)->Unit):RecyclerView.Adapter<ConvAdapter.H>(){
    class H(v:View):RecyclerView.ViewHolder(v){val name:TextView=v.findViewById(R.id.convName); val body:TextView=v.findViewById(R.id.convBody); val icon:TextView=v.findViewById(R.id.convIcon)}
    override fun onCreateViewHolder(p:ViewGroup,t:Int):H{val v=LayoutInflater.from(p.context).inflate(R.layout.item_conversation,p,false); return H(v)}
    override fun getItemCount()=items.size
    override fun onBindViewHolder(h:H,pos:Int){
        val item=items[pos]
        h.name.text=item.second
        h.body.text=item.third.take(60)
        h.icon.text=item.second.take(1).uppercase()
        h.itemView.setOnClickListener{onClick(item.first)}
    }
}
