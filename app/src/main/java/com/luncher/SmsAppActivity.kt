package com.luncher
import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Telephony
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
class SmsAppActivity : AppCompatActivity() {
    private lateinit var recyclerConv: RecyclerView
    private lateinit var recyclerMsg: RecyclerView
    private var currentNumber: String? = null
    private var recorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioFile: String? = null
    private var isRecording = false
    private var pendingImageUri: Uri? = null
    private val PICK_IMAGE = 1001
    private val messages = mutableListOf<Msg>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sms)
        recyclerConv = findViewById(R.id.recyclerConv)
        recyclerMsg = findViewById(R.id.recyclerMsg)
        recyclerConv.layoutManager = LinearLayoutManager(this)
        recyclerMsg.layoutManager = LinearLayoutManager(this).apply{ stackFromEnd=true }
        findViewById<View>(R.id.back).setOnClickListener { finish() }
        findViewById<View>(R.id.backChat).setOnClickListener { showList() }
        findViewById<View>(R.id.btnNew).setOnClickListener {
            val dialog = android.app.AlertDialog.Builder(this)
            val input = EditText(this).apply{ hint="Numéro" }
            dialog.setTitle("Nouveau").setView(input).setPositiveButton("Ouvrir"){_,_-> val n=input.text.toString().trim(); if(n.isNotBlank()) showChat(n) }.show()
        }
        findViewById<View>(R.id.btnImage).setOnClickListener { pickImage() }
        findViewById<View>(R.id.btnSend).setOnClickListener { sendSms() }
        findViewById<View>(R.id.btnVoice).setOnClickListener { toggleVoice() }
        checkPerms()
    }
    private fun checkPerms(){
        val perms = arrayOf(Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS, Manifest.permission.READ_CONTACTS, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_MEDIA_IMAGES)
        if(perms.any{ ActivityCompat.checkSelfPermission(this,it)!=PackageManager.PERMISSION_GRANTED }) ActivityCompat.requestPermissions(this, perms, 200) else loadConversations()
    }
    override fun onRequestPermissionsResult(c:Int, p:Array<out String>, r:IntArray){ super.onRequestPermissionsResult(c,p,r); loadConversations() }
    private fun loadConversations(){
        val map = linkedMapOf<String, Conv>()
        try{
            val cur = contentResolver.query(Telephony.Sms.CONTENT_URI, null, null, null, Telephony.Sms.DATE+" DESC")
            cur?.use{ while(it.moveToNext()){ val addr=it.getString(it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))?: continue; val body=it.getString(it.getColumnIndexOrThrow(Telephony.Sms.BODY))?:""; val date=it.getLong(it.getColumnIndexOrThrow(Telephony.Sms.DATE)); if(!map.containsKey(addr)) map[addr]=Conv(addr,body,date) } }
        }catch(e:Exception){}
        recyclerConv.adapter = ConvAdapter(map.values.toList())
    }
    private fun showChat(number:String){
        currentNumber=number; messages.clear()
        findViewById<View>(R.id.chatArea).visibility=View.VISIBLE; recyclerConv.visibility=View.GONE
        findViewById<TextView>(R.id.chatTitle).text=number; loadMessages(number)
    }
    private fun showList(){ findViewById<View>(R.id.chatArea).visibility=View.GONE; recyclerConv.visibility=View.VISIBLE; currentNumber=null; mediaPlayer?.release(); loadConversations() }
    private fun loadMessages(number:String){
        messages.clear()
        try{
            val cur = contentResolver.query(Telephony.Sms.CONTENT_URI, null, "${Telephony.Sms.ADDRESS}=?", arrayOf(number), Telephony.Sms.DATE+" ASC")
            cur?.use{ while(it.moveToNext()){ val body=it.getString(it.getColumnIndexOrThrow(Telephony.Sms.BODY)); val type=it.getInt(it.getColumnIndexOrThrow(Telephony.Sms.TYPE)); val date=it.getLong(it.getColumnIndexOrThrow(Telephony.Sms.DATE)); messages.add(Msg(body,null,null,type==Telephony.Sms.MESSAGE_TYPE_SENT,date)) } }
        }catch(e:Exception){}
        recyclerMsg.adapter = MsgAdapter(messages); if(messages.isNotEmpty()) recyclerMsg.scrollToPosition(messages.size-1)
    }
    private fun sendSms(){
        val input=findViewById<EditText>(R.id.inputMsg); val txt=input.text.toString().trim(); val num=currentNumber?: return; if(txt.isBlank() && pendingImageUri==null) return
        try{
            if(pendingImageUri!=null){ messages.add(Msg(txt, pendingImageUri.toString(), null, true, System.currentTimeMillis())); recyclerMsg.adapter?.notifyItemInserted(messages.size-1); recyclerMsg.scrollToPosition(messages.size-1) }
            else{
                val sms=android.telephony.SmsManager.getDefault(); sms.sendTextMessage(num,null,txt,null,null)
                val values=ContentValues().apply{ put(Telephony.Sms.ADDRESS,num); put(Telephony.Sms.BODY,txt); put(Telephony.Sms.DATE,System.currentTimeMillis()); put(Telephony.Sms.TYPE,Telephony.Sms.MESSAGE_TYPE_SENT) }
                contentResolver.insert(Telephony.Sms.CONTENT_URI, values); messages.add(Msg(txt,null,null,true,System.currentTimeMillis())); recyclerMsg.adapter?.notifyItemInserted(messages.size-1); recyclerMsg.scrollToPosition(messages.size-1)
            }
            input.text.clear(); pendingImageUri=null; findViewById<TextView>(R.id.btnImage).text="📷"; Toast.makeText(this,"✓ Envoyé",0).show()
        }catch(e:Exception){ Toast.makeText(this,"Échec: ${e.message}",0).show() }
    }
    private fun pickImage(){ val intent=Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI); startActivityForResult(intent,PICK_IMAGE) }
    override fun onActivityResult(rc:Int, res:Int, data:Intent?){ super.onActivityResult(rc,res,data); if(rc==PICK_IMAGE && res==RESULT_OK && data!=null){ val uri=data.data; if(uri!=null){ pendingImageUri=uri; findViewById<TextView>(R.id.btnImage).text="✅📷"; currentNumber?.let{ messages.add(Msg("",uri.toString(),null,true,System.currentTimeMillis())); recyclerMsg.adapter?.notifyItemInserted(messages.size-1); recyclerMsg.scrollToPosition(messages.size-1) } } } }
    private fun toggleVoice(){ if(isRecording) stopRecording() else startRecording() }
    private fun startRecording(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){ ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO),201); return }
        try{
            val file="${externalCacheDir?.absolutePath}/voice_${System.currentTimeMillis()}.m4a"; audioFile=file
            recorder=MediaRecorder().apply{ setAudioSource(MediaRecorder.AudioSource.MIC); setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); setAudioEncoder(MediaRecorder.AudioEncoder.AAC); setOutputFile(file); prepare(); start() }
            isRecording=true; findViewById<TextView>(R.id.voiceStatus).visibility=View.VISIBLE; findViewById<TextView>(R.id.btnVoice).text="⏹️"
        }catch(e:Exception){ Toast.makeText(this,"Micro: ${e.message}",0).show() }
    }
    private fun stopRecording(){
        try{
            recorder?.stop(); recorder?.release(); recorder=null; isRecording=false; findViewById<TextView>(R.id.voiceStatus).visibility=View.GONE; findViewById<TextView>(R.id.btnVoice).text="🎤"
            audioFile?.let{ path-> messages.add(Msg("",null,path,true,System.currentTimeMillis())); recyclerMsg.adapter?.notifyItemInserted(messages.size-1); recyclerMsg.scrollToPosition(messages.size-1) }
        }catch(e:Exception){ Toast.makeText(this,"Erreur",0).show() }
    }
    private fun playAudio(path:String, btn:TextView){
        try{ mediaPlayer?.release(); mediaPlayer=MediaPlayer().apply{ setDataSource(path); prepare(); start(); setOnCompletionListener{ btn.text="▶️" } }; btn.text="⏸️" }catch(e:Exception){ Toast.makeText(this,"Lecture impossible",0).show() }
    }
    data class Conv(val number:String, val last:String, val date:Long)
    data class Msg(val body:String, val imageUri:String?, val audioPath:String?, val sent:Boolean, val date:Long)
    inner class ConvAdapter(val items:List<Conv>): RecyclerView.Adapter<ConvAdapter.H>(){
        inner class H(v:View): RecyclerView.ViewHolder(v){ val t1:TextView=v.findViewById(R.id.title); val t2:TextView=v.findViewById(R.id.sub) }
        override fun onCreateViewHolder(p:android.view.ViewGroup, t:Int): H { val ll=android.widget.LinearLayout(this@SmsAppActivity).apply{ orientation=android.widget.LinearLayout.VERTICAL; setPadding(24,18,24,18) }; val a=TextView(this@SmsAppActivity).apply{ id=R.id.title; textSize=15f }; val b=TextView(this@SmsAppActivity).apply{ id=R.id.sub; textSize=13f }; ll.addView(a); ll.addView(b); return H(ll) }
        override fun getItemCount()=items.size
        override fun onBindViewHolder(h:H, p:Int){ val c=items[p]; h.t1.text=c.number; h.t2.text=c.last; h.itemView.setOnClickListener{ showChat(c.number) } }
    }
    inner class MsgAdapter(val items:List<Msg>): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        override fun getItemViewType(pos:Int)=when{ items[pos].imageUri!=null->1; items[pos].audioPath!=null->2; else->0 }
        override fun onCreateViewHolder(p:android.view.ViewGroup, vt:Int): RecyclerView.ViewHolder{
            return when(vt){
                1->{ val ll=android.widget.LinearLayout(this@SmsAppActivity).apply{ orientation=android.widget.LinearLayout.VERTICAL; setPadding(12,8,12,8) }; val img=ImageView(this@SmsAppActivity).apply{ id=R.id.img; layoutParams=android.widget.LinearLayout.LayoutParams(600,600); scaleType=ImageView.ScaleType.CENTER_CROP }; val txt=TextView(this@SmsAppActivity).apply{ id=R.id.txt }; ll.addView(img); ll.addView(txt); object: RecyclerView.ViewHolder(ll){} }
                2->{ val ll=android.widget.LinearLayout(this@SmsAppActivity).apply{ orientation=android.widget.LinearLayout.HORIZONTAL; setPadding(16,12,16,12) }; val btn=TextView(this@SmsAppActivity).apply{ id=R.id.play; text="▶️" }; val dur=TextView(this@SmsAppActivity).apply{ id=R.id.dur; text="Vocal" }; ll.addView(btn); ll.addView(dur); object: RecyclerView.ViewHolder(ll){} }
                else->{ val tv=TextView(this@SmsAppActivity).apply{ id=R.id.txt; setPadding(24,14,24,14) }; val card=android.widget.FrameLayout(this@SmsAppActivity).apply{ setPadding(8,4,8,4); addView(tv) }; object: RecyclerView.ViewHolder(card){} }
            }
        }
        override fun getItemCount()=items.size
        override fun onBindViewHolder(h:RecyclerView.ViewHolder, pos:Int){
            val m=items[pos]; val v=h.itemView
            when{ m.imageUri!=null->{ try{ v.findViewById<ImageView>(R.id.img).setImageURI(Uri.parse(m.imageUri)) }catch(_:Exception){}; v.findViewById<TextView>(R.id.txt)?.text=m.body }
                m.audioPath!=null->{ val b=v.findViewById<TextView>(R.id.play); b.setOnClickListener{ playAudio(m.audioPath!!, b) } }
                else->{ val tv=v.findViewById<TextView>(R.id.txt); tv.text=m.body; tv.setBackgroundColor(if(m.sent) android.graphics.Color.BLACK else android.graphics.Color.parseColor("#F0F0F0")); tv.setTextColor(if(m.sent) android.graphics.Color.WHITE else android.graphics.Color.BLACK) }
            }
        }
    }
}
