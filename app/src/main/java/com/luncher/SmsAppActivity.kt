package com.luncher
import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
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
import java.util.concurrent.Executors

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
    private val contactsList = mutableListOf<Contact>()
    private val executor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sms)
        recyclerConv = findViewById(R.id.recyclerConv)
        recyclerMsg = findViewById(R.id.recyclerMsg)
        recyclerConv.layoutManager = LinearLayoutManager(this)
        recyclerConv.setHasFixedSize(true); recyclerConv.itemAnimator=null
        recyclerMsg.layoutManager = LinearLayoutManager(this).apply{ stackFromEnd=true }
        recyclerMsg.setHasFixedSize(true); recyclerMsg.itemAnimator=null

        // Tous les boutons en safe?. pour ne jamais crasher
        findViewById<View>(R.id.back)?.setOnClickListener { finish() }
        findViewById<View>(R.id.backChat)?.setOnClickListener { showList() }
        findViewById<View>(R.id.btnNew)?.setOnClickListener { showNewMessageDialog() }
        findViewById<View>(R.id.btnImage)?.setOnClickListener { pickImage() }
        findViewById<View>(R.id.btnSend)?.setOnClickListener { sendSms() }
        findViewById<View>(R.id.btnVoice)?.setOnClickListener { toggleVoice() }

        checkPerms()
    }

    private fun checkPerms(){
        val perms = arrayOf(Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS, Manifest.permission.READ_CONTACTS, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_MEDIA_IMAGES)
        if(perms.any{ ActivityCompat.checkSelfPermission(this,it)!=PackageManager.PERMISSION_GRANTED }) ActivityCompat.requestPermissions(this, perms, 200) else { loadConversationsFast(); loadContactsFast() }
    }
    override fun onRequestPermissionsResult(c:Int, p:Array<out String>, r:IntArray){ super.onRequestPermissionsResult(c,p,r); loadConversationsFast(); loadContactsFast() }

    private fun loadContactsFast(){
        executor.execute{
            contactsList.clear()
            try{
                val cur: Cursor? = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER), null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC")
                cur?.use{
                    val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    while(it.moveToNext() && contactsList.size<500){
                        val n = it.getString(nameIdx)?: continue
                        val num = it.getString(numIdx)?: continue
                        if(contactsList.none{ c-> c.number==num }) contactsList.add(Contact(n,num))
                    }
                }
            }catch(e:Exception){}
        }
    }

    private fun loadConversationsFast(){
        executor.execute{
            val map = linkedMapOf<String, Conv>()
            try{
                val cur = contentResolver.query(Telephony.Sms.CONTENT_URI, null, null, null, Telephony.Sms.DATE+" DESC")
                cur?.use{ while(it.moveToNext() && map.size<100){ val addr=it.getString(it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))?: continue; val body=it.getString(it.getColumnIndexOrThrow(Telephony.Sms.BODY))?:""; val date=it.getLong(it.getColumnIndexOrThrow(Telephony.Sms.DATE)); if(!map.containsKey(addr)) map[addr]=Conv(addr,body,date) } }
            }catch(e:Exception){}
            runOnUiThread{ recyclerConv.adapter = ConvAdapter(map.values.toList()); recyclerConv.visibility = View.VISIBLE; findViewById<View>(R.id.chatArea)?.visibility = View.GONE }
        }
    }

    private fun showNewMessageDialog(){
        val dialogView = layoutInflater.inflate(R.layout.dialog_new_message, null)
        val search = dialogView.findViewById<EditText>(R.id.searchContact)
        val recycler = dialogView.findViewById<RecyclerView>(R.id.recyclerContacts)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = object: RecyclerView.Adapter<RecyclerView.ViewHolder>(){
            var items = contactsList.toList()
            override fun onCreateViewHolder(p: android.view.ViewGroup, t: Int): RecyclerView.ViewHolder { val v = layoutInflater.inflate(R.layout.item_contact_pick, p, false); return object: RecyclerView.ViewHolder(v){} }
            override fun getItemCount()=items.size
            override fun onBindViewHolder(h: RecyclerView.ViewHolder, pos: Int){
                val c = items[pos]
                h.itemView.findViewById<TextView>(R.id.name).text = c.name
                h.itemView.findViewById<TextView>(R.id.number).text = c.number
                h.itemView.setOnClickListener{ showChat(c.number); try{ (dialogView.tag as android.app.AlertDialog).dismiss() }catch(_:Exception){} }
            }
            fun update(q:String){
                items = if(q.isBlank()) contactsList else contactsList.filter{ it.name.contains(q,true) || it.number.contains(q,true) }
                notifyDataSetChanged()
            }
        }
        search.addTextChangedListener(object: android.text.TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int){}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int){}
            override fun afterTextChanged(s: android.text.Editable?){ (recycler.adapter as? RecyclerView.Adapter<*>)?.let{ val adapter = it as? Any; try{ val m = it.javaClass.getMethod("update", String::class.java); m.invoke(it, s.toString()) }catch(_:Exception){} } }
        })
        val manual = dialogView.findViewById<EditText>(R.id.manualNumber)
        dialogView.findViewById<View>(R.id.btnOpenManual)?.setOnClickListener{
            val n = manual.text.toString().trim()
            if(n.isNotBlank()){ showChat(n); try{ (dialogView.tag as android.app.AlertDialog).dismiss() }catch(_:Exception){} }
        }
        val alert = android.app.AlertDialog.Builder(this).setView(dialogView).create()
        dialogView.tag = alert
        alert.show()
    }

    private fun showChat(number:String){
        currentNumber=number; messages.clear()
        findViewById<View>(R.id.chatArea)?.visibility=View.VISIBLE; recyclerConv.visibility=View.GONE
        findViewById<TextView>(R.id.chatTitle)?.text=number; loadMessagesFast(number)
    }
    private fun showList(){ findViewById<View>(R.id.chatArea)?.visibility=View.GONE; recyclerConv.visibility=View.VISIBLE; currentNumber=null; mediaPlayer?.release(); loadConversationsFast() }

    private fun loadMessagesFast(number:String){
        executor.execute{
            val list = mutableListOf<Msg>()
            try{
                val cur = contentResolver.query(Telephony.Sms.CONTENT_URI, null, "${Telephony.Sms.ADDRESS}=?", arrayOf(number), Telephony.Sms.DATE+" ASC")
                cur?.use{ while(it.moveToNext() && list.size<200){ val body=it.getString(it.getColumnIndexOrThrow(Telephony.Sms.BODY)); val type=it.getInt(it.getColumnIndexOrThrow(Telephony.Sms.TYPE)); list.add(Msg(body,null,null,type==Telephony.Sms.MESSAGE_TYPE_SENT,0)) } }
            }catch(e:Exception){}
            runOnUiThread{ messages.clear(); messages.addAll(list); recyclerMsg.adapter = MsgAdapter(messages); if(messages.isNotEmpty()) recyclerMsg.scrollToPosition(messages.size-1) }
        }
    }

    private fun sendSms(){
        val input=findViewById<EditText>(R.id.inputMsg)?: return; val txt=input.text.toString().trim(); val num=currentNumber?: return; if(txt.isBlank() && pendingImageUri==null) return
        try{
            android.telephony.SmsManager.getDefault().sendTextMessage(num,null,txt,null,null)
            val values=ContentValues().apply{ put(Telephony.Sms.ADDRESS,num); put(Telephony.Sms.BODY,txt); put(Telephony.Sms.DATE,System.currentTimeMillis()); put(Telephony.Sms.TYPE,Telephony.Sms.MESSAGE_TYPE_SENT) }
            contentResolver.insert(Telephony.Sms.CONTENT_URI, values)
            messages.add(Msg(txt,null,null,true,System.currentTimeMillis())); recyclerMsg.adapter?.notifyItemInserted(messages.size-1); recyclerMsg.scrollToPosition(messages.size-1)
            input.text.clear(); pendingImageUri=null; findViewById<TextView>(R.id.btnImage)?.text="📷"
        }catch(e:Exception){ Toast.makeText(this,"Échec",0).show() }
    }
    private fun pickImage(){ startActivityForResult(Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), PICK_IMAGE) }
    override fun onActivityResult(rc:Int, res:Int, data:Intent?){ super.onActivityResult(rc,res,data); if(rc==PICK_IMAGE && res==RESULT_OK && data!=null){ data.data?.let{ pendingImageUri=it; findViewById<TextView>(R.id.btnImage)?.text="✅" } } }
    private fun toggleVoice(){ if(isRecording) stopRecording() else startRecording() }
    private fun startRecording(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){ ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO),201); return }
        try{ val file="${externalCacheDir?.absolutePath}/voice_${System.currentTimeMillis()}.m4a"; audioFile=file; recorder=MediaRecorder().apply{ setAudioSource(MediaRecorder.AudioSource.MIC); setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); setAudioEncoder(MediaRecorder.AudioEncoder.AAC); setOutputFile(file); prepare(); start() }; isRecording=true; findViewById<TextView>(R.id.voiceStatus)?.visibility=View.VISIBLE; findViewById<TextView>(R.id.btnVoice)?.text="⏹️" }catch(e:Exception){}
    }
    private fun stopRecording(){ try{ recorder?.stop(); recorder?.release(); recorder=null; isRecording=false; findViewById<TextView>(R.id.voiceStatus)?.visibility=View.GONE; findViewById<TextView>(R.id.btnVoice)?.text="🎤" }catch(e:Exception){} }
    data class Conv(val number:String, val last:String, val date:Long)
    data class Msg(val body:String, val imageUri:String?, val audioPath:String?, val sent:Boolean, val date:Long)
    data class Contact(val name:String, val number:String)
    inner class ConvAdapter(val items:List<Conv>): RecyclerView.Adapter<ConvAdapter.H>(){
        inner class H(v:View): RecyclerView.ViewHolder(v){ val t1:TextView=v.findViewById(R.id.title); val t2:TextView=v.findViewById(R.id.sub) }
        override fun onCreateViewHolder(p:android.view.ViewGroup, t:Int): H { return H(layoutInflater.inflate(R.layout.item_sms_conv, p, false)) }
        override fun getItemCount()=items.size
        override fun onBindViewHolder(h:H, p:Int){ val c=items[p]; h.t1.text=c.number; h.t2.text=c.last; h.itemView.setOnClickListener{ showChat(c.number) } }
    }
    inner class MsgAdapter(val items:List<Msg>): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        override fun getItemCount()=items.size
        override fun onCreateViewHolder(p:android.view.ViewGroup, vt:Int): RecyclerView.ViewHolder{
            val tv=TextView(this@SmsAppActivity).apply{ id=R.id.txt; setPadding(32,20,32,20) }; return object: RecyclerView.ViewHolder(tv){}
        }
        override fun onBindViewHolder(h:RecyclerView.ViewHolder, pos:Int){
            val m=items[pos]; val tv=h.itemView as TextView; tv.text=m.body; tv.setBackgroundColor(if(m.sent) 0xFF111827.toInt() else 0xFFFFFFFF.toInt()); tv.setTextColor(if(m.sent) 0xFFFFFFFF.toInt() else 0xFF111827.toInt())
        }
    }
    override fun onDestroy(){ super.onDestroy(); executor.shutdown() }
}
