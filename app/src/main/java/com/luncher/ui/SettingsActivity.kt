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

        main.addView(section("Liquid Glass"))
        main.addView(sliderRow("Transparence", 40, 100, prefs.getInt("alpha",70)) { newVal -> prefs.edit().putInt("alpha",newVal).apply() })

        main.addView(section("Desktop Icon"))
        main.addView(sliderRow("Taille icônes", 64, 140, prefs.getInt("iconSize",96)) { newVal -> prefs.edit().putInt("iconSize",newVal).apply() })
        main.addView(toggleRow("Afficher labels", prefs.getBoolean("showLabel",true)) { checked -> prefs.edit().putBoolean("showLabel",checked).apply() })

        main.addView(section("Drawer Icon"))
        main.addView(sliderRow("Taille label", 8, 14, prefs.getInt("labelSize",10)) { newVal -> prefs.edit().putInt("labelSize",newVal).apply() })

        main.addView(section("Home screen style"))
        main.addView(choiceRow())

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
        val seek=SeekBar(this).apply{ this.max=max-min; progress=cur-min}
        seek.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(s:SeekBar?, p:Int, f:Boolean){ val newV=p+min; label.text="$name: $newV"; onChange(newV)}
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
        sw.setOnCheckedChangeListener{ _,checked -> onChange(checked)}
        row.addView(sw)
        return row
    }

    private fun choiceRow(): LinearLayout {
        val row=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; background=GlassUtil.liquidCard(prefs); setPadding(20,16,20,16); layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,0,0,8)}}
        row.addView(TextView(this).apply{ text="Thème"; setTextColor(Color.parseColor("#111827"))})
        val group=RadioGroup(this).apply{ orientation=RadioGroup.HORIZONTAL}
        val current=prefs.getString("theme","light")!!
        listOf("light" to "Light","purple" to "Purple","dark" to "Dark").forEach{ (value,label) ->
            val rb=RadioButton(this).apply{ text=label; isChecked=(value==current); textSize=11f}
            rb.setOnClickListener{
                prefs.edit().putString("theme",value).apply()
                (row.parent as? LinearLayout)?.let{ parent -> parent.background=GlassUtil.bgLiquid(prefs)}
            }
            group.addView(rb)
        }
        row.addView(group)
        return row
    }
}
