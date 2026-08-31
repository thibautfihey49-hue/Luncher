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
        setContentView(R.layout.activity_launcher)

        // Hook clé wrench -> ouvre themes
        try {
            val root = findViewById<View>(android.R.id.content)
            root.post {
                // cherche toutes les vues et rend la wrench cliquable
                fun hook(v: View) {
                    if (v is android.view.ViewGroup) {
                        for (i in 0 until v.childCount) hook(v.getChildAt(i))
                    }
                    // si c'est en haut à droite (wrench)
                    val loc = IntArray(2)
                    v.getLocationOnScreen(loc)
                    if (loc[1] < 400 && loc[0] > 500) {
                        v.isClickable = true
                        v.setOnClickListener {
                            startActivity(Intent(this, ThemeSettingsActivity::class.java))
                        }
                    }
                }
                hook(root)
            }
        } catch(e:Exception){
            Toast.makeText(this, "Hook err: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
