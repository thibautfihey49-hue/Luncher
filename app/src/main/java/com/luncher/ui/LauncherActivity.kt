package com.luncher.ui

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.luncher.util.GlassUtil
import java.text.SimpleDateFormat
import java.util.*

class LauncherActivity: AppCompatActivity(){
    private lateinit var appsContainer:LinearLayout
    private lateinit var search:EditText
    private lateinit var timeView:TextView
    private lateinit var dateView:TextView
    private var allApps:List<Triple<String,String,android.graphics.drawable.Drawable?>> = emptyList()
    private var intents:Map<String,Intent> = emptyMap()
    private var filter=""
    private lateinit var prefs:SharedPreferences
    private val handler=Handler(Looper.getMainLooper())

    override fun onCreate(b:Bundle?){
        super.onCreate(b)
        prefs=getSharedPreferences("luncher",0)

        val root=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; background=GlassUtil.bgLiquid(prefs); setPadding(32,80,32,24)}

        // HEURE + DATE SEULEMENT
        timeView=TextView(this).apply{
            textSize=46f; setTextColor(Color.parseColor("#111827")); gravity=Gravity.CENTER
            typeface=android.graphics.Typeface.create("sans-serif-light",0)
        }
        dateView=TextView(this).apply{
            textSize=15f; setTextColor(Color.parseColor("#6B7280")); gravity=Gravity.CENTER
            typeface=android.graphics.Typeface.create("sans-serif-medium",0)
            setPadding(0,4,0,24)
        }
        root.addView(timeView)
        root.addView(dateView)
        updateTime()

        // BOUTON PARAMETRES EN HAUT DROIT COMME AVANT
        val topBar=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL; gravity=Gravity.END}
        topBar.addView(TextView(this).apply{
            text="⚙️"; textSize=18f; gravity=Gravity.CENTER
            background=GlassUtil.liquidCard(prefs); setPadding(24,16,24,16)
            setOnClickListener{ startActivity(Intent(this@LauncherActivity, SettingsActivity::class.java))}
        })
        root.addView(topBar, LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,0,0,16)})

        // BARRE DE RECHERCHE
        search=EditText(this).apply{
            hint="Rechercher une application..."; setHintTextColor(Color.parseColor("#8A8FA3")); setTextColor(Color.parseColor("#111827")); textSize=15f
            background=GlassUtil.searchBar(prefs); setPadding(48,32,48,32)
            layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,0,0,20)}
        }
        search.addTextChangedListener(object: android.text.TextWatcher{
            override fun afterTextChanged(s: android.text.Editable?){ filter=s.toString(); refreshApps()}
            override fun beforeTextChanged(a:CharSequence?,b:Int,c:Int,d:Int){}
            override fun onTextChanged(a:CharSequence?,b:Int,c:Int,d:Int){}
        })
        root.addView(search)

        // TOUTES LES APPS SCROLLABLE
        val scroll=ScrollView(this).apply{ isVerticalScrollBarEnabled=false}
        appsContainer=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL}
        scroll.addView(appsContainer)
        root.addView(scroll, LinearLayout.LayoutParams(-1,0,1f))

        setContentView(root)
        loadApps(); refreshApps()
    }

    private fun updateTime(){
        try{
            val now=Date()
            timeView.text=SimpleDateFormat("HH:mm", Locale.FRENCH).format(now)
            dateView.text=SimpleDateFormat("EEEE d MMMM", Locale.FRENCH).format(now).replaceFirstChar{ it.uppercase()}
            handler.postDelayed({updateTime()}, 1000*30)
        }catch(_:Exception){}
    }

    private fun loadApps(){
        try{
            val pm=packageManager
            val i=Intent(Intent.ACTION_MAIN,null).apply{ addCategory(Intent.CATEGORY_LAUNCHER)}
            val res=pm.queryIntentActivities(i,0)
            val map=mutableMapOf<String,Intent>()
            allApps=res.mapNotNull{
                try{
                    val pkg=it.activityInfo.packageName
                    if(pkg==packageName) return@mapNotNull null
                    val launch=pm.getLaunchIntentForPackage(pkg)?: return@mapNotNull null
                    val label=it.loadLabel(pm).toString()
                    map[label]=launch
                    Triple(label,pkg,it.loadIcon(pm))
                }catch(_:Exception){null}
            }.sortedBy{it.first.lowercase()}
            intents=map
        }catch(_:Exception){}
    }

    private fun refreshApps(){
        appsContainer.removeAllViews()
        val iconSize=prefs.getInt("iconSize",96)
        val filtered=allApps.filter{ it.first.contains(filter,true)}.take(200)
        var row:LinearLayout?=null
        filtered.forEachIndexed{ index,(label,_,icon) ->
            if(index%4==0){
                row=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL}
                appsContainer.addView(row, LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,0,0,14)})
            }
            val item=LinearLayout(this).apply{
                orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER
                layoutParams=LinearLayout.LayoutParams(0,-2,1f)
                setOnClickListener{ try{ intents[label]?.let{ startActivity(it)}}catch(_:Exception){}}
            }
            item.addView(ImageView(this).apply{
                setImageDrawable(icon)
                layoutParams=LinearLayout.LayoutParams(iconSize,iconSize)
                background=GlassUtil.liquidCardSmall(prefs)
                setPadding(10,10,10,10)
            })
            if(prefs.getBoolean("showLabel",true)){
                item.addView(TextView(this).apply{
                    text=label; textSize=prefs.getInt("labelSize",10).toFloat()
                    setTextColor(Color.parseColor("#111827")); gravity=Gravity.CENTER; maxLines=1
                    setPadding(0,6,0,0)
                })
            }
            row?.addView(item)
        }
    }
    override fun onResume(){ super.onResume(); loadApps(); refreshApps(); rootView?.background=GlassUtil.bgLiquid(prefs)}
    private val rootView get() = window.decorView as? LinearLayout
}
