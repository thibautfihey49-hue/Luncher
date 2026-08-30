package com.luncher
import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Telephony
import android.widget.EditText
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
    private var audioFile: String? = null
    private var isRecording = false
    private val PICK_IMAGE = 1001
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sms)
        recyclerConv = findViewById(R.id.recyclerConv)
        recyclerMsg = findViewById(R.id.recyclerMsg)
        recyclerConv.layoutManager = LinearLayoutManager(this)
        recyclerMsg.layoutManager = LinearLayoutManager(this).apply{ stackFromEnd=true }
        findViewById<TextView>(R.id.back).setOnClickListener { finish() }
        findViewById<TextView>(R.id.backChat).setOnClickListener { showList() }
        findViewById<TextView>(R.id.btnNew).setOnClickListener {
            val i = android.content.Intent(android.content.Intent.ACTION_SENDTO, Uri.parse("smsto:"))
            i.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            try{ startActivity(i) }catch(_:Exception){ Toast.makeText(this,"Entrez un numéro dans le chat",0).show() }
        }
        findViewById<TextView>(R.id.btnImage).setOnClickListener { pickImage() }
        findViewById<TextView>(R.id.btnSend).setOnClickListener { sendSms() }
        findViewById<TextView>(R.id.btnVoice).setOnClickListener { toggleVoice() }
        checkPerms()
    }
    private fun checkPerms(){
        val perms = arrayOf(Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS, Manifest.permission.READ_CONTACTS, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_MEDIA_IMAGES)
        if(perms.any{ ActivityCompat.checkSelfPermission(this,it)!=PackageManager.PERMISSION_GRANTED }){
            ActivityCompat.requestPermissions(this, perms, 200)
        } else loadConversations()
    }
    override fun onRequestPermissionsResult(c:Int, p:Array<out String>, r:IntArray){ super.onRequestPermissionsResult(c,p,r); loadConversations() }
    private fun loadConversations(){
        val map = linkedMapOf<String, Conv>()
        try{
            val cur = contentResolver.query(Telephony.Sms.CONTENT_URI, null, null, null, Telephony.Sms.DATE+" DESC")
            cur?.use{
                while(it.moveToNext()){
                    val addr = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))?: continue
                    val body = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.BODY))?: ""
                    val date = it.getLong(it.getColumnIndexOrThrow(Telephony.Sms.DATE))
                    if(!map.containsKey(addr)) map[addr]=Conv(addr, body, date)
                }
            }
        }catch(e:Exception){}
        recyclerConv.adapter = ConvAdapter(map.values.toList())
    }
    private fun showChat(number:String){
        currentNumber = number
        findViewById<android.view.View>(R.id.chatArea).visibility = android.view.View.VISIBLE
        recyclerConv.visibility = android.view.View.GONE
        findViewById<TextView>(R.id.chatTitle).text = number
        loadMessages(number)
    }
    private fun showList(){
        findViewById<android.view.View>(R.id.chatArea).visibility = android.view.View.GONE
        recyclerConv.visibility = android.view.View.VISIBLE
        currentNumber=null
        loadConversations()
    }
    private fun loadMessages(number:String){
        val list = mutableListOf<Msg>()
        try{
            val cur = contentResolver.query(Telephony.Sms.CONTENT_URI, null, "${Telephony.Sms.ADDRESS}=?", arrayOf(number), Telephony.Sms.DATE+" ASC")
            cur?.use{
                while(it.moveToNext()){
                    val body = it.getString(it.getColumnIndexOrThrow(Telephony.Sms.BODY))
                    val type = it.getInt(it.getColumnIndexOrThrow(Telephony.Sms.TYPE))
                    val date = it.getLong(it.getColumnIndexOrThrow(Telephony.Sms.DATE))
                    list.add(Msg(body, type==Telephony.Sms.MESSAGE_TYPE_SENT, date))
                }
            }
        }catch(e:Exception){}
        recyclerMsg.adapter = MsgAdapter(list)
        recyclerMsg.scrollToPosition(list.size-1)
    }
    private fun sendSms(){
        val input = findViewById<EditText>(R.id.inputMsg)
        val txt = input.text.toString().trim()
        val num = currentNumber
        if(num==null){ Toast.makeText(this,"Pas de contact sélectionné",0).show(); return }
        if(txt.isBlank()) return
        try{
            val sms = android.telephony.SmsManager.getDefault()
            sms.sendTextMessage(num, null, txt, null, null)
            // save to inbox
            val values = ContentValues().apply{
                put(Telephony.Sms.ADDRESS, num)
                put(Telephony.Sms.BODY, txt)
                put(Telephony.Sms.DATE, System.currentTimeMillis())
                put(Telephony.Sms.TYPE, Telephony.Sms.MESSAGE_TYPE_SENT)
            }
            contentResolver.insert(Telephony.Sms.CONTENT_URI, values)
            input.text.clear()
            loadMessages(num)
            Toast.makeText(this,"✓ Envoyé",0).show()
        }catch(e:Exception){ Toast.makeText(this,"Échec envoi: ${e.message}",0).show() }
    }
    private fun pickImage(){
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE)
    }
    override fun onActivityResult(rc:Int, res:Int, data:Intent?){
        super.onActivityResult(rc,res,data)
        if(rc==PICK_IMAGE && res==RESULT_OK && data!=null){
            val uri = data.data
            val num = currentNumber
            if(num!=null && uri!=null){
                try{
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.putExtra(Intent.EXTRA_STREAM, uri)
                    intent.type = "image/*"
                    intent.putExtra("address", num)
                    intent.putExtra(Intent.EXTRA_TEXT, "Image")
                    intent.setPackage("com.google.android.apps.messaging")
                    startActivity(Intent.createChooser(intent, "Envoyer image via"))
                }catch(e:Exception){
                    Toast.makeText(this,"Image sélectionnée: $uri - envoi via MMS non natif, ouverture SMS",0).show()
                }
            }
        }
    }
    private fun toggleVoice(){
        if(isRecording){
            stopRecording()
        } else {
            startRecording()
        }
    }
    private fun startRecording(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 201); return
        }
        try{
            val file = "${externalCacheDir?.absolutePath}/voice_${System.currentTimeMillis()}.m4a"
            audioFile=file
            recorder = MediaRecorder().apply{
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file)
                prepare(); start()
            }
            isRecording=true
            findViewById<TextView>(R.id.voiceStatus).visibility = android.view.View.VISIBLE
            Toast.makeText(this,"🎤 Enregistrement...",0).show()
        }catch(e:Exception){ Toast.makeText(this,"Erreur micro: ${e.message}",0).show() }
    }
    private fun stopRecording(){
        try{
            recorder?.stop(); recorder?.release(); recorder=null
            isRecording=false
            findViewById<TextView>(R.id.voiceStatus).visibility = android.view.View.GONE
            // Envoi comme MMS ou partage
            audioFile?.let{
                val uri = Uri.parse(it)
                val intent = Intent(Intent.ACTION_SEND).apply{
                    type="audio/*"
                    putExtra(Intent.EXTRA_STREAM, Uri.fromFile(java.io.File(it)))
                    putExtra("address", currentNumber)
                }
                startActivity(Intent.createChooser(intent, "Envoyer vocal"))
                Toast.makeText(this,"Vocal enregistré: $it",0).show()
            }
        }catch(e:Exception){ Toast.makeText(this,"Erreur arrêt: ${e.message}",0).show() }
    }
    data class Conv(val number:String, val last:String, val date:Long)
    data class Msg(val body:String, val sent:Boolean, val date:Long)
    inner class ConvAdapter(val items:List<Conv>): RecyclerView.Adapter<ConvAdapter.H>(){
        inner class H(v:android.view.View): RecyclerView.ViewHolder(v){ val t1:TextView=v.findViewById(R.id.title); val t2:TextView=v.findViewById(R.id.sub) }
        override fun onCreateViewHolder(p:android.view.ViewGroup, t:Int): H {
            val ll = android.widget.LinearLayout(this@SmsAppActivity).apply{ orientation=android.widget.LinearLayout.VERTICAL; setPadding(24,18,24,18) }
            val a = TextView(this@SmsAppActivity).apply{ id=R.id.title; textSize=15f; setTextColor(android.graphics.Color.BLACK) }
            val b = TextView(this@SmsAppActivity).apply{ id=R.id.sub; textSize=13f; setTextColor(android.graphics.Color.GRAY); maxLines=1 }
            ll.addView(a); ll.addView(b); return H(ll)
        }
        override fun getItemCount()=items.size
        override fun onBindViewHolder(h:H, p:Int){ val c=items[p]; h.t1.text=c.number; h.t2.text=c.last; h.itemView.setOnClickListener{ showChat(c.number) } }
    }
    inner class MsgAdapter(val items:List<Msg>): RecyclerView.Adapter<MsgAdapter.H>(){
        inner class H(v:android.view.View): RecyclerView.ViewHolder(v){ val txt:TextView=v.findViewById(R.id.txt) }
        override fun onCreateViewHolder(p:android.view.ViewGroup, t:Int): H {
            val tv = TextView(this@SmsAppActivity).apply{ id=R.id.txt; setPadding(24,14,24,14); textSize=15f }
            val card = android.widget.FrameLayout(this@SmsAppActivity).apply{ setPadding(8,4,8,4); addView(tv) }
            return H(card)
        }
        override fun getItemCount()=items.size
        override fun onBindViewHolder(h:H, p:Int){
            val m=items[p]
            h.txt.text=m.body
            h.txt.setBackgroundColor(if(m.sent) android.graphics.Color.BLACK else android.graphics.Color.parseColor("#F0F0F0"))
            h.txt.setTextColor(if(m.sent) android.graphics.Color.WHITE else android.graphics.Color.BLACK)
        }
    }
}
