package com.luncher
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tv = TextView(this)
        tv.text = "Luncher V37 OK - si tu vois ca, le manifest marche"
        tv.textSize = 24f
        tv.setPadding(40,200,40,40)
        setContentView(tv)
        // test ouverture Files après 1 sec
        tv.postDelayed({
            try { startActivity(android.content.Intent(this, FileManagerActivity::class.java)) } catch(e:Exception){ tv.text = "Erreur Files: "+e.message }
        }, 1500)
    }
}
