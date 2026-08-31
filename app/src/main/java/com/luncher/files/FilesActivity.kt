package com.luncher.files

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.luncher.util.GlassUtil
import java.io.File

class FilesActivity: AppCompatActivity(){
    private lateinit var list:LinearLayout
    private lateinit var pathView:TextView
    private var current:File = Environment.getExternalStorageDirectory()

    override fun onCreate(b:Bundle?){
        super.onCreate(b)
        val t=GlassUtil.get(this)
        val root=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; background=GlassUtil.bg(t); setPadding(24,80,24,20)}

        pathView=TextView(this).apply{ text=current.absolutePath; textSize=13f; setTextColor(Color.parseColor(t.subText)); setPadding(0,0,0,12)}
        root.addView(pathView)

        val top=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL}
        top.addView(Button(this).apply{ text="⬆️"; setOnClickListener{ current.parentFile?.let{ current=it; refresh()}}})
        top.addView(Button(this).apply{ text="🏠 Home"; layoutParams=LinearLayout.LayoutParams(0,-2,1f).apply{ setMargins(8,0,8,0)}; setOnClickListener{ current=Environment.getExternalStorageDirectory(); refresh()}})
        top.addView(Button(this).apply{ text="⬇️ DL"; setOnClickListener{ current=File(Environment.getExternalStorageDirectory(),"Download"); refresh()}})
        root.addView(top)

        val scroll=ScrollView(this); list=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL}; scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(-1,0,1f))
        setContentView(root)
        refresh()
    }

    private fun refresh(){
        pathView.text="${current.absolutePath} • ${current.listFiles()?.size?:0} items"
        list.removeAllViews()
        val files=current.listFiles()?.sortedWith(compareBy({!it.isDirectory}, {it.name.lowercase()}))?:return
        files.take(300).forEach{ f ->
            if(f.name.startsWith(".") && f.name!="..") return@forEach
            val row=LinearLayout(this).apply{
                orientation=LinearLayout.HORIZONTAL; gravity=android.view.Gravity.CENTER_VERTICAL
                background=GlassUtil.cardSolid(); setPadding(20,18,20,18)
                layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,0,0,10)}
            }
            val icon=TextView(this).apply{
                text=if(f.isDirectory) "📁" else when(f.extension.lowercase()){ "jpg","png"->"🖼️"; "mp4"->"🎬"; "mp3"->"🎵"; "pdf"->"📕"; else->"📄"}; textSize=22f
            }
            val name=TextView(this).apply{
                text="${f.name}\n${if(f.isDirectory) "${f.listFiles()?.size?:0} fichiers" else "${f.length()/1024} KB"}"
                setTextColor(Color.WHITE); setPadding(16,0,16,0)
                typeface=android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
                layoutParams=LinearLayout.LayoutParams(0,-2,1f)
            }
            row.addView(icon); row.addView(name)
            row.setOnClickListener{ if(f.isDirectory){ current=f; refresh()} else Toast.makeText(this,f.name,Toast.LENGTH_SHORT).show()}
            list.addView(row)
        }
    }
}
