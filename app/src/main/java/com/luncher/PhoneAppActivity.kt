package com.luncher
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.CallLog
import android.provider.ContactsContract
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
class PhoneAppActivity : AppCompatActivity() {
    private lateinit var recycler: RecyclerView
    private lateinit var input: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone)
        recycler = findViewById(R.id.recycler)
        input = findViewById(R.id.inputNumber)
        recycler.layoutManager = LinearLayoutManager(this)
        findViewById<TextView>(R.id.back).setOnClickListener { finish() }
        findViewById<TextView>(R.id.btnDial).setOnClickListener {
            val pad = findViewById<android.view.View>(R.id.dialPad)
            pad.visibility = if(pad.visibility==android.view.View.GONE) android.view.View.VISIBLE else android.view.View.GONE
        }
        findViewById<TextView>(R.id.btnCall).setOnClickListener { makeCall(input.text.toString()) }
        // dial pad clicks
        val root = findViewById<android.widget.GridLayout>(R.id.dialPad).parent as android.view.ViewGroup
        // attach listeners via traversing
        val grid = findViewById<android.widget.GridLayout>(R.id.dialPad).getChildAt(1) as android.widget.GridLayout
        for(i in 0 until grid.childCount){
            val tv = grid.getChildAt(i) as TextView
            tv.setOnClickListener { input.append(tv.text) }
        }
        checkPerms()
    }
    private fun checkPerms(){
        val perms = arrayOf(Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE)
        if(perms.any{ ActivityCompat.checkSelfPermission(this,it)!=PackageManager.PERMISSION_GRANTED }){
            ActivityCompat.requestPermissions(this, perms, 100)
        } else loadLogs()
    }
    override fun onRequestPermissionsResult(c:Int, p:Array<out String>, r:IntArray){ super.onRequestPermissionsResult(c,p,r); loadLogs() }
    private fun loadLogs(){
        val list = mutableListOf<CallItem>()
        try{
            val cur: Cursor? = contentResolver.query(CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE+" DESC")
            cur?.use{
                while(it.moveToNext() && list.size<100){
                    val num = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.NUMBER))
                    val name = it.getString(it.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME))?: num
                    val date = it.getLong(it.getColumnIndexOrThrow(CallLog.Calls.DATE))
                    val type = it.getInt(it.getColumnIndexOrThrow(CallLog.Calls.TYPE))
                    val dur = it.getLong(it.getColumnIndexOrThrow(CallLog.Calls.DURATION))
                    list.add(CallItem(name,num,date,type,dur))
                }
            }
        }catch(e:Exception){}
        recycler.adapter = CallAdapter(list)
    }
    private fun makeCall(num:String){
        if(num.isBlank()){ Toast.makeText(this,"Numéro vide",0).show(); return }
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), 101); return
        }
        try{ startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$num"))) }catch(e:Exception){ Toast.makeText(this,"Appel impossible",0).show() }
    }
    data class CallItem(val name:String, val number:String, val date:Long, val type:Int, val dur:Long)
    inner class CallAdapter(val items:List<CallItem>): RecyclerView.Adapter<CallAdapter.H>(){
        inner class H(v:android.view.View): RecyclerView.ViewHolder(v){
            val t1: TextView = v.findViewById(R.id.title)
            val t2: TextView = v.findViewById(R.id.sub)
        }
        override fun onCreateViewHolder(p:android.view.ViewGroup, t:Int): H {
            val v = layoutInflater.inflate(android.R.layout.simple_list_item_2, p, false)
            v.findViewById<TextView>(android.R.id.text1).id = R.id.title
            v.findViewById<TextView>(android.R.id.text2).id = R.id.sub
            // custom simple
            val card = android.widget.LinearLayout(this@PhoneAppActivity).apply{ orientation=android.widget.LinearLayout.VERTICAL; setPadding(24,20,24,20) }
            val a = TextView(this@PhoneAppActivity).apply{ id=R.id.title; textSize=15f; setTextColor(android.graphics.Color.BLACK) }
            val b = TextView(this@PhoneAppActivity).apply{ id=R.id.sub; textSize=12f; setTextColor(android.graphics.Color.GRAY) }
            card.addView(a); card.addView(b)
            card.setOnClickListener{}
            return H(card)
        }
        override fun getItemCount()=items.size
        override fun onBindViewHolder(h:H, pos:Int){
            val c = items[pos]
            val typeStr = when(c.type){ CallLog.Calls.INCOMING_TYPE->"Entrant"; CallLog.Calls.OUTGOING_TYPE->"Sortant"; CallLog.Calls.MISSED_TYPE->"Manqué"; else->"Appel" }
            h.t1.text = "${c.name} • $typeStr"
            h.t2.text = "${c.number} • ${c.dur}s • ${java.text.DateFormat.getDateTimeInstance().format(java.util.Date(c.date))}"
            h.itemView.setOnClickListener { makeCall(c.number) }
        }
    }
}
