package com.luncher.ui

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.luncher.files.FilesActivity
import com.luncher.util.GlassUtil
import com.luncher.util.IconUtil

class LauncherActivity: AppCompatActivity(){
    private lateinit var list:LinearLayout
    private var all:List<Triple<String,String,android.graphics.drawable.Drawable?>> = emptyList()
    private var intents:Map<String,Intent> = emptyMap()
    private var filter=""

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        val root=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; background=GlassUtil.bg(); setPadding(32,90,32,24)}

        // Header light
        val header=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL}
        val left=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; layoutParams=LinearLayout.LayoutParams(0,-2,1f)}
        left.addView(TextView(this@LauncherActivity).apply{
            text="Luncher"; textSize=34f; setTextColor(Color.parseColor("#111827"))
            typeface=android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.BOLD)
        })
        left.addView(TextView(this@LauncherActivity).apply{ text="Light • Glass"; textSize=12f; setTextColor(Color.parseColor("#9CA3AF")); typeface=android.graphics.Typeface.create("sans-serif-medium",0)})
        header.addView(left)
        header.addView(TextView(this).apply{
            text="◍"; textSize=20f; setTextColor(Color.parseColor("#111827"))
            background=GlassUtil.card(); setPadding(26,18,26,18)
            setOnClickListener{ startActivity(Intent(this@LauncherActivity, ThemeActivity::class.java))}
        })
        root.addView(header)

        val search=EditText(this).apply{
            hint="Rechercher une app"; setHintTextColor(Color.parseColor("#9CA3AF")); setTextColor(Color.parseColor("#111827")); textSize=15f
            background=GlassUtil.searchBg(); setPadding(48,36,48,36)
            layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,28,0,20)}
            typeface=android.graphics.Typeface.create("sans-serif",0)
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

        // Dock light glass
        val dock=LinearLayout(this).apply{
            orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER
            background=GlassUtil.dock(); setPadding(20,20,20,20)
            layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,16,0,0)}
        }
        dock.addView(dockItem("Phone","phone"){ startActivity(Intent(Intent.ACTION_DIAL))})
        dock.addView(dockItem("Messages","sms"){ try{ startActivity(Intent(Intent.ACTION_MAIN).apply{ type="vnd.android-dir/mms-sms"}) }catch(_:Exception){ startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("sms:"))) }})
        dock.addView(dockItem("Files","files"){ startActivity(Intent(this@LauncherActivity, FilesActivity::class.java))})
        root.addView(dock)

        setContentView(root)
        load(); refresh()
    }

    private fun dockItem(name:String, type:String, click:()->Unit)=LinearLayout(this).apply{
        orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER
        layoutParams=LinearLayout.LayoutParams(0,-2,1f); setPadding(8,8,8,8)
        setOnClickListener{ click()}
        val icon=TextView(this@LauncherActivity).apply{ layoutParams=LinearLayout.LayoutParams(108,108); gravity=Gravity.CENTER}
        when(type){ "phone"->IconUtil.phone(icon); "sms"->IconUtil.sms(icon); else->IconUtil.files(icon)}
        addView(icon)
        addView(TextView(this@LauncherActivity).apply{ text=name; textSize=11f; setTextColor(Color.parseColor("#6B7280")); gravity=Gravity.CENTER; setPadding(0,8,0,0); typeface=android.graphics.Typeface.create("sans-serif-medium",0)})
    }

    private fun load(){
        try{
            val pm=packageManager
            val i=Intent(Intent.ACTION_MAIN,null).apply{ addCategory(Intent.CATEGORY_LAUNCHER)}
            val res=pm.queryIntentActivities(i,0)
            val map=mutableMapOf<String,Intent>()
            all=res.mapNotNull{
                try{
                    val pkg=it.activityInfo.packageName
                    if(pkg==packageName) return@mapNotNull null
                    val launch=pm.getLaunchIntentForPackage(pkg)?:return@mapNotNull null
                    map[it.loadLabel(pm).toString()]=launch
                    Triple(it.loadLabel(pm).toString(), pkg, it.loadIcon(pm))
                }catch(_:Exception){null}
            }.sortedBy{it.first.lowercase()}.distinctBy{it.second}
            intents=map
        }catch(_:Exception){}
    }

    private fun refresh(){
        list.removeAllViews()
        all.filter{ it.first.contains(filter,true)}.take(50).forEach{ (label,_,icon)->
            val row=LinearLayout(this).apply{
                orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL
                background=GlassUtil.card(); setPadding(16,14,16,14)
                layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,0,0,10)}
                setOnClickListener{ try{ intents[label]?.let{ startActivity(it)}}catch(_:Exception){}}
            }
            row.addView(ImageView(this).apply{ setImageDrawable(icon); layoutParams=LinearLayout.LayoutParams(88,88).apply{ setMargins(0,0,16,0)}})
            row.addView(TextView(this).apply{ text=label; textSize=15f; setTextColor(Color.parseColor("#111827")); layoutParams=LinearLayout.LayoutParams(0,-2,1f); typeface=android.graphics.Typeface.create("sans-serif-medium",0)})
            row.addView(TextView(this).apply{ text="›"; textSize=20f; setTextColor(Color.parseColor("#9CA3AF"))})
            list.addView(row)
        }
    }
    override fun onResume(){ super.onResume(); refresh()}
}
