package com.luncher.ui

import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.luncher.util.GlassUtil

class ThemeActivity: AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        val root=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; setPadding(30,80,30,30); setBackgroundColor(Color.parseColor("#0A0A0A"))}
        root.addView(TextView(this).apply{ text="Glass Themes"; textSize=26f; setTextColor(Color.WHITE)})
        GlassUtil.themes.forEach{ t ->
            val card=LinearLayout(this).apply{
                orientation=LinearLayout.VERTICAL; background=GlassUtil.bg(t); setPadding(30,30,30,30)
                layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,20,0,0)}
            }
            card.addView(TextView(this).apply{ text=t.name; textSize=20f; setTextColor(Color.parseColor(t.text))})
            card.addView(Button(this).apply{ text="Appliquer"; setOnClickListener{ GlassUtil.save(this@ThemeActivity,t); finish()}})
            root.addView(card)
        }
        setContentView(ScrollView(this).apply{ addView(root)})
    }
}
