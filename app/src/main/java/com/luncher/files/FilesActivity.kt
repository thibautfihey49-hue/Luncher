package com.luncher.files

import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.luncher.util.GlassUtil
import com.luncher.util.IconUtil
import java.io.File

class FilesActivity: AppCompatActivity(){
    private lateinit var list:LinearLayout
    private lateinit var pathView:TextView
    private var current:File = Environment.getExternalStorageDirectory()

    override fun onCreate(b:Bundle?){
        super.onCreate(b)
        val t=GlassUtil.get(this)
        val root=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; background=GlassUtil.bg(t); setPadding(28,80,28,20)}

        root.addView(TextView(this).apply{ text="Files"; textSize=32f; setTextColor(Color.WHITE); typeface=android.graphics.Typeface.DEFAULT_BOLD})
        pathView=TextView(this).apply{ textSize=12f; setTextColor(Color.parseColor("#9AA0C0")); setPadding(0,8,0,16)}
        root.addView(pathView)

        val top=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL}
        top.addView(TextView(this).apply{ text="⬆️"; gravity=Gravity.CENTER; background=GlassUtil.card(100f); setPadding(28,16,28,16); setOnClickListener{ current.parentFile?.let{ current=it; refresh()}}})
        top.addView(TextView(this).apply{ text="🏠 Home"; gravity=Gravity.CENTER; background=GlassUtil.card(100f); setPadding(0,16,0,16); layoutParams=LinearLayout.LayoutParams(0,-2,1f).apply{ setMargins(8,0,8,0)}; setOnClickListener{ current=Environment.getExternalStorageDirectory(); refresh()}})
        top.addView(TextView(this).apply{ text="⬇️"; gravity=Gravity.CENTER; background=GlassUtil.card(100f); setPadding(28,16,28,16); setOnClickListener{ current=File(Environment.getExternalStorageDirectory(),"Download"); refresh()}})
        root.addView(top)

        val scroll=ScrollView(this); list=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; setPadding(0,16,0,0)}; scroll.addView(list)
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
                orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL
                background=GlassUtil.cardSolid(); setPadding(18,16,18,16)
                layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,0,0,10)}
            }
            val icon=TextView(this).apply{
                text=if(f.isDirectory) "📁" else when(f.extension.lowercase()){ "jpg","png"->"🖼️"; "mp4"->"🎬"; "mp3"->"🎵"; "pdf"->"📕"; "apk"->"📦"; else->"📄"}
                textSize=20f; gravity=Gravity.CENTER
                background=GlassUtil.card(16f); setPadding(20,16,20,16)
                layoutParams=LinearLayout.LayoutParams(96,96).apply{ setMargins(0,0,14,0)}
            }
            row.addView(icon)
            row.addView(LinearLayout(this).apply{
                orientation=LinearLayout.VERTICAL; layoutParams=LinearLayout.LayoutParams(0,-2,1f)
                addView(TextView(this@FilesActivity).apply{ text=f.name; setTextColor(Color.WHITE); typeface=android.graphics.Typeface.create("sans-serif-medium",0)})
                addView(TextView(this@FilesActivity).apply{ text=if(f.isDirectory) "${f.listFiles()?.size?:0} fichiers" else "${f.length()/1024} KB"; textSize=12f; setTextColor(Color.parseColor("#9AA0C0"))})
            })
            row.setOnClickListener{ if(f.isDirectory){ current=f; refresh()} else Toast.makeText(this,f.name,Toast.LENGTH_SHORT).show()}
            list.addView(row)
        }
    }
}
