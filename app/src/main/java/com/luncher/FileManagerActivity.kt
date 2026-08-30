package com.luncher
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
class FileManagerActivity : AppCompatActivity() {
    private lateinit var recycler: RecyclerView
    private lateinit var pathView: TextView
    private lateinit var search: EditText
    private var currentPath: File = Environment.getExternalStorageDirectory()
    private var allFiles: List<File> = emptyList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_manager)
        recycler = findViewById(R.id.recyclerFiles)
        pathView = findViewById(R.id.pathView)
        search = findViewById(R.id.searchFile)
        recycler.layoutManager = LinearLayoutManager(this)
        findViewById<View>(R.id.back).setOnClickListener { goBack() }
        findViewById<View>(R.id.btnPaste).setOnClickListener { }
        search.addTextChangedListener(object: android.text.TextWatcher{
            override fun afterTextChanged(s: android.text.Editable?){ filter(s.toString()) }
            override fun beforeTextChanged(s:CharSequence?, a:Int,b:Int,c:Int){}
            override fun onTextChanged(s:CharSequence?, a:Int,b:Int,c:Int){}
        })
        load(currentPath)
    }
    private fun load(dir: File){
        currentPath=dir; pathView.text=dir.absolutePath
        try{ allFiles=dir.listFiles()?.sortedWith(compareBy({!it.isDirectory},{it.name.lowercase()}))?: emptyList(); recycler.adapter=FileAdapter(allFiles) }catch(e:Exception){ Toast.makeText(this,"Accès refusé",0).show() }
    }
    private fun filter(q:String){ if(q.isBlank()) recycler.adapter=FileAdapter(allFiles) else recycler.adapter=FileAdapter(allFiles.filter{ it.name.contains(q,true) }) }
    private fun goBack(){ if(currentPath.parentFile!=null) load(currentPath.parentFile!!) else finish() }
    inner class FileAdapter(val files:List<File>): RecyclerView.Adapter<FileAdapter.H>(){
        inner class H(v:View): RecyclerView.ViewHolder(v){ val icon:TextView=v.findViewById(R.id.fIcon); val name:TextView=v.findViewById(R.id.fName); val info:TextView=v.findViewById(R.id.fInfo) }
        override fun onCreateViewHolder(p:android.view.ViewGroup, t:Int): H { return H(layoutInflater.inflate(R.layout.item_file, p, false)) }
        override fun getItemCount()=files.size
        override fun onBindViewHolder(h:H, pos:Int){
            val f=files[pos]; h.icon.text=if(f.isDirectory) "📁" else "📄"; h.name.text=f.name; h.info.text=if(f.isDirectory) "${f.listFiles()?.size?:0} éléments" else "${f.length()/1024} Ko"; h.itemView.setOnClickListener{ if(f.isDirectory) load(f) }
        }
    }
}
