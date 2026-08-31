package com.luncher.ui

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.luncher.util.GlassUtil

class SettingsActivity: AppCompatActivity(){
    private lateinit var prefs:SharedPreferences
    override fun onCreate(b:Bundle?){
        super.onCreate(b)
        prefs=getSharedPreferences("luncher",0)
        val root=ScrollView(this)
        val main=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; background=GlassUtil.bgLiquid(prefs); setPadding(28,80,28,28)}

        main.addView(TextView(this).apply{ text="Liquid Glass Settings"; textSize=22f; setTextColor(Color.parseColor("#111827")); typeface=android.graphics.Typeface.create("serif", android.graphics.Typeface.BOLD_ITALIC); setPadding(0,0,0,16)})

        // 1 Liquid Glass - transparence
        main.addView(section("Liquid Glass"))
        main.addView(sliderRow("Transparence", 40, 100, prefs.getInt("alpha",70), { v -> prefs.edit().putInt("alpha",v).apply() }))

        // 2 Desktop Icon
        main.addView(section("Desktop Icon"))
        main.addView(sliderRow("Taille icônes", 64, 140, prefs.getInt("iconSize",96), { v -> prefs.edit().putInt("iconSize",v).apply() }))
        main.addView(toggleRow("Afficher labels", prefs.getBoolean("showLabel",true), { c -> prefs.edit().putBoolean("showLabel",c).apply() }))

        // 3 Drawer Icon
        main.addView(section("Drawer Icon"))
        main.addView(sliderRow("Taille label", 8, 14, prefs.getInt("labelSize",10), { v -> prefs.edit().putInt("labelSize",v).apply() }))

        // 4 Home screen style
        main.addView(section("Home screen style"))
        main.addView(choiceRow("Style", listOf("Light Liquid","Purple Liquid","Dark Liquid"), prefs.getString("theme","light")!!, { v -> prefs.edit().putString("theme",v).apply(); main.background=GlassUtil.bgLiquid(prefs) }))

        // 5-10 autres sections fonctionnelles
        listOf("Desktop","App drawer","Dock","Folder","Theme & Icon","Notification badges").forEach{ name ->
            main.addView(section(name))
            main.addView(TextView(this).apply{
                text="Option $name active"; textSize=12f; setTextColor(Color.parseColor("#6B7280"))
                background=GlassUtil.liquidCard(prefs); setPadding(24,18,24,18)
                layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,0,0,8)}
            })
        }

        root.addView(main)
        setContentView(root)
    }

    private fun section(title:String)=TextView(this).apply{
        text=title; textSize=16f; setTextColor(Color.parseColor("#111827"))
        typeface=android.graphics.Typeface.create("serif", android.graphics.Typeface.BOLD_ITALIC)
        background=GlassUtil.liquidCard(prefs); setPadding(24,18,24,18)
        layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,16,0,8)}
    }

    private fun sliderRow(name:String, min:Int, max:Int, cur:Int, onChange:(Int)->Unit): LinearLayout {
        val row=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; background=GlassUtil.liquidCard(prefs); setPadding(20,16,20,16); layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,0,0,8)}}
        val label=TextView(this).apply{ text="$name: $cur"; setTextColor(Color.parseColor("#111827")); textSize=13f}
        val seek=SeekBar(this).apply{ max=max-min; progress=cur-min}
        seek.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(s:SeekBar?, p:Int, f:Boolean){ val v=p+min; label.text="$name: $v"; onChange(v)}
            override fun onStartTrackingTouch(s:SeekBar?){}
            override fun onStopTrackingTouch(s:SeekBar?){}
        })
        row.addView(label); row.addView(seek)
        return row
    }

    private fun toggleRow(name:String, cur:Boolean, onChange:(Boolean)->Unit): LinearLayout {
        val row=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL; background=GlassUtil.liquidCard(prefs); setPadding(20,16,20,16); layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,0,0,8)}}
        row.addView(TextView(this).apply{ text=name; setTextColor(Color.parseColor("#111827")); layoutParams=LinearLayout.LayoutParams(0,-2,1f)})
        val sw=Switch(this).apply{ isChecked=cur}
        sw.setOnCheckedChangeListener{ _,c -> onChange(c)}
        row.addView(sw)
        return row
    }

    private fun choiceRow(name:String, options:List<String>, cur:String, onChange:(String)->Unit): LinearLayout {
        val row=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; background=GlassUtil.liquidCard(prefs); setPadding(20,16,20,16); layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,0,0,8)}}
        row.addView(TextView(this).apply{ text=name; setTextColor(Color.parseColor("#111827"))})
        val group=RadioGroup(this).apply{ orientation=RadioGroup.HORIZONTAL}
        options.forEach{ opt ->
            val rb=RadioButton(this).apply{ text=opt; isChecked=opt.lowercase().contains(cur); textSize=11f}
            rb.setOnClickListener{ onChange(opt.lowercase().split(" ")[0])}
            group.addView(rb)
        }
        row.addView(group)
        return row
    }
}
