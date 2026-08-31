package com.thibautfihey.luncher

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class ThemeSettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val logFile = File(filesDir, "luncher_debug.txt")
        fun d(msg:String){ Log.e("LUNCHER_DEBUG", msg); try{ logFile.appendText("$msg\n")}catch(_:Exception){} }

        try {
            d("ThemeSettings start")
            val scroll = ScrollView(this)
            val container = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(32,32,32,32)
                setBackgroundColor(Color.parseColor("#121212"))
            }

            container.addView(TextView(this).apply {
                text = "Luncher Themes\nAppui pour appliquer\n(long press = debug)"
                setTextColor(Color.WHITE)
                textSize = 18f
                setPadding(0,0,0,30)
            })

            // affiche dernier log si existe
            if(logFile.exists()){
                container.addView(TextView(this).apply {
                    text = "Debug log:\n${logFile.readText().take(1000)}"
                    setTextColor(Color.YELLOW)
                    textSize = 12f
                    setPadding(0,0,0,20)
                })
            }

            val themes = listOf(
                "Amoled Black" to "#000000",
                "Midnight" to "#0A1931",
                "Forest" to "#0B3D20",
                "Crimson" to "#8B0000",
                "Orange" to "#FF4500",
                "Purple" to "#4B0082",
                "Teal" to "#008080",
                "Gray" to "#2F3640"
            )

            themes.forEach { (name, col) ->
                val btn = Button(this).apply {
                    text = name
                    try{ setBackgroundColor(Color.parseColor(col)) }catch(_:Exception){}
                    setTextColor(Color.WHITE)
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 180).apply{ setMargins(0,0,0,20) }
                    setOnClickListener {
                        try{
                            d("apply $name $col")
                            getSharedPreferences("luncher", MODE_PRIVATE).edit().putString("bg", col).putString("theme_name", name).apply()
                            Toast.makeText(this@ThemeSettingsActivity, "$name appliqué", Toast.LENGTH_LONG).show()
                            // relance launcher
                            startActivity(packageManager.getLaunchIntentForPackage(packageName))
                        }catch(e:Exception){
                            d("apply err ${e.message} ${e.stackTraceToString()}")
                            Toast.makeText(this@ThemeSettingsActivity, "Err: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                    setOnLongClickListener {
                        Toast.makeText(this@ThemeSettingsActivity, "DEBUG: bg=${getSharedPreferences("luncher",MODE_PRIVATE).getString("bg","none")}", Toast.LENGTH_LONG).show()
                        true
                    }
                }
                container.addView(btn)
            }

            val clearLog = Button(this).apply {
                text = "Effacer debug log"
                setOnClickListener { logFile.delete(); Toast.makeText(this@ThemeSettingsActivity, "log effacé", Toast.LENGTH_SHORT).show(); recreate() }
            }
            container.addView(clearLog)

            scroll.addView(container)
            setContentView(scroll)
            d("ThemeSettings ok")

        } catch(e:Exception){
            val err = "THEME CRASH: ${e.message}\n${e.stackTraceToString()}"
            d(err)
            val tv = TextView(this).apply { text = err.take(3000); setTextColor(Color.RED); setBackgroundColor(Color.BLACK); setPadding(20,20,20,20) }
            setContentView(tv)
        }
    }
}
