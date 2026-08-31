package com.luncher.ui

import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.luncher.util.GlassUtil

class SettingsActivity: AppCompatActivity(){
    override fun onCreate(b:Bundle?){
        super.onCreate(b)
        val root=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; background=GlassUtil.bgLiquid(); setPadding(32,90,32,32)}
        root.addView(TextView(this).apply{ text="Liquid Glass Settings"; textSize=22f; setTextColor(Color.parseColor("#111827")); typeface=android.graphics.Typeface.DEFAULT_BOLD})
        val items = listOf("Liquid Glass","Desktop Icon","Drawer Icon","Home screen style","Desktop","App drawer","Dock","Folder","Theme & Icon","Notification badges")
        items.forEach{ name ->
            val row = LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL; background=GlassUtil.liquidCard(); setPadding(20,18,20,18); layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,12,0,0)}}
            row.addView(TextView(this).apply{ text=name; setTextColor(Color.parseColor("#111827")); layoutParams=LinearLayout.LayoutParams(0,-2,1f)})
            row.addView(TextView(this).apply{ text="›"; setTextColor(Color.parseColor("#9CA3AF"))})
            root.addView(row)
        }
        setContentView(root)
    }
}
