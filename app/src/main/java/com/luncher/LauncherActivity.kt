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

        // LONG PRESS = THEMES (100% fiable)
        try {
            val root = findViewById<View>(android.R.id.content)
            root.post {
                root.setOnLongClickListener {
                    startActivity(Intent(this, ThemeSettingsActivity::class.java))
                    true
                }
                // aussi la clé wrench si elle existe
                fun hook(v: View){
                    if(v is android.view.ViewGroup){
                        for(i in 0 until v.childCount) hook(v.getChildAt(i))
                    }
                    try{
                        v.setOnLongClickListener{
                            startActivity(Intent(this, ThemeSettingsActivity::class.java))
                            true
                        }
                    }catch(_:Exception){}
                }
                hook(root)
                Toast.makeText(this, "Appui long = Themes", Toast.LENGTH_SHORT).show()
            }
        } catch(e:Exception){}
    }
}
