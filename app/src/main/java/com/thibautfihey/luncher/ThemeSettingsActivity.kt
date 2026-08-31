package com.thibautfihey.luncher

import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.luncher.util.ThemeUtil
import java.io.File

class ThemeSettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try{
            ThemeUtil.log(this, "ThemeSettings start")
            val scroll = ScrollView(this)
            val col = LinearLayout(this).apply{ orientation = LinearLayout.VERTICAL; setPadding(32,32,32,32); setBackgroundColor(Color.parseColor("#121212")) }

            col.addView(TextView(this).apply{ text="Themes - clique pour appliquer"; setTextColor(Color.WHITE); textSize=20f; setPadding(0,0,0,20)})

            val logFile = File(filesDir, "luncher_debug.txt")
            if(logFile.exists()){
                col.addView(TextView(this).apply{ text="DEBUG:\n${logFile.readText().take(2000)}"; setTextColor(Color.YELLOW); textSize=11f; setPadding(0,0,0,20)})
                col.addView(Button(this).apply{ text="Effacer log"; setOnClickListener{ logFile.delete(); recreate() } })
            }

            listOf("#000000" to "Black", "#0A1931" to "Midnight", "#0B3D20" to "Forest", "#8B0000" to "Crimson", "#FF4500" to "Orange", "#4B0082" to "Purple", "#008080" to "Teal", "#2F3640" to "Gray", "#FFFFFF" to "White").forEach { (hex,name) ->
                col.addView(Button(this).apply{
                    text="$name $hex"; try{ setBackgroundColor(Color.parseColor(hex)); setTextColor(if(hex=="#FFFFFF") Color.BLACK else Color.WHITE) }catch(_:Exception){}
                    setOnClickListener{
                        try{ ThemeUtil.saveBg(this@ThemeSettingsActivity, hex); Toast.makeText(this@ThemeSettingsActivity, "$name appliqué", Toast.LENGTH_SHORT).show(); finish() }
                        catch(e:Exception){ ThemeUtil.log(this@ThemeSettingsActivity, "apply err ${e.message}"); Toast.makeText(this@ThemeSettingsActivity, e.message, Toast.LENGTH_LONG).show() }
                    }
                })
            }
            scroll.addView(col)
            setContentView(scroll)
        }catch(e:Exception){
            ThemeUtil.log(this, "ThemeSettings crash ${e.stackTraceToString()}")
            setContentView(TextView(this).apply{ text="Crash theme:\n${e.message}\n${e.stackTraceToString().take(2000)}"; setTextColor(Color.RED)})
        }
    }
}
