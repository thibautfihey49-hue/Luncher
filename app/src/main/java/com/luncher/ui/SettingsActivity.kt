package com.luncher.ui

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.luncher.util.GlassUtil

class SettingsActivity: AppCompatActivity(){
    private lateinit var prefs:SharedPreferences
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()){ uri: Uri? ->
        uri?.let{
            try{ contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) }catch(_:Exception){}
            prefs.edit().putString("wallpaper_uri", it.toString()).apply()
            recreate()
        }
    }
    override fun onCreate(b:Bundle?){
        super.onCreate(b)
        prefs=getSharedPreferences("luncher",0)
        val root=ScrollView(this)
        val main=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; background=GlassUtil.bgLiquid(prefs); setPadding(24,80,24,28)}

        main.addView(TextView(this).apply{ text="Paramètres"; textSize=26f; setTextColor(Color.WHITE); typeface=android.graphics.Typeface.DEFAULT_BOLD; setPadding(0,0,0,20)})

        // PERMISSIONS
        main.addView(section("Autorisations requises"))
        main.addView(actionRow("🔔 Accès notifications (WhatsApp/SMS/Gmail)", {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }))
        main.addView(actionRow("📷 Choisir fond d'écran", { pickImage.launch("image/*") }))
        main.addView(actionRow("✕ Reset fond", { prefs.edit().remove("wallpaper_uri").apply(); recreate()}))

        main.addView(section("Apparence - taille icônes & texte"))
        main.addView(sliderRow("Taille icônes", 48, 160, prefs.getInt("iconSize",92)) { v -> prefs.edit().putInt("iconSize",v).apply() })
        main.addView(sliderRow("Taille texte", 8, 16, prefs.getInt("labelSize",10)) { v -> prefs.edit().putInt("labelSize",v).apply() })
        main.addView(toggleRow("Afficher noms", prefs.getBoolean("showLabel",true)) { c -> prefs.edit().putBoolean("showLabel",c).apply() })
        main.addView(sliderRow("Transparence", 30, 100, prefs.getInt("alpha",85)) { v -> prefs.edit().putInt("alpha",v).apply() })

        main.addView(section("Thème mec"))
        main.addView(choiceRow())

        root.addView(main)
        setContentView(root)
    }
    private fun section(title:String)=TextView(this).apply{
        text=title; textSize=13f; setTextColor(Color.WHITE); typeface=android.graphics.Typeface.DEFAULT_BOLD
        background=GlassUtil.liquidCard(prefs); setPadding(20,14,20,14)
        layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,20,0,8)}
    }
    private fun sliderRow(name:String, min:Int, max:Int, cur:Int, onChange:(Int)->Unit): LinearLayout {
        val row=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; background=GlassUtil.liquidCard(prefs); setPadding(18,14,18,14); layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,0,0,8)}}
        val label=TextView(this).apply{ text="$name: $cur"; setTextColor(Color.WHITE); textSize=12f}
        val seek=SeekBar(this).apply{ this.max=max-min; progress=cur-min}
        seek.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(s:SeekBar?, p:Int, f:Boolean){ val nv=p+min; label.text="$name: $nv"; onChange(nv)}
            override fun onStartTrackingTouch(s:SeekBar?){}
            override fun onStopTrackingTouch(s:SeekBar?){}
        })
        row.addView(label); row.addView(seek); return row
    }
    private fun toggleRow(name:String, cur:Boolean, onChange:(Boolean)->Unit): LinearLayout {
        val row=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL; background=GlassUtil.liquidCard(prefs); setPadding(18,14,18,14); layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,0,0,8)}}
        row.addView(TextView(this).apply{ text=name; setTextColor(Color.WHITE); layoutParams=LinearLayout.LayoutParams(0,-2,1f)})
        val sw=Switch(this).apply{ isChecked=cur}
        sw.setOnCheckedChangeListener{ _,c -> onChange(c)}
        row.addView(sw); return row
    }
    private fun choiceRow(): LinearLayout {
        val row=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; background=GlassUtil.liquidCard(prefs); setPadding(18,14,18,14); layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,0,0,8)}}
        val cur=prefs.getString("theme","dark")!!
        listOf("black" to "⚫ AMOLED","dark" to "🌑 Dark","blue" to "🔵 Bleu nuit","grey" to "⚙️ Gris").forEach{ (v,l) ->
            val rb=RadioButton(this).apply{ text=l; isChecked=(v==cur); setTextColor(Color.WHITE); textSize=12f}
            rb.setOnClickListener{ prefs.edit().putString("theme",v).remove("wallpaper_uri").apply(); recreate()}
            row.addView(rb)
        }
        return row
    }
    private fun actionRow(name:String, action:()->Unit)=TextView(this).apply{
        text=name; textSize=12f; setTextColor(Color.WHITE)
        background=GlassUtil.liquidCard(prefs); setPadding(20,14,20,14)
        layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,0,0,8)}
        setOnClickListener{ action()}
    }
}
