package com.luncher
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
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
    private lateinit var pathView: TextView
    private lateinit var search: EditText
    private lateinit var infoView: TextView
    private var currentPath: File = Environment.getExternalStorageDirectory()
    private var allFiles: List<File> = emptyList()
    private var clipboard: File? = null
    private var isCut = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_manager)
        recycler = findViewById(R.id.recyclerFiles)
        pathView = findViewById(R.id.pathView)
        search = findViewById(R.id.searchFile)
        infoView = findViewById(R.id.infoView)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.setHasFixedSize(true)

        findViewById<View>(R.id.back).setOnClickListener { goBack() }
        findViewById<View>(R.id.btnHome).setOnClickListener { load(Environment.getExternalStorageDirectory()) }
        findViewById<View>(R.id.btnPaste).setOnClickListener { paste() }
        findViewById<View>(R.id.btnNewFolder).setOnClickListener { newFolder() }
        findViewById<View>(R.id.btnSelectAll).setOnClickListener { Toast.makeText(this,"Sélection: ${allFiles.size}",0).show() }

        search.addTextChangedListener(object: android.text.TextWatcher{
            override fun afterTextChanged(s: android.text.Editable?){ filter(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int){}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int){}
        })
        load(currentPath)
    }

    private fun load(dir: File){
        currentPath = dir
        pathView.text = dir.absolutePath.replace("/storage/emulated/0","/Stockage interne")
        try{
            val files = dir.listFiles()
            allFiles = files?.sortedWith(compareBy({!it.isDirectory}, {it.name.lowercase()}))?: emptyList()
            infoView.text = "${allFiles.size} éléments • ${allFiles.count{it.isDirectory}} dossiers"
            recycler.adapter = FileAdapter(allFiles)
        }catch(e:Exception){ Toast.makeText(this,"Accès refusé",0).show() }
    }

    private fun filter(q:String){
        if(q.isBlank()) recycler.adapter = FileAdapter(allFiles)
        else recycler.adapter = FileAdapter(allFiles.filter{ it.name.contains(q, true) })
    }

    private fun goBack(){ if(currentPath.parentFile!=null) load(currentPath.parentFile!!) else finish() }

    private fun newFolder(){
        val input = EditText(this).apply{ hint="Nom du dossier" }
        AlertDialog.Builder(this).setTitle("Nouveau dossier").setView(input).setPositiveButton("Créer"){_,_->
            val name = input.text.toString().trim()
            if(name.isNotBlank()){
                File(currentPath, name).mkdirs()
                load(currentPath)
            }
        }.setNegativeButton("Annuler",null).show()
    }

    private fun paste(){
        val clip = clipboard?: return
        try{
            val dest = File(currentPath, clip.name)
            if(isCut){ clip.renameTo(dest); Toast.makeText(this,"Déplacé",0).show() }
            else{ clip.copyRecursively(dest, true); Toast.makeText(this,"Copié",0).show() }
            clipboard=null
            load(currentPath)
        }catch(e:Exception){ Toast.makeText(this,"Erreur: ${e.message}",0).show() }
    }

    private fun shareFile(f:File){
        try{
            val uri = FileProvider.getUriForFile(this, "$packageName.provider", f)
            val intent = Intent(Intent.ACTION_SEND).apply{ type="*/*"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            startActivity(Intent.createChooser(intent, "Partager"))
        }catch(e:Exception){ Toast.makeText(this,"Partage impossible",0).show() }
    }

    private fun renameFile(f:File){
        val input = EditText(this).apply{ setText(f.name) }
        AlertDialog.Builder(this).setTitle("Renommer").setView(input).setPositiveButton("OK"){_,_->
            val newName = input.text.toString().trim()
            if(newName.isNotBlank()){ f.renameTo(File(f.parent, newName)); load(currentPath) }
        }.show()
    }

    inner class FileAdapter(val files: List<File>): RecyclerView.Adapter<FileAdapter.H>(){
        inner class H(v: View): RecyclerView.ViewHolder(v){
            val icon: TextView = v.findViewById(R.id.fIcon)
            val name: TextView = v.findViewById(R.id.fName)
            val info: TextView = v.findViewById(R.id.fInfo)
            val btnCopy: View = v.findViewById(R.id.fCopy)
            val btnCut: View = v.findViewById(R.id.fCut)
            val btnMore: View = v.findViewById(R.id.fMore)
        }
        override fun onCreateViewHolder(p: android.view.ViewGroup, t: Int): H { return H(layoutInflater.inflate(R.layout.item_file, p, false)) }
        override fun getItemCount()=files.size
        override fun onBindViewHolder(h:H, pos:Int){
            val f = files[pos]
            val ext = f.extension.lowercase()
            h.icon.text = when{
                f.isDirectory -> "📁"
                ext in listOf("jpg","jpeg","png","webp") -> "🖼️"
                ext in listOf("mp4","mkv","avi") -> "🎬"
                ext in listOf("mp3","m4a","wav","flac") -> "🎵"
                ext=="pdf" -> "📄"
                ext in listOf("zip","rar","7z") -> "🗜️"
                ext in listOf("apk") -> "📦"
                else -> "📄"
            }
            h.name.text = f.name
            h.info.text = if(f.isDirectory) "${f.listFiles()?.size?:0} éléments • ${android.text.format.DateFormat.format("dd MMM", f.lastModified())}" else "${f.length()/1024} Ko • ${android.text.format.DateFormat.format("dd MMM yyyy", f.lastModified())}"
            h.itemView.setOnClickListener{
                if(f.isDirectory) load(f)
                else{
                    try{
                        val uri = FileProvider.getUriForFile(this@FileManagerActivity, "$packageName.provider", f)
                        val mime = when(ext){ "jpg","jpeg"->"image/jpeg"; "png"->"image/png"; "pdf"->"application/pdf"; "mp4"->"video/mp4"; "mp3"->"audio/mpeg"; else->"*/*" }
                        val intent = Intent(Intent.ACTION_VIEW).apply{ setDataAndType(uri, mime); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                        startActivity(intent)
                    }catch(e:Exception){ Toast.makeText(this@FileManagerActivity, f.absolutePath,0).show() }
                }
            }
            h.btnCopy.setOnClickListener{ clipboard=f; isCut=false; Toast.makeText(this@FileManagerActivity,"Copié: ${f.name}",0).show() }
            h.btnCut.setOnClickListener{ clipboard=f; isCut=true; Toast.makeText(this@FileManagerActivity,"Coupé: ${f.name}",0).show() }
            h.btnMore.setOnClickListener{
                val opts = arrayOf("Renommer","Partager","Supprimer","Détails")
                AlertDialog.Builder(this@FileManagerActivity).setTitle(f.name).setItems(opts){_,which->
                    when(which){
                        0->renameFile(f)
                        1->shareFile(f)
                        2->{ AlertDialog.Builder(this@FileManagerActivity).setTitle("Supprimer ${f.name}?").setPositiveButton("Supprimer"){_,_-> try{ if(f.isDirectory) f.deleteRecursively() else f.delete(); load(currentPath) }catch(e:Exception){} }.setNegativeButton("Annuler",null).show() }
                        3-> AlertDialog.Builder(this@FileManagerActivity).setTitle("Détails").setMessage("Nom: ${f.name}\nChemin: ${f.absolutePath}\nTaille: ${f.length()} octets\nModifié: ${java.util.Date(f.lastModified())}\nDossier: ${f.isDirectory}").show()
                    }
                }.show()
            }
        }
    }
}
