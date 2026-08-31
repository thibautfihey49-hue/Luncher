package com.luncher.ui

import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.luncher.util.GlassUtil

class ThemeActivity: AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        val root=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; setPadding(32,80,32,32); background=GlassUtil.bg(GlassUtil.get(this))}
        root.addView(TextView(this).apply{ text="Themes"; textSize=28f; setTextColor(Color.WHITE)})
        setContentView(root)
    }
}
