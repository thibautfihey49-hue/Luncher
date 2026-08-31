package com.luncher.files

import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.luncher.util.GlassUtil
import java.io.File

class FilesActivity: AppCompatActivity(){
    private lateinit var list:LinearLayout
    private lateinit var path:TextView
    private var cur:File = Environment.getExternalStorageDirectory()
    override fun onCreate(b:Bundle?){
        super.onCreate(b)
        val root=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; background=GlassUtil.bg(); setPadding(28,90,28,20)}
        root.addView(TextView(this).apply{ text="Fichiers"; textSize=28f; setTextColor(Color.parseColor("#111827")); typeface=android.graphics.Typeface.DEFAULT_BOLD})
        path=TextView(this).apply{ textSize=12f; setTextColor(Color.parseColor("#9CA3AF")); setPadding(0,8,0,16)}
        root.addView(path)
        val top=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL}
        top.addView(TextView(this).apply{ text="⬆️"; gravity=Gravity.CENTER; background=GlassUtil.card(); setPadding(28,14,28,14); setOnClickListener{ cur.parentFile?.let{ cur=it; refresh()}}})
        top.addView(TextView(this).apply{ text="🏠"; gravity=Gravity.CENTER; background=GlassUtil.card(); setPadding(0,14,0,14); layoutParams=LinearLayout.LayoutParams(0,-2,1f).apply{ setMargins(8,0,8,0)}; setOnClickListener{ cur=Environment.getExternalStorageDirectory(); refresh()}})
        top.addView(TextView(this).apply{ text="⬇️"; gravity=Gravity.CENTER; background=GlassUtil.card(); setPadding(28,14,28,14); setOnClickListener{ cur=File(Environment.getExternalStorageDirectory(),"Download"); refresh()}})
        root.addView(top)
        val scroll=ScrollView(this); list=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; setPadding(0,16,0,0)}; scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(-1,0,1f))
        setContentView(root); refresh()
    }
    private fun refresh(){
        path.text=cur.absolutePath
        list.removeAllViews()
        cur.listFiles()?.sortedWith(compareBy({!it.isDirectory},{it.name.lowercase()}))?.take(200)?.forEach{ f ->
            if(f.name.startsWith(".") && f.name!="..") return@forEach
            val row=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL; background=GlassUtil.card(); setPadding(16,14,16,14); layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,0,0,8)}}
            row.addView(TextView(this).apply{ text=if(f.isDirectory)"📁" else "📄"; textSize=20f; background=GlassUtil.searchBg(); setPadding(18,12,18,12); layoutParams=LinearLayout.LayoutParams(88,88).apply{ setMargins(0,0,12,0)}; gravity=Gravity.CENTER})
            val col=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; layoutParams=LinearLayout.LayoutParams(0,-2,1f)}
            col.addView(TextView(this).apply{ text=f.name; setTextColor(Color.parseColor("#111827")); typeface=android.graphics.Typeface.create("sans-serif-medium",0)})
            col.addView(TextView(this).apply{ text=if(f.isDirectory)"${f.listFiles()?.size?:0} fichiers" else "${f.length()/1024} KB"; textSize=12f; setTextColor(Color.parseColor("#9CA3AF"))})
            row.addView(col)
            row.setOnClickListener{ if(f.isDirectory){ cur=f; refresh()} }
            list.addView(row)
        }
    }
}
