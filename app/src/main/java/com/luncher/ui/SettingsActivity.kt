package com.luncher.ui

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.luncher.util.GlassUtil

class SettingsActivity: AppCompatActivity(){
    private lateinit var prefs:SharedPreferences
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()){ uri: Uri? ->
        uri?.let{
            try{ contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) }catch(_:Exception){}
            prefs.edit().putString("wallpaper_uri", it.toString()).apply()
            Toast.makeText(this,"Fond changé",Toast.LENGTH_SHORT).show()
            recreate()
        }
    }
    override fun onCreate(b:Bundle?){
        super.onCreate(b)
        prefs=getSharedPreferences("luncher",0)
        val root=ScrollView(this)
        val main=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; background=GlassUtil.bgLiquid(prefs); setPadding(24,80,24,28)}
        main.addView(TextView(this).apply{ text="Paramètres"; textSize=26f; setTextColor(Color.WHITE); typeface=android.graphics.Typeface.DEFAULT_BOLD; setPadding(0,0,0,20)})
        main.addView(section("Fond d'écran"))
        val wpRow=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; background=GlassUtil.liquidCard(prefs); setPadding(20,16,20,16); layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,0,0,8)}}
        wpRow.addView(TextView(this).apply{ text="Appui long sur accueil aussi dispo"; setTextColor(Color.parseColor("#9CA3AF")); textSize=11f})
        val wpBtn=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL; setPadding(0,12,0,0)}
        wpBtn.addView(TextView(this).apply{
            text="📷 Galerie"; setTextColor(Color.WHITE); background=GlassUtil.searchBar(prefs).apply{ setColor(Color.parseColor("#3B82F6"))}
            setPadding(22,14,22,14); layoutParams=LinearLayout.LayoutParams(0,-2,1f).apply{ setMargins(0,0,8,0)}; gravity=Gravity.CENTER
            setOnClickListener{ pickImage.launch("image/*") }
        })
        wpBtn.addView(TextView(this).apply{
            text="✕ Reset"; setTextColor(Color.WHITE); background=GlassUtil.liquidCardSmall(prefs)
            setPadding(18,14,18,14); gravity=Gravity.CENTER
            setOnClickListener{ prefs.edit().remove("wallpaper_uri").apply(); recreate()}
        })
        wpRow.addView(wpBtn)
        main.addView(wpRow)
        main.addView(section("Thème mec"))
        main.addView(choiceRow())
        main.addView(section("Transparence"))
        main.addView(sliderRow("Alpha", 40, 100, prefs.getInt("alpha",85)) { v -> prefs.edit().putInt("alpha",v).apply() })
        root.addView(main)
        setContentView(root)
    }
    private fun section(title:String)=TextView(this).apply{
        text=title; textSize=13f; setTextColor(Color.WHITE)
        typeface=android.graphics.Typeface.DEFAULT_BOLD
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
    private fun choiceRow(): LinearLayout {
        val row=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; background=GlassUtil.liquidCard(prefs); setPadding(18,14,18,14); layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,0,0,8)}}
        val cur=prefs.getString("theme","dark")!!
        listOf("black" to "⚫ AMOLED Noir","dark" to "🌑 Dark","blue" to "🔵 Bleu nuit","grey" to "⚙️ Gris béton").forEach{ (v,l) ->
            val rb=RadioButton(this).apply{ text=l; isChecked=(v==cur); setTextColor(Color.WHITE); textSize=12f}
            rb.setOnClickListener{ prefs.edit().putString("theme",v).remove("wallpaper_uri").apply(); recreate()}
            row.addView(rb)
        }
        return row
    }
}
