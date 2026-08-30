package com.luncher
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class FileManagerActivity : AppCompatActivity() {
    private lateinit var recycler: RecyclerView
    private lateinit var search: EditText
    private var currentPath: File = Environment.getExternalStorageDirectory()
    private var allFiles: List<File> = emptyList()
    private var clipboard: File? = null
    private var isCut = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_manager)
        recycler = findViewById(R.id.recyclerFiles)
        search = findViewById(R.id.searchFile)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.setHasFixedSize(true)
        recycler.itemAnimator = null

        findViewById<View>(R.id.btnParcourir)?.setOnClickListener { load(Environment.getExternalStorageDirectory()) }
        findViewById<View>(R.id.btnRecent)?.setOnClickListener { Toast.makeText(this,"Récent bientôt",0).show() }
        findViewById<View>(R.id.btnPaste)?.setOnClickListener { paste() }
        findViewById<View>(R.id.btnNewFolder)?.setOnClickListener { newFolder() }

        findViewById<View>(R.id.catDoc)?.setOnClickListener { filterCategory(listOf("pdf","doc","docx","txt")) }
        findViewById<View>(R.id.catImg)?.setOnClickListener { filterCategory(listOf("jpg","jpeg","png","webp","gif")) }
        findViewById<View>(R.id.catVid)?.setOnClickListener { filterCategory(listOf("mp4","mkv","avi","mov")) }
        findViewById<View>(R.id.catMusic)?.setOnClickListener { filterCategory(listOf("mp3","m4a","wav","flac")) }

        search.addTextChangedListener(object: android.text.TextWatcher{
            override fun afterTextChanged(s: android.text.Editable?){ filter(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int){}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int){}
        })

        load(currentPath)
    }

    private fun load(dir: File){
        currentPath = dir
        try{
            allFiles = dir.listFiles()?.sortedWith(compareBy({!it.isDirectory}, {it.name.lowercase()}))?: emptyList()
            recycler.adapter = FileAdapter(allFiles)
        }catch(e:Exception){ Toast.makeText(this,"Accès refusé",0).show() }
    }

    private fun filter(q:String){
        if(q.isBlank()) recycler.adapter = FileAdapter(allFiles)
        else recycler.adapter = FileAdapter(allFiles.filter{ it.name.contains(q, true) })
    }

    private fun filterCategory(exts: List<String>){
        val found = mutableListOf<File>()
        try{ Environment.getExternalStorageDirectory().walkTopDown().forEach{ if(!it.isDirectory && it.extension.lowercase() in exts && found.size<80) found.add(it) } }catch(e:Exception){}
        recycler.adapter = FileAdapter(found)
        Toast.makeText(this,"${found.size} fichiers",0).show()
    }

    private fun newFolder(){
        val input = EditText(this).apply{ hint="Nom du dossier" }
        AlertDialog.Builder(this).setTitle("Nouveau dossier").setView(input).setPositiveButton("Créer"){_,_-> val name=input.text.toString().trim(); if(name.isNotBlank()){ File(currentPath, name).mkdirs(); load(currentPath) } }.setNegativeButton("Annuler",null).show()
    }

    private fun paste(){
        val clip = clipboard?: run{ Toast.makeText(this,"Rien à coller",0).show(); return }
        try{
            val dest = File(currentPath, clip.name)
            if(isCut) clip.renameTo(dest) else clip.copyRecursively(dest, true)
            clipboard=null; Toast.makeText(this,"Collé",0).show(); load(currentPath)
        }catch(e:Exception){ Toast.makeText(this,"Erreur: ${e.message}",0).show() }
    }

    inner class FileAdapter(val files: List<File>): RecyclerView.Adapter<FileAdapter.H>(){
        inner class H(v: View): RecyclerView.ViewHolder(v){ val icon: TextView=v.findViewById(R.id.fIcon); val name: TextView=v.findViewById(R.id.fName); val info: TextView=v.findViewById(R.id.fInfo) }
        override fun onCreateViewHolder(p: android.view.ViewGroup, t: Int): H { return H(layoutInflater.inflate(R.layout.item_file_luncher, p, false)) }
        override fun getItemCount()=files.size
        override fun onBindViewHolder(h:H, pos:Int){
            val f=files[pos]; val isDir=f.isDirectory
            h.icon.text=if(isDir) "▭" else when(f.extension.lowercase()){ "jpg","jpeg","png","webp"->"◫"; "mp4","mkv"->"▶"; "mp3","m4a"->"♫"; "pdf"->"≡"; "zip"->"◫"; "apk"->"◍"; else->"≡" }
            h.name.text=f.name
            h.info.text=if(isDir) "${f.listFiles()?.size?:0} éléments" else "${f.length()/1024} Ko"
            h.itemView.setOnClickListener{ if(isDir) load(f) else{ try{ val uri=FileProvider.getUriForFile(this@FileManagerActivity, "$packageName.provider", f); val mime=when(f.extension.lowercase()){ "jpg","jpeg"->"image/jpeg"; "png"->"image/png"; "pdf"->"application/pdf"; "mp4"->"video/mp4"; "mp3"->"audio/mpeg"; else->"*/*" }; val intent=Intent(Intent.ACTION_VIEW).apply{ setDataAndType(uri, mime); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }; startActivity(intent) }catch(e:Exception){} } }
            h.itemView.setOnLongClickListener{
                val opts=arrayOf("Copier","Couper","Renommer","Partager","Supprimer")
                AlertDialog.Builder(this@FileManagerActivity).setTitle(f.name).setItems(opts){_,which->
                    when(which){
                        0->{ clipboard=f; isCut=false; Toast.makeText(this@FileManagerActivity,"Copié",0).show() }
                        1->{ clipboard=f; isCut=true; Toast.makeText(this@FileManagerActivity,"Coupé",0).show() }
                        2->{ val input=EditText(this@FileManagerActivity).apply{ setText(f.name) }; AlertDialog.Builder(this@FileManagerActivity).setTitle("Renommer").setView(input).setPositiveButton("OK"){_,_-> f.renameTo(File(f.parent, input.text.toString())); load(currentPath) }.show() }
                        3->{ try{ val uri=FileProvider.getUriForFile(this@FileManagerActivity, "$packageName.provider", f); val intent=Intent(Intent.ACTION_SEND).apply{ type="*/*"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }; startActivity(Intent.createChooser(intent,"Partager")) }catch(e:Exception){} }
                        4->{ AlertDialog.Builder(this@FileManagerActivity).setTitle("Supprimer?").setPositiveButton("Oui"){_,_-> if(f.isDirectory) f.deleteRecursively() else f.delete(); load(currentPath) }.setNegativeButton("Non",null).show() }
                    }
                }.show(); true
            }
        }
    }
}
