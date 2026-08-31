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
import com.luncher.util.IconUtil

class LauncherActivity: AppCompatActivity(){
    private lateinit var list:LinearLayout
    private var all:List<Triple<String,String,android.graphics.drawable.Drawable?>> = emptyList()
    private var intents:Map<String,Intent> = emptyMap()
    private var filter=""

    override fun onCreate(savedInstanceState: Bundle?){ super.onCreate(savedInstanceState)
        val t=GlassUtil.get(this)
        val root=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; background=GlassUtil.bg(t); setPadding(32,110,32,24)}

        // Header premium
        val header=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL}
        header.addView(LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL; layoutParams=LinearLayout.LayoutParams(0,-2,1f)
            addView(TextView(this@LauncherActivity).apply{ text="Luncher"; textSize=36f; setTextColor(Color.WHITE); typeface=android.graphics.Typeface.create("sans-serif-black",0)})
            addView(TextView(this@LauncherActivity).apply{ text="OS 2.0 • Glass"; textSize=12f; setTextColor(Color.parseColor("#9AA0C0"))})
        })
        header.addView(TextView(this).apply{ text="◍"; textSize=22f; setTextColor(Color.WHITE); background=GlassUtil.card(100f); setPadding(28,20,28,20); setOnClickListener{ startActivity(Intent(this@LauncherActivity, ThemeActivity::class.java))}})
        root.addView(header)

        // Search pro
        val search=EditText(this).apply{
            hint="Search apps"; setHintTextColor(Color.parseColor("#66FFFFFF")); setTextColor(Color.WHITE); textSize=16f
            background=GlassUtil.card(100f, "#1AFFFFFF", "#22FFFFFF")
            setPadding(52,40,52,40)
            layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,36,0,20)}
        }
        search.addTextChangedListener(object:android.text.TextWatcher{
            override fun afterTextChanged(s:android.text.Editable?){ filter=s.toString(); refresh()}
            override fun beforeTextChanged(a:CharSequence?,b:Int,c:Int,d:Int){}
            override fun onTextChanged(a:CharSequence?,b:Int,c:Int,d:Int){}
        })
        root.addView(search)

        val scroll=ScrollView(this).apply{ isVerticalScrollBarEnabled=false}
        list=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL}
        scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(-1,0,1f))

        // Dock PRO avec icônes stylées
        val dock=LinearLayout(this).apply{
            orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER
            background=GlassUtil.card(32f, "#1AFFFFFF", "#22FFFFFF")
            setPadding(20,20,20,20)
            layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,16,0,0)}
        }
        dock.addView(dockItem("Phone", PhoneActivity::class.java.name, "phone"))
        dock.addView(dockItem("Messages", SmsActivity::class.java.name, "sms"))
        dock.addView(dockItem("Files", FilesActivity::class.java.name, "files"))
        root.addView(dock)

        setContentView(root)
        load(); refresh()
    }

    private fun dockItem(name:String, cls:String, type:String)=LinearLayout(this).apply{
        orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER
        layoutParams=LinearLayout.LayoutParams(0,-2,1f)
        setPadding(8,8,8,8)
        setOnClickListener{ startActivity(Intent().setClassName(packageName,cls))}
        val icon=TextView(this@LauncherActivity).apply{ layoutParams=LinearLayout.LayoutParams(112,112)}
        when(type){ "phone"->IconUtil.phoneIcon(icon); "sms"->IconUtil.smsIcon(icon); else->IconUtil.filesIcon(icon)}
        addView(icon)
        addView(TextView(this@LauncherActivity).apply{ text=name; textSize=11f; setTextColor(Color.parseColor("#9AA0C0")); gravity=Gravity.CENTER; setPadding(0,10,0,0)})
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
        list.removeAllViews()
        all.filter{ it.first.contains(filter,true)}.take(60).forEach{ (label,_,icon) ->
            val row=LinearLayout(this).apply{
                orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL
                background=GlassUtil.cardSolid()
                setPadding(18,16,18,16)
                layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,0,0,12)}
                setOnClickListener{ intents[label]?.let{ startActivity(it) }}
            }
            row.addView(ImageView(this).apply{ setImageDrawable(icon); layoutParams=LinearLayout.LayoutParams(92,92).apply{ setMargins(0,0,18,0)}})
            row.addView(TextView(this).apply{ text=label; textSize=16f; setTextColor(Color.WHITE); layoutParams=LinearLayout.LayoutParams(0,-2,1f); typeface=android.graphics.Typeface.create("sans-serif-medium",0)})
            row.addView(TextView(this).apply{ text="↗"; setTextColor(Color.parseColor("#9AA0C0"))})
            list.addView(row)
        }
    }
    override fun onResume(){ super.onResume(); refresh()}
}
