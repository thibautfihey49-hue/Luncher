package com.luncher
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
class FileManagerActivity : AppCompatActivity() {
    private lateinit var recycler: RecyclerView
    private lateinit var pathView: TextView
    private lateinit var search: EditText
    private var currentPath: File = Environment.getExternalStorageDirectory()
    private var allFiles: List<File> = emptyList()
    private var clipboard: File? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_manager)
        recycler = findViewById(R.id.recyclerFiles)
        pathView = findViewById(R.id.pathView)
        search = findViewById(R.id.searchFile)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.setHasFixedSize(true)
        recycler.setItemViewCacheSize(30)
        findViewById<TextView>(R.id.back).setOnClickListener { goBack() }
        findViewById<TextView>(R.id.btnPaste).setOnClickListener { paste() }
        search.addTextChangedListener(object: android.text.TextWatcher{
            override fun afterTextChanged(s: android.text.Editable?){ filter(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, a:Int,b:Int,c:Int){}
            override fun onTextChanged(s: CharSequence?, a:Int,b:Int,c:Int){}
        })
        load(currentPath)
    }
    private fun load(dir: File){
        currentPath = dir
        pathView.text = dir.absolutePath.replace("/storage/emulated/0","Stockage")
        try{
            allFiles = dir.listFiles()?.sortedWith(compareBy({!it.isDirectory}, {it.name.lowercase()}))?: emptyList()
            recycler.adapter = FileAdapter(allFiles)
        }catch(e:Exception){ Toast.makeText(this,"Accès refusé",0).show() }
    }
    private fun filter(q:String){
        if(q.isBlank()){ recycler.adapter = FileAdapter(allFiles); return }
        recycler.adapter = FileAdapter(allFiles.filter{ it.name.contains(q, true) })
    }
    private fun goBack(){
        if(currentPath.parentFile!=null) load(currentPath.parentFile!!) else finish()
    }
    private fun paste(){
        val clip = clipboard?: return
        try{
            val dest = File(currentPath, clip.name)
            clip.copyTo(dest, true)
            Toast.makeText(this,"Collé",0).show(); load(currentPath)
        }catch(e:Exception){ Toast.makeText(this,"Erreur",0).show() }
    }
    inner class FileAdapter(val files: List<File>): RecyclerView.Adapter<FileAdapter.H>(){
        inner class H(v: android.view.View): RecyclerView.ViewHolder(v){
            val icon: TextView = v.findViewById(R.id.fIcon)
            val name: TextView = v.findViewById(R.id.fName)
            val info: TextView = v.findViewById(R.id.fInfo)
        }
        override fun onCreateViewHolder(p: android.view.ViewGroup, t: Int): H {
            val v = layoutInflater.inflate(R.layout.item_file, p, false); return H(v)
        }
        override fun getItemCount()=files.size
        override fun onBindViewHolder(h:H, pos:Int){
            val f = files[pos]
            h.icon.text = if(f.isDirectory) "📁" else when(f.extension.lowercase()){ "jpg","png","jpeg"->"🖼️"; "mp4","mkv"->"🎬"; "mp3","m4a"->"🎵"; "pdf"->"📄"; "zip"->"🗜️"; else->"📄" }
            h.name.text = f.name
            h.info.text = if(f.isDirectory) "${f.listFiles()?.size?:0} éléments" else "${f.length()/1024} Ko • ${java.text.DateFormat.getDateInstance().format(java.util.Date(f.lastModified()))}"
            h.itemView.setOnClickListener{ if(f.isDirectory) load(f) else Toast.makeText(this@FileManagerActivity, f.name,0).show() }
        }
    }
}
