package com.luncher.ui

import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.luncher.util.GlassUtil

class ThemeActivity: AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        val t = GlassUtil.get(this)
        val root=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; setPadding(32,80,32,32); background=GlassUtil.bg(t)}
        root.addView(TextView(this).apply{ text="Themes"; textSize=28f; setTextColor(Color.WHITE); typeface=android.graphics.Typeface.DEFAULT_BOLD})
        root.addView(TextView(this).apply{ text="Midnight Glass actif"; setTextColor(Color.parseColor("#9AA0C0")); setPadding(0,12,0,0)})
        setContentView(root)
    }
}
