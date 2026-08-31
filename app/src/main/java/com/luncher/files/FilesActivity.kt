package com.luncher.files

import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.luncher.util.GlassUtil
import java.io.File

class FilesActivity: AppCompatActivity(){
    private lateinit var list:LinearLayout
    private var cur:File = Environment.getExternalStorageDirectory()
    override fun onCreate(b:Bundle?){
        super.onCreate(b)
        val prefs=getSharedPreferences("luncher",0)
        val root=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; background=GlassUtil.bgLiquid(prefs); setPadding(28,90,28,20)}
        root.addView(TextView(this).apply{ text="Fichiers"; textSize=24f; setTextColor(Color.parseColor("#111827")); typeface=android.graphics.Typeface.DEFAULT_BOLD})
        val scroll=ScrollView(this)
        list=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; setPadding(0,16,0,0)}
        scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(-1,0,1f))
        setContentView(root); refresh()
    }
    private fun refresh(){
        val prefs=getSharedPreferences("luncher",0)
        list.removeAllViews()
        cur.listFiles()?.sortedBy{ it.name }?.take(150)?.forEach{ f ->
            val row=LinearLayout(this).apply{
                orientation=LinearLayout.HORIZONTAL
                background=GlassUtil.liquidCard(prefs)
                setPadding(16,12,16,12)
                layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,0,0,8)}
            }
            row.addView(TextView(this).apply{ text=if(f.isDirectory)"📁" else "📄"; setPadding(12,0,12,0)})
            row.addView(TextView(this).apply{ text=f.name; setTextColor(Color.parseColor("#111827")); layoutParams=LinearLayout.LayoutParams(0,-2,1f)})
            row.setOnClickListener{ if(f.isDirectory){ cur=f; refresh()}}
            list.addView(row)
        }
    }
}
