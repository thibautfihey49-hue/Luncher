package com.thibautfihey.luncher
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.luncher.util.ThemeUtil
import java.io.File
class ThemeSettingsActivity: AppCompatActivity(){
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        val scroll=ScrollView(this)
        val col=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; setPadding(32,70,32,32); setBackgroundColor(Color.parseColor("#121212"))}
        col.addView(TextView(this).apply{ text="Thèmes Luncher"; setTextColor(Color.WHITE); textSize=24f; setPadding(0,0,0,10)})
        col.addView(TextView(this).apply{ text="Le fond change instantanément"; setTextColor(Color.GRAY); setPadding(0,0,0,30)})

        val logFile=File(filesDir,"luncher_debug.txt")
        if(logFile.exists()){
            col.addView(TextView(this).apply{ text="LOG:\n${logFile.readText().take(1500)}"; setTextColor(Color.YELLOW); textSize=10f})
            col.addView(Button(this).apply{ text="Effacer log"; setOnClickListener{ logFile.delete(); recreate()}})
        }

        val themes=listOf(
            "#F5F5F7" to "Original Luncher",
            "#FFFFFF" to "Blanc pur",
            "#000000" to "Amoled Noir",
            "#121212" to "Noir doux",
            "#1A1A2E" to "Midnight",
            "#0B3D20" to "Forest",
            "#FFF8E1" to "Crème",
            "#E3F2FD" to "Bleu ciel",
            "#F3E5F5" to "Lavande",
            "#FFEBEE" to "Rose"
        )

        themes.forEach{ (hex,name) ->
            col.addView(Button(this).apply{
                text=name; layoutParams=LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,160).apply{ setMargins(0,0,0,20)}
                try{ setBackgroundColor(Color.parseColor(hex)); setTextColor(if(ThemeUtil.isDark(Color.parseColor(hex))) Color.WHITE else Color.BLACK)}catch(_:Exception){}
                setOnClickListener{ ThemeUtil.saveBg(this@ThemeSettingsActivity,hex); ThemeUtil.log(this@ThemeSettingsActivity,"theme $name $hex"); Toast.makeText(this@ThemeSettingsActivity,"$name appliqué - retour",Toast.LENGTH_SHORT).show(); finish()}
            })
        }
        scroll.addView(col); setContentView(scroll)
    }
}
