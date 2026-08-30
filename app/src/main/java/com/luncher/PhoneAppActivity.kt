package com.luncher
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.CallLog
import android.view.View
import android.widget.EditText
import android.widget.GridLayout
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
        findViewById<View>(R.id.back).setOnClickListener { finish() }
        findViewById<View>(R.id.btnDial).setOnClickListener {
            val pad = findViewById<View>(R.id.dialPad)
            pad.visibility = if(pad.visibility==View.GONE) View.VISIBLE else View.GONE
        }
        findViewById<View>(R.id.btnCall).setOnClickListener { makeCall(input.text.toString()) }
        val grid = findViewById<GridLayout>(R.id.dialGrid)
        for(i in 0 until grid.childCount){
            val tv = grid.getChildAt(i) as TextView
            tv.setOnClickListener { input.append(tv.text) }
        }
        checkPerms()
    }
    private fun checkPerms(){
        val perms = arrayOf(Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE)
        if(perms.any{ ActivityCompat.checkSelfPermission(this,it)!=PackageManager.PERMISSION_GRANTED }) ActivityCompat.requestPermissions(this, perms, 100) else loadLogs()
    }
    override fun onRequestPermissionsResult(c:Int, p:Array<out String>, r:IntArray){ super.onRequestPermissionsResult(c,p,r); loadLogs() }
    private fun loadLogs(){
        val list = mutableListOf<CallItem>()
        try{
            val cur: Cursor? = contentResolver.query(CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE+" DESC")
            cur?.use{
                while(it.moveToNext() && list.size<200){
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
        if(num.isBlank()) return
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)!=PackageManager.PERMISSION_GRANTED){ ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), 101); return }
        try{ startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:$num"))) }catch(e:Exception){ Toast.makeText(this,"Appel impossible",0).show() }
    }
    data class CallItem(val name:String, val number:String, val date:Long, val type:Int, val dur:Long)
    inner class CallAdapter(val items:List<CallItem>): RecyclerView.Adapter<CallAdapter.H>(){
        inner class H(v:View): RecyclerView.ViewHolder(v){ val t1: TextView = v.findViewById(R.id.t1); val t2: TextView = v.findViewById(R.id.t2); val btn: View = v.findViewById(R.id.btnCall) }
        override fun onCreateViewHolder(p:android.view.ViewGroup, t:Int): H { return H(layoutInflater.inflate(R.layout.item_call, p, false)) }
        override fun getItemCount()=items.size
        override fun onBindViewHolder(h:H, pos:Int){
            val c = items[pos]
            val typeStr = when(c.type){ CallLog.Calls.INCOMING_TYPE->"Entrant • "; CallLog.Calls.OUTGOING_TYPE->"Sortant • "; CallLog.Calls.MISSED_TYPE->"Manqué • "; else->"" }
            h.t1.text = c.name
            h.t2.text = typeStr + "${c.number} • ${c.dur}s • ${java.text.DateFormat.getDateTimeInstance().format(java.util.Date(c.date))}"
            h.btn.setOnClickListener{ makeCall(c.number) }
            h.itemView.setOnClickListener{ makeCall(c.number) }
        }
    }
}
