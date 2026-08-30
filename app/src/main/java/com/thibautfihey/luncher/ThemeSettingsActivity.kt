package com.thibautfihey.luncher

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.thibautfihey.luncher.theme.ThemeRepository

class ThemeSettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val theme = ThemeRepository.getSelected(this)
        // applique le fond selon theme.bgColor
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_theme_settings)
    }
}
