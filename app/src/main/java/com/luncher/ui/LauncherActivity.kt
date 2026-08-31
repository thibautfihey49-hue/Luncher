package com.luncher.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.luncher.files.FilesActivity
import com.luncher.phone.PhoneActivity
import com.luncher.sms.SmsActivity
import com.luncher.util.GlassUtil

class LauncherActivity: AppCompatActivity(){
    private lateinit var list:LinearLayout
    private var all:List<Triple<String,String,android.graphics.drawable.Drawable?>> = emptyList()
    private var intents:Map<String,Intent> = emptyMap()
    private var filter=""

    override fun onCreate(savedInstanceState: Bundle?){ super.onCreate(savedInstanceState); build()}

    private fun build(){
        val t=GlassUtil.get(this)
        val root=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; background=GlassUtil.bg(t); setPadding(32,110,32,24)}

        // Header
        val header=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL}
        header.addView(TextView(this).apply{ text="Luncher"; textSize=32f; setTextColor(Color.parseColor(t.text)); typeface=android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL); layoutParams=LinearLayout.LayoutParams(0,-2,1f)})
        header.addView(TextView(this).apply{ text="◍"; textSize=24f; setTextColor(Color.parseColor(t.text)); background=GlassUtil.card(100f); setPadding(24,16,24,16); setOnClickListener{ startActivity(Intent(this@LauncherActivity, ThemeActivity::class.java))}})
        root.addView(header)

        // Search - vrai glass
        val search=EditText(this).apply{
            hint="Search"; setHintTextColor(Color.parseColor("#66FFFFFF")); setTextColor(Color.parseColor(t.text)); textSize=16f
            background=GlassUtil.card(100f, "#22FFFFFF", "#22FFFFFF")
            setPadding(48,38,48,38)
            layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,36,0,24)}
            isSingleLine=true
            addTextChangedListener(object:android.text.TextWatcher{
                override fun afterTextChanged(s:android.text.Editable?){ filter=s.toString(); refresh()}
                override fun beforeTextChanged(a:CharSequence?,b:Int,c:Int,d:Int){}
                override fun onTextChanged(a:CharSequence?,b:Int,c:Int,d:Int){}
            })
        }
        root.addView(search)

        val scroll=ScrollView(this).apply{ isVerticalScrollBarEnabled=false}
        list=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL}
        scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(-1,0,1f))

        // Dock glass premium
        val dock=LinearLayout(this).apply{
            orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER
            background=GlassUtil.card(32f, "#1AFFFFFF", "#22FFFFFF")
            setPadding(16,16,16,16)
            layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,20,0,0)}
        }
        dock.addView(dockItem("Phone", "📞", PhoneActivity::class.java.name, t))
        dock.addView(dockItem("SMS", "💬", SmsActivity::class.java.name, t))
        dock.addView(dockItem("Files", "📁", FilesActivity::class.java.name, t))
        root.addView(dock)

        setContentView(root)
        load(); refresh()
    }

    private fun dockItem(name:String, emoji:String, cls:String, t: com.luncher.util.GlassTheme)=LinearLayout(this).apply{
        orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER
        layoutParams=LinearLayout.LayoutParams(0,-2,1f)
        setPadding(12,12,12,12)
        background=GlassUtil.card(24f, "#00FFFFFF", "#00FFFFFF")
        setOnClickListener{ startActivity(Intent().setClassName(packageName,cls))}
        addView(TextView(this@LauncherActivity).apply{ text=emoji; textSize=28f; gravity=Gravity.CENTER})
        addView(TextView(this@LauncherActivity).apply{ text=name; textSize=11f; setTextColor(Color.parseColor(t.subText)); gravity=Gravity.CENTER; setPadding(0,8,0,0); typeface=android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL)})
    }

    private fun load(){
        val pm=packageManager; val i=Intent(Intent.ACTION_MAIN,null).apply{ addCategory(Intent.CATEGORY_LAUNCHER)}
        val res=pm.queryIntentActivities(i,0); val map=mutableMapOf<String,Intent>()
        all=res.mapNotNull{
            try{
                val pkg=it.activityInfo.packageName; if(pkg==packageName) return@mapNotNull null
                val intent=pm.getLaunchIntentForPackage(pkg)?:return@mapNotNull null
                map[it.loadLabel(pm).toString()]=intent
                Triple(it.loadLabel(pm).toString(), pkg, it.loadIcon(pm))
            }catch(_:Exception){ null}
        }.sortedBy{ it.first.lowercase()}.distinctBy{ it.second }
        intents=map
    }

    private fun refresh(){
        list.removeAllViews(); val t=GlassUtil.get(this)
        all.filter{ it.first.contains(filter,true)}.take(80).forEach{ (label,_,icon) ->
            val row=LinearLayout(this).apply{
                orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL
                background=GlassUtil.cardSolid()
                setPadding(20,18,20,18)
                layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,0,0,12)}
                setOnClickListener{ intents[label]?.let{ startActivity(it) }}
            }
            row.addView(ImageView(this).apply{ setImageDrawable(icon); layoutParams=LinearLayout.LayoutParams(96,96).apply{ setMargins(0,0,20,0)}})
            row.addView(TextView(this).apply{ text=label; textSize=16f; setTextColor(Color.parseColor(t.text)); typeface=android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL); layoutParams=LinearLayout.LayoutParams(0,-2,1f)})
            list.addView(row)
        }
    }
    override fun onResume(){ super.onResume(); build(); refresh()}
}
