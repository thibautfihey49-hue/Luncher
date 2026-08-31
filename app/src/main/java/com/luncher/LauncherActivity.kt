package com.luncher

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_launcher)
            val root = findViewById<View>(android.R.id.content)

            // hook clé wrench + appui long = themes
            fun openThemes(){
                try {
                    val i = Intent()
                    i.setClassName("com.thibautfihey.luncher", "com.thibautfihey.luncher.ThemeSettingsActivity")
                    startActivity(i)
                } catch(e:Exception){
                    Toast.makeText(this, "Launch err: ${e.message}\n${e.stackTraceToString().take(1500)}", Toast.LENGTH_LONG).show()
                }
            }

            root.post {
                root.setOnLongClickListener { openThemes(); true }
                fun hook(v: View){
                    if(v is android.view.ViewGroup){
                        for(i in 0 until v.childCount) hook(v.getChildAt(i))
                    }
                    // wrench en haut droite
                    try{
                        val loc = IntArray(2); v.getLocationOnScreen(loc)
                        if(loc[1] < 600 && loc[0] > 800){
                            v.isClickable = true
                            v.setOnClickListener { openThemes() }
                            v.setOnLongClickListener { openThemes(); true }
                        }
                    }catch(_:Exception){}
                }
                hook(root)
            }
            Toast.makeText(this, "Clé ou appui long = Themes", Toast.LENGTH_SHORT).show()

        } catch(e:Exception){
            Toast.makeText(this, "Launcher err: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
