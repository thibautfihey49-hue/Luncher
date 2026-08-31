
package com.luncher
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.content.Intent
import com.thibautfihey.luncher.ThemeSettingsActivity
import android.os.Environment
import android.os.StatFs
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class FileManagerActivity : AppCompatActivity() {
    private var currentPath = Environment.getExternalStorageDirectory().path
    private lateinit var recyclerFiles: RecyclerView
    private lateinit var tvPath: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_files)
 try { window.decorView.post { try { com.thibautfihey.luncher.attachThemeButton(window.decorView.rootView); findViewById<android.view.View>(android.R.id.content)?.setOnClickListener { startActivity(Intent(this, ThemeSettingsActivity::class.java)) } } catch(e:Exception){} } } catch(e:Exception){}
        recyclerFiles = findViewById(R.id.recyclerFiles)
        tvPath = findViewById(R.id.tvPath)
        tvPath.text = currentPath

        val tvUsed = findViewById<TextView>(R.id.tvUsed)
        val tvRem = findViewById<TextView>(R.id.tvRemaining)
        val tvPct = findViewById<TextView>(R.id.tvPercent)
        try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val total = stat.totalBytes
            val free = stat.availableBytes
            val used = total - free
            val pct = (used * 100 / total).toInt()
            tvUsed?.text = String.format("%.1f GB / %.0f GB used", used / 1e9, total / 1e9)
            tvRem?.text = String.format("Remaining: %.1f GB", free / 1e9)
            tvPct?.text = "$pct%"
        } catch (_: Exception) {}

        findViewById<View>(R.id.cardDocuments)?.setOnClickListener { openFolder(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).path) }
        findViewById<View>(R.id.cardImages)?.setOnClickListener { openFolder(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path) }
        findViewById<View>(R.id.cardVideos)?.setOnClickListener { openFolder(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).path) }
        findViewById<View>(R.id.cardAudio)?.setOnClickListener { openFolder(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).path) }
        findViewById<View>(R.id.btnBack)?.setOnClickListener { goBack() }

        checkPermAndLoad()
    }

    private fun checkPermAndLoad() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        } else {
            loadFiles(currentPath)
        }
    }

    override fun onRequestPermissionsResult(code: Int, perms: Array<String>, res: IntArray) {
        super.onRequestPermissionsResult(code, perms, res)
        loadFiles(currentPath)
    }

    private fun openFolder(path: String) {
        val f = File(path)
        if (f.exists()) {
            currentPath = path
            tvPath.text = path
            loadFiles(path)
            findViewById<View>(R.id.layoutCategories)?.visibility = View.GONE
            findViewById<View>(R.id.layoutFiles)?.visibility = View.VISIBLE
        } else {
            Toast.makeText(this, "Dossier non trouve: $path", Toast.LENGTH_SHORT).show()
        }
    }

    private fun goBack() {
        val parent = File(currentPath).parent
        if (parent!= null && findViewById<View>(R.id.layoutFiles)?.visibility == View.VISIBLE) {
            if (File(parent).path == Environment.getExternalStorageDirectory().parent) {
                findViewById<View>(R.id.layoutCategories)?.visibility = View.VISIBLE
                findViewById<View>(R.id.layoutFiles)?.visibility = View.GONE
                currentPath = Environment.getExternalStorageDirectory().path
                tvPath.text = currentPath
            } else {
                currentPath = parent
                tvPath.text = parent
                loadFiles(parent)
            }
        } else {
            finish()
        }
    }

    private fun loadFiles(path: String) {
        recyclerFiles.layoutManager = LinearLayoutManager(this)
        try {
            val dir = File(path)
            val files = dir.listFiles()?.sortedWith(compareBy({!it.isDirectory }, { it.name.lowercase() }))?: emptyList()
            if (files.isEmpty()) {
                Toast.makeText(this, "Dossier vide", Toast.LENGTH_SHORT).show()
            }
            recyclerFiles.adapter = FileAdapter(files) { file ->
                if (file.isDirectory) {
                    currentPath = file.path
                    tvPath.text = file.path
                    loadFiles(file.path)
                } else {
                    Toast.makeText(this, file.name, Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Erreur: " + e.message, Toast.LENGTH_LONG).show()
        }
    }
}

class FileAdapter(private val files: List<File>, private val onClick: (File) -> Unit) : RecyclerView.Adapter<FileAdapter.H>() {
    class H(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.itemText)
        val icon: TextView = v.findViewById(R.id.itemIcon)
    }
    override fun onCreateViewHolder(p: ViewGroup, t: Int): H {
        val v = LayoutInflater.from(p.context).inflate(R.layout.item_file, p, false)
        return H(v)
    }
    override fun getItemCount() = files.size
    override fun onBindViewHolder(h: H, pos: Int) {
        val f = files[pos]
        h.name.text = f.name
        h.icon.text = if (f.isDirectory) "📁" else "📄"
        h.itemView.setOnClickListener { onClick(f) }
    }
}
