package com.thibautfihey.luncher

import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class ThemeSettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            val scroll = ScrollView(this)
            val container = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(32,32,32,32)
            }
            container.addView(TextView(this).apply {
                text = "Themes Luncher - Appui pour appliquer"
                textSize = 20f
                setTextColor(Color.WHITE)
                setPadding(0,0,0,40)
            })

            // themes en dur, pas de fichier = pas de crash
            val themes = listOf(
                "Amoled Black" to "#000000",
                "Midnight Blue" to "#0A1931",
                "Forest Green" to "#0B3D20",
                "Crimson Red" to "#8B0000",
                "Sunset Orange" to "#FF4500",
                "Royal Purple" to "#4B0082",
                "Ocean Teal" to "#008080",
                "Slate Gray" to "#2F3640"
            )

            themes.forEach { (name, color) ->
                val btn = Button(this).apply {
                    text = name
                    try { setBackgroundColor(Color.parseColor(color)) } catch(_:Exception){}
                    setTextColor(Color.WHITE)
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(0,0,0,20) }
                    setOnClickListener {
                        try {
                            // applique via prefs
                            getSharedPreferences("luncher", MODE_PRIVATE).edit()
                                .putString("bg", color)
                                .putString("theme_name", name)
                                .apply()
                            Toast.makeText(this@ThemeSettingsActivity, "$name appliqué - relance Luncher", Toast.LENGTH_LONG).show()
                            finish()
                        } catch(e:Exception){
                            Toast.makeText(this@ThemeSettingsActivity, "Err: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                container.addView(btn)
            }

            scroll.addView(container)
            scroll.setBackgroundColor(Color.parseColor("#121212"))
            setContentView(scroll)

        } catch(e:Exception){
            // si même ça crash, affiche l'erreur
            val tv = TextView(this).apply {
                text = "Crash: ${e.message}\n${e.stackTraceToString().take(500)}"
                setTextColor(Color.RED)
            }
            setContentView(tv)
        }
    }
}
