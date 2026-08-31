package com.luncher

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.luncher.util.ThemeUtil

class LauncherActivity: AppCompatActivity(){
    private lateinit var list: LinearLayout
    private var all: List<AppInfo> = emptyList()
    private var filter=""

    data class AppInfo(val label:String, val pkg:String, val icon:android.graphics.drawable.Drawable?, val intent:Intent)

    override fun onCreate(savedInstanceState: Bundle?){ super.onCreate(savedInstanceState); build()}

    private fun build(){
        val t = ThemeUtil.get(this)
        val root = LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; background=ThemeUtil.drawable(t); setPadding(40,90,40,20)}

        val header = LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL }
        header.addView(TextView(this).apply{
            text="Luncher"; textSize=34f; setTextColor(Color.parseColor(t.textColor))
            typeface=android.graphics.Typeface.DEFAULT_BOLD
            layoutParams=LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f)
        })
        header.addView(TextView(this).apply{ text=t.wrenchIcon; textSize=26f; setPadding(20,10,10,10); setOnClickListener{ startActivity(Intent(this@LauncherActivity, com.thibautfihey.luncher.ThemeSettingsActivity::class.java))}})
        root.addView(header)

        val search = EditText(this).apply{
            hint="Search apps..."; setHintTextColor(Color.parseColor("#AAAAAA")); setTextColor(Color.BLACK)
            background=GradientDrawable().apply{ cornerRadius=60f; setColor(Color.WHITE); setStroke(2, Color.parseColor("#E0E0E0"))}
            setPadding(48,36,48,36)
            layoutParams=LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT).apply{ setMargins(0,35,0,35)}
            addTextChangedListener(object:android.text.TextWatcher{
                override fun afterTextChanged(s:android.text.Editable?){ filter=s.toString(); refresh()}
                override fun beforeTextChanged(a:CharSequence?,b:Int,c:Int,d:Int){}
                override fun onTextChanged(a:CharSequence?,b:Int,c:Int,d:Int){}
            })
        }
        root.addView(search)

        val scroll=ScrollView(this)
        list=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL }
        scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0,1f))

        val bottom=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL; setPadding(0,30,0,0)}
        bottom.addView(bottomBtn("PHONE", "com.luncher.PhoneAppActivity", t))
        bottom.addView(bottomBtn("SMS", "com.luncher.SmsAppActivity", t))
        bottom.addView(bottomBtn("FILES", "com.luncher.FileManagerActivity", t))
        root.addView(bottom)

        setContentView(root)
        load(); refresh()
    }

    private fun bottomBtn(name:String, cls:String, t: com.luncher.util.UltraTheme)=Button(this).apply{
        text=name; setTextColor(Color.parseColor("#6A5ACD"))
        background=GradientDrawable().apply{ cornerRadius=12f; setColor(Color.parseColor("#444444")); setStroke(1, Color.parseColor("#555555")) }
        layoutParams=LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f).apply{ setMargins(10,0,10,0)}
        setOnClickListener{ startActivity(Intent().setClassName(packageName,cls))}
    }

    private fun load(){
        val pm=packageManager
        val main=Intent(Intent.ACTION_MAIN,null).apply{ addCategory(Intent.CATEGORY_LAUNCHER)}
        all = pm.queryIntentActivities(main,0).mapNotNull{
            try{
                val pkg=it.activityInfo.packageName
                val intent=pm.getLaunchIntentForPackage(pkg)?:return@mapNotNull null
                AppInfo(it.loadLabel(pm).toString(), pkg, it.loadIcon(pm), intent)
            }catch(_:Exception){ null }
        }.sortedBy{ it.label.lowercase() }.distinctBy{ it.pkg }
    }

    private fun refresh(){
        val t=ThemeUtil.get(this)
        list.removeAllViews()
        all.filter{ it.label.contains(filter,true)}.take(200).forEach{ app ->
            val row=LinearLayout(this).apply{
                orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL
                setPadding(10,18,10,18)
                setOnClickListener{ try{ startActivity(app.intent)}catch(_:Exception){}}
            }
            val icon = ImageView(this).apply{
                setImageDrawable(app.icon)
                layoutParams=LinearLayout.LayoutParams(96,96).apply{ setMargins(0,0,24,0)}
            }
            val label = TextView(this).apply{
                text=app.label; textSize=18f; setTextColor(Color.parseColor(t.textColor))
                typeface=android.graphics.Typeface.DEFAULT
                layoutParams=LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f)
            }
            row.addView(icon); row.addView(label)
            list.addView(row)
        }
    }
    override fun onResume(){ super.onResume(); build(); refresh()}
}
