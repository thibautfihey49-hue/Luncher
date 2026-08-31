package com.thibautfihey.luncher

import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.thibautfihey.luncher.theme.ThemeRepository

class ThemeSettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val themes = ThemeRepository.loadThemes(this)
        val selected = ThemeRepository.getSelected(this)

        val scroll = ScrollView(this).apply {
            setBackgroundColor(Color.parseColor(selected.bgColor))
        }
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32,32,32,32)
        }

        val title = TextView(this).apply {
            text = "Thèmes (${themes.size})"
            textSize = 24f
            setTextColor(Color.parseColor(selected.textColor))
            setPadding(0,0,0,32)
        }
        container.addView(title)

        themes.forEach { theme ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(24,24,24,24)
                val isSel = theme.id == selected.id
                setBackgroundColor(if(isSel) Color.parseColor("#333333") else Color.parseColor(theme.glassColor))
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(0,0,0,16) }
                isClickable = true
                isFocusable = true
            }
            val texts = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f) }
            texts.addView(TextView(this).apply { text = theme.name + if(theme.id==selected.id) " ✓ ACTIF" else ""; setTextColor(Color.WHITE); textSize=16f })
            texts.addView(TextView(this).apply { text = theme.desc; setTextColor(Color.LTGRAY); textSize=12f })

            val btn = Button(this).apply {
                text = if(theme.id==selected.id) "Actif" else "Appliquer"
                isEnabled = theme.id!= selected.id
                setOnClickListener {
                    ThemeRepository.apply(this@ThemeSettingsActivity, theme.id)
                    Toast.makeText(this@ThemeSettingsActivity, "${theme.name} appliqué - redémarre Luncher", Toast.LENGTH_LONG).show()
                    finish()
                    startActivity(intent)
                }
            }
            card.addView(texts)
            card.addView(btn)
            card.setOnClickListener { btn.performClick() }
            container.addView(card)
        }

        scroll.addView(container)
        setContentView(scroll)
    }
}
