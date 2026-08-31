package com.luncher.ui

import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.luncher.util.GlassUtil

class ThemeActivity: AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        val root=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; setPadding(32,90,32,32); background=GlassUtil.bg()}
        root.addView(TextView(this).apply{ text="Thème clair actif"; textSize=24f; setTextColor(Color.parseColor("#111827")); typeface=android.graphics.Typeface.DEFAULT_BOLD})
        root.addView(TextView(this).apply{ text="Glass Light Modern arrondi"; setTextColor(Color.parseColor("#6B7280")); setPadding(0,8,0,0)})
        setContentView(root)
    }
}
