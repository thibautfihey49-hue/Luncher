package com.luncher

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.thibautfihey.luncher.ThemeSettingsActivity

class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_launcher)
            val root = findViewById<View>(android.R.id.content)
            root.setOnLongClickListener {
                try {
                    startActivity(Intent(this, ThemeSettingsActivity::class.java))
                } catch(e:Exception){
                    Toast.makeText(this, "Launch err: ${e.message}\n${e.stackTraceToString().take(1000)}", Toast.LENGTH_LONG).show()
                }
                true
            }
            Toast.makeText(this, "APPUI LONG = DEBUG THEMES", Toast.LENGTH_SHORT).show()
        } catch(e:Exception){
            Toast.makeText(this, "Launcher err: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
