package com.luncher.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.luncher.util.GlassUtil

class LauncherActivity: AppCompatActivity(){
    override fun onCreate(b:Bundle?){
        super.onCreate(b)
        val root=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL
            background=GlassUtil.bgLiquid(null)
            setPadding(50,120,50,50)
        }
        root.addView(TextView(this).apply{
            text="Luncher SAFE 36\n\nSi tu vois ce texte,\nle crash est REPARE"; textSize=22f; setTextColor(Color.WHITE)
        })
        root.addView(Button(this).apply{
            text="Paramètres"
            setOnClickListener{ startActivity(Intent(this@LauncherActivity, SettingsActivity::class.java)) }
        })
        setContentView(root)
    }
}
