package com.thibautfihey.luncher

import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.luncher.util.ThemeUtil

class ThemeSettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scroll = ScrollView(this)
        val col = LinearLayout(this).apply{
            orientation = LinearLayout.VERTICAL
            setPadding(32,60,32,32)
            setBackgroundColor(Color.parseColor("#121212"))
        }
        col.addView(TextView(this).apply{ text="Choisis un thème"; setTextColor(Color.WHITE); textSize=22f; setPadding(0,0,0,30)})

        val themes = listOf(
            "#F5F5F7" to "Luncher Blanc (original)",
            "#000000" to "Amoled Black",
            "#1A1A2E" to "Midnight Blue",
            "#0B3D20" to "Forest",
            "#FFF8E1" to "Crème",
            "#E3F2FD" to "Bleu clair",
            "#FFEBEE" to "Rose clair",
            "#212121" to "Dark Gray"
        )

        themes.forEach { (hex, name) ->
            val btn = Button(this).apply{
                text = name
                try{
                    setBackgroundColor(Color.parseColor(hex))
                    setTextColor(if(ThemeUtil.isDark(Color.parseColor(hex))) Color.WHITE else Color.BLACK)
                }catch(_:Exception){}
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 160).apply{ setMargins(0,0,0,20) }
                setOnClickListener{
                    ThemeUtil.saveBg(this@ThemeSettingsActivity, hex)
                    Toast.makeText(this@ThemeSettingsActivity, "$name appliqué", Toast.LENGTH_SHORT).show()
                    finish() // onResume du Launcher va reconstruire avec la bonne couleur
                }
            }
            col.addView(btn)
        }
        scroll.addView(col)
        setContentView(scroll)
    }
}
