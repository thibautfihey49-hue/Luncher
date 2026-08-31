package com.luncher

import android.os.Bundle
import android.os.Environment
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.luncher.util.ThemeUtil
import java.io.File

class FileManagerActivity: AppCompatActivity(){
    private lateinit var list:LinearLayout
    private lateinit var pathView:TextView
    private var currentDir:File = Environment.getExternalStorageDirectory()

    override fun onCreate(b:Bundle?){
        super.onCreate(b)
        val t=ThemeUtil.get(this)
        val root=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; background=ThemeUtil.drawable(t); setPadding(30,70,30,20)}

        pathView=TextView(this).apply{ text=currentDir.absolutePath; textSize=12f}
        root.addView(pathView)

        val top=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL}
        top.addView(Button(this).apply{ text=".."; setOnClickListener{ currentDir.parentFile?.let{ currentDir=it; refresh()}}})
        top.addView(Button(this).apply{ text="Home"; setOnClickListener{ currentDir=Environment.getExternalStorageDirectory(); refresh()}})
        root.addView(top)

        val scroll=ScrollView(this); list=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL}; scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(-1,0,1f))

        setContentView(root)
        refresh()
    }

    private fun refresh(){
        pathView.text=currentDir.absolutePath
        list.removeAllViews()
        val files=currentDir.listFiles()?.sortedWith(compareBy({!it.isDirectory}, {it.name.lowercase()}))?:emptyList()
        files.take(200).forEach{ f ->
            val row=TextView(this).apply{
                text="${if(f.isDirectory) "📁" else "📄"} ${f.name}"
                textSize=16f; setPadding(20,24,20,24)
                setOnClickListener{
                    if(f.isDirectory){ currentDir=f; refresh()}
                    else Toast.makeText(this@FileManagerActivity, f.absolutePath, Toast.LENGTH_SHORT).show()
                }
            }
            list.addView(row)
        }
    }
}
