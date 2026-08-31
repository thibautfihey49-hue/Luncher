package com.thibautfihey.luncher

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.luncher.util.ThemeUtil

class ThemeSettingsActivity: AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        val scroll=ScrollView(this)
        val col=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; setPadding(30,70,30,30); setBackgroundColor(Color.parseColor("#0A0A0A"))}
        col.addView(TextView(this).apply{ text="THEMES"; setTextColor(Color.WHITE); textSize=28f; typeface=android.graphics.Typeface.DEFAULT_BOLD})
        col.addView(TextView(this).apply{ text="Choisis un thème complet"; setTextColor(Color.GRAY); setPadding(0,10,0,30)})

        ThemeUtil.themes.forEach{ t ->
            val card=LinearLayout(this).apply{
                orientation=LinearLayout.VERTICAL; background=ThemeUtil.drawable(t)
                setPadding(28,28,28,28)
                layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,0,0,28)}
            }
            card.addView(TextView(this).apply{ text="${t.wrenchIcon} ${t.name}"; textSize=20f; setTextColor(Color.parseColor(t.textColor)); typeface=android.graphics.Typeface.DEFAULT_BOLD})
            card.addView(TextView(this).apply{ text=t.desc; setTextColor(Color.parseColor(t.secondaryText)); textSize=12f; setPadding(0,6,0,18)})
            card.addView(Button(this).apply{
                text="Appliquer"; setTextColor(Color.WHITE)
                background=GradientDrawable().apply{ cornerRadius=20f; setColor(Color.parseColor(t.accent))}
                setOnClickListener{ ThemeUtil.save(this@ThemeSettingsActivity,t); finish()}
            })
            card.setOnClickListener{ ThemeUtil.save(this@ThemeSettingsActivity,t); finish()}
            col.addView(card)
        }
        scroll.addView(col); setContentView(scroll)
    }
}
