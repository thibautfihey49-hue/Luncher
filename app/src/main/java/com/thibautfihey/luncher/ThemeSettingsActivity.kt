package com.thibautfihey.luncher

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class ThemeSettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val logFile = File(filesDir, "crash_log.txt")
        fun log(msg:String){
            Log.e("THEME_DEBUG", msg)
            try{ logFile.appendText("$msg\n") }catch(_:Exception){}
        }

        try {
            log("onCreate start")
            val layout = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(30,30,30,30)
                setBackgroundColor(Color.BLACK)
            }

            val tv = TextView(this).apply {
                text = "DEBUG THEME - Si tu vois ça, ça crash pas ici"
                setTextColor(Color.GREEN)
                textSize = 18f
            }
            layout.addView(tv)

            // test chaque étape
            try {
                log("step1: prefs")
                val prefs = getSharedPreferences("luncher", MODE_PRIVATE)
                log("prefs ok: ${prefs.all}")

                log("step2: creation bouton")
                val btn = Button(this).apply { text = "Test couleur #000000" }
                btn.setOnClickListener {
                    try {
                        log("click appliquer")
                        prefs.edit().putString("bg","#000000").apply()
                        Toast.makeText(this@ThemeSettingsActivity, "OK APPLIQUÉ", Toast.LENGTH_LONG).show()
                        log("appliqué ok")
                    } catch(e:Exception){
                        val err = "CLICK ERR: ${e.message}\n${e.stackTraceToString()}"
                        log(err)
                        Toast.makeText(this@ThemeSettingsActivity, err.take(500), Toast.LENGTH_LONG).show()
                    }
                }
                layout.addView(btn)

                // affiche log précédent s'il existe
                if(logFile.exists()){
                    layout.addView(TextView(this).apply {
                        text = "Dernier log:\n${logFile.readText().take(2000)}"
                        setTextColor(Color.YELLOW)
                        setPadding(0,50,0,0)
                    })
                }

                log("step3: setContentView")
                setContentView(layout)
                log("setContentView ok")

            } catch(e:Exception){
                val err = "INNER ERR: ${e.message}\n${e.stackTraceToString()}"
                log(err)
                layout.addView(TextView(this).apply {
                    text = err
                    setTextColor(Color.RED)
                })
                setContentView(layout)
            }

        } catch(e:Exception){
            val err = "OUTER CRASH: ${e.message}\n${e.stackTraceToString()}"
            Log.e("THEME_DEBUG", err)
            try{
                File(filesDir, "crash_log.txt").appendText(err)
            }catch(_:Exception){}
            // affiche quand même quelque chose pour pas fermer
            try {
                val tv = TextView(this).apply {
                    text = err.take(3000)
                    setTextColor(Color.RED)
                    setBackgroundColor(Color.BLACK)
                    setPadding(20,20,20,20)
                }
                setContentView(tv)
                Toast.makeText(this, "CRASH LOG: $err", Toast.LENGTH_LONG).show()
            } catch(_:Exception){
                // dernier recours
            }
        }
    }
}
