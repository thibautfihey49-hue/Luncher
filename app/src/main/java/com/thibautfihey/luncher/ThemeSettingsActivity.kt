package com.thibautfihey.luncher
import android.content.Intent
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
        val scroll = ScrollView(this).apply { setBackgroundColor(Color.parseColor(selected.bgColor)) }
        val container = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32,32,32,32) }
        container.addView(TextView(this).apply { text = "Themes (${themes.size})"; textSize = 24f; setTextColor(Color.WHITE); setPadding(0,0,0,32) })
        themes.forEach { theme ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL; setPadding(24,24,24,24)
                setBackgroundColor(if(theme.id==selected.id) Color.DKGRAY else Color.parseColor("#222222"))
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply { setMargins(0,0,0,16) }
            }
            val texts = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f) }
            texts.addView(TextView(this).apply { text = theme.name + (if(theme.id==selected.id) " ✓" else ""); setTextColor(Color.WHITE) })
            val btn = Button(this).apply {
                text = if(theme.id==selected.id) "Actif" else "Appliquer"
                setOnClickListener {
                    ThemeRepository.apply(this@ThemeSettingsActivity, theme.id)
                    Toast.makeText(this@ThemeSettingsActivity, "${theme.name} appliqué", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            card.addView(texts); card.addView(btn); card.setOnClickListener { btn.performClick() }
            container.addView(card)
        }
        scroll.addView(container); setContentView(scroll)
    }
}
