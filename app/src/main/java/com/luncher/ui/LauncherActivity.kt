package com.luncher.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
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

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(GlassUtil.bg(GlassUtil.get(this)))
        build()
    }

    private fun build(){
        val t=GlassUtil.get(this)
        val root=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; background=GlassUtil.bg(t); setPadding(40,100,40,30)}

        // Header glass
        val header=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL}
        header.addView(TextView(this).apply{ text="Luncher"; textSize=36f; setTextColor(Color.parseColor(t.text)); typeface=android.graphics.Typeface.DEFAULT_BOLD; layoutParams=LinearLayout.LayoutParams(0,-2,1f)})
        header.addView(TextView(this).apply{ text="◍"; textSize=26f; setTextColor(Color.parseColor(t.text)); setPadding(20,10,10,10); setOnClickListener{ startActivity(Intent(this@LauncherActivity, ThemeActivity::class.java))}})
        root.addView(header)

        // Search glass transparent
        val search=EditText(this).apply{
            hint="Search apps, contacts, files..."; setHintTextColor(Color.parseColor("#88FFFFFF")); setTextColor(Color.WHITE)
            background=GradientDrawable().apply{ cornerRadius=60f; setColor(Color.parseColor("#1AFFFFFF")); setStroke(1, Color.parseColor("#33FFFFFF"))}
            setPadding(48,36,48,36)
            layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,40,0,40)}
            addTextChangedListener(object:android.text.TextWatcher{
                override fun afterTextChanged(s:android.text.Editable?){ filter=s.toString(); refresh()}
                override fun beforeTextChanged(a:CharSequence?,b:Int,c:Int,d:Int){}
                override fun onTextChanged(a:CharSequence?,b:Int,c:Int,d:Int){}
            })
        }
        root.addView(search)

        // Apps list glass
        val scroll=ScrollView(this)
        list=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL}
        scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(-1,0,1f))

        // Bottom glass dock
        val dock=LinearLayout(this).apply{
            orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER
            background=GradientDrawable().apply{ cornerRadius=40f; setColor(Color.parseColor("#1A000000")); setStroke(1, Color.parseColor("#22FFFFFF"))}
            setPadding(20,20,20,20)
            layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,30,0,0)}
        }
        dock.addView(dockBtn("Phone", PhoneActivity::class.java.name, "📞", t))
        dock.addView(dockBtn("SMS", SmsActivity::class.java.name, "💬", t))
        dock.addView(dockBtn("Files", FilesActivity::class.java.name, "📁", t))
        root.addView(dock)

        setContentView(root)
        load(); refresh()
    }

    private fun dockBtn(name:String, cls:String, icon:String, t: com.luncher.util.GlassTheme)=LinearLayout(this).apply{
        orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER
        layoutParams=LinearLayout.LayoutParams(0,-2,1f)
        setPadding(10,10,10,10)
        setOnClickListener{ startActivity(Intent().setClassName(packageName,cls))}
        addView(TextView(this@LauncherActivity).apply{ text=icon; textSize=26f; gravity=Gravity.CENTER})
        addView(TextView(this@LauncherActivity).apply{ text=name; textSize=11f; setTextColor(Color.parseColor(t.text)); gravity=Gravity.CENTER; setPadding(0,6,0,0)})
    }

    private fun load(){
        val pm=packageManager
        val i=Intent(Intent.ACTION_MAIN,null).apply{ addCategory(Intent.CATEGORY_LAUNCHER)}
        val res=pm.queryIntentActivities(i,0)
        val map=mutableMapOf<String,Intent>()
        all=res.mapNotNull{
            try{
                val pkg=it.activityInfo.packageName
                val intent=pm.getLaunchIntentForPackage(pkg)?:return@mapNotNull null
                map[it.loadLabel(pm).toString()]=intent
                Triple(it.loadLabel(pm).toString(), pkg, it.loadIcon(pm))
            }catch(_:Exception){ null}
        }.sortedBy{ it.first.lowercase()}.distinctBy{ it.second }
        intents=map
    }

    private fun refresh(){
        list.removeAllViews()
        val t=GlassUtil.get(this)
        all.filter{ it.first.contains(filter,true)}.take(100).forEach{ (label,_,icon) ->
            val row=LinearLayout(this).apply{
                orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL
                background=GlassUtil.glassCard(24f)
                setPadding(20,16,20,16)
                layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,8,0,8)}
                setOnClickListener{ intents[label]?.let{ startActivity(it) }}
            }
            row.addView(ImageView(this).apply{ setImageDrawable(icon); layoutParams=LinearLayout.LayoutParams(88,88).apply{ setMargins(0,0,20,0)}})
            row.addView(TextView(this).apply{ text=label; textSize=17f; setTextColor(Color.parseColor(t.text)); layoutParams=LinearLayout.LayoutParams(0,-2,1f)})
            list.addView(row)
        }
    }

    override fun onResume(){ super.onResume(); build(); refresh()}
}
