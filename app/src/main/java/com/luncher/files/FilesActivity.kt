package com.luncher.files

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.luncher.util.GlassUtil
import java.io.File

class FilesActivity: AppCompatActivity(){
    private lateinit var list:LinearLayout
    private lateinit var pathView:TextView
    private var current:File = Environment.getExternalStorageDirectory()
    private var showHidden=false

    override fun onCreate(b:Bundle?){
        super.onCreate(b)
        val t=GlassUtil.get(this)
        val root=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; background=GlassUtil.bg(t); setPadding(20,80,20,20)}

        if(Build.VERSION.SDK_INT>=30 &&!Environment.isExternalStorageManager()){
            root.addView(Button(this).apply{ text="Autoriser accès total fichiers"; setOnClickListener{ startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:$packageName")))}})
        }

        pathView=TextView(this).apply{ text=current.absolutePath; textSize=12f}
        root.addView(pathView)

        val top=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL}
        top.addView(Button(this).apply{ text="⬆️"; setOnClickListener{ current.parentFile?.let{ current=it; refresh()}}})
        top.addView(Button(this).apply{ text="🏠"; setOnClickListener{ current=Environment.getExternalStorageDirectory(); refresh()}})
        top.addView(Button(this).apply{ text="📱 DCIM"; setOnClickListener{ current=File(Environment.getExternalStorageDirectory(),"DCIM"); refresh()}})
        top.addView(Button(this).apply{ text="⬇️ Downloads"; setOnClickListener{ current=File(Environment.getExternalStorageDirectory(),"Download"); refresh()}})
        root.addView(top)

        val info=TextView(this).apply{ text="Dossiers système: /sdcard, /Android, /data - appui long pour infos"}
        root.addView(info)

        val scroll=ScrollView(this); list=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL}; scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(-1,0,1f))

        setContentView(root)
        refresh()
    }

    private fun refresh(){
        pathView.text="${current.absolutePath} (${current.listFiles()?.size?:0} items)"
        list.removeAllViews()
        val files=current.listFiles()?.sortedWith(compareBy({!it.isDirectory}, {it.name.lowercase()}))?:return
        files.forEach{ f ->
            if(!showHidden && f.name.startsWith(".")) return@forEach
            val row=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL; setPadding(16,14,16,14); background=GlassUtil.glassCard(14f); layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,4,0,4)}}
            val icon=TextView(this).apply{ text=if(f.isDirectory) "📁" else when(f.extension.lowercase()){ "jpg","png","jpeg"->"🖼️"; "mp4","mkv"->"🎬"; "mp3","wav"->"🎵"; "pdf"->"📕"; "apk"->"📦"; else->"📄"}; textSize=20f}
            val name=TextView(this).apply{ text="${f.name}\n${if(f.isDirectory) "${f.listFiles()?.size?:0} items" else "${f.length()/1024} KB"}"; layoutParams=LinearLayout.LayoutParams(0,-2,1f); setPadding(12,0,12,0)}
            row.addView(icon); row.addView(name)
            row.setOnClickListener{ if(f.isDirectory){ current=f; refresh()} else { try{ val intent=Intent(Intent.ACTION_VIEW); val uri=Uri.fromFile(f); intent.setDataAndType(uri, "*/*"); intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); startActivity(Intent.createChooser(intent,"Ouvrir"))}catch(e:Exception){ Toast.makeText(this,f.absolutePath,Toast.LENGTH_SHORT).show()}}}
            row.setOnLongClickListener{ Toast.makeText(this,"${f.absolutePath}\n${f.length()} bytes\nModif: ${java.util.Date(f.lastModified())}",Toast.LENGTH_LONG).show(); true}
            list.addView(row)
        }
    }
}
