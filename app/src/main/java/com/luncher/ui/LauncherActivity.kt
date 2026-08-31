package com.luncher.ui

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
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
    private lateinit var rootBg:LinearLayout
    private val handler=Handler(Looper.getMainLooper())

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()){ uri: Uri? ->
        uri?.let{
            try{ contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) }catch(_:Exception){}
            prefs.edit().putString("wallpaper_uri", it.toString()).apply()
            applyWallpaper()
            Toast.makeText(this,"Fond changé",Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(b:Bundle?){
        super.onCreate(b)
        prefs=getSharedPreferences("luncher",0)
        rootBg=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL
            setPadding(28,70,28,24)
            setOnLongClickListener{ showWallpaperDialog(); true}
        }
        timeView=TextView(this).apply{
            textSize=48f; gravity=Gravity.CENTER
            setTextColor(Color.WHITE)
            typeface=android.graphics.Typeface.create("sans-serif-medium",0)
        }
        dateView=TextView(this).apply{
            textSize=13f; gravity=Gravity.CENTER
            setTextColor(Color.parseColor("#9CA3AF"))
            setPadding(0,4,0,20)
        }
        rootBg.addView(timeView)
        rootBg.addView(dateView)
        updateTime()
        applyWallpaper()

        val topBar=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL; gravity=Gravity.END}
        topBar.addView(TextView(this).apply{
            text="⚙️"; textSize=18f; gravity=Gravity.CENTER
            background=GlassUtil.liquidCardSmall(prefs); setPadding(22,14,22,14)
            setTextColor(Color.WHITE)
            setOnClickListener{ startActivity(Intent(this@LauncherActivity, SettingsActivity::class.java))}
        })
        rootBg.addView(topBar, LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,0,0,16)})

        search=EditText(this).apply{
            hint="Rechercher..."; textSize=14f
            setTextColor(Color.WHITE); setHintTextColor(Color.parseColor("#6B7280"))
            background=GlassUtil.searchBar(prefs); setPadding(44,28,44,28)
            layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,0,0,18)}
        }
        search.addTextChangedListener(object: android.text.TextWatcher{
            override fun afterTextChanged(s: android.text.Editable?){ filter=s.toString(); refreshApps()}
            override fun beforeTextChanged(a:CharSequence?,b:Int,c:Int,d:Int){}
            override fun onTextChanged(a:CharSequence?,b:Int,c:Int,d:Int){}
        })
        rootBg.addView(search)

        val scroll=ScrollView(this).apply{ isVerticalScrollBarEnabled=false }
        appsContainer=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL }
        scroll.addView(appsContainer)
        rootBg.addView(scroll, LinearLayout.LayoutParams(-1,0,1f))

        setContentView(rootBg)
        loadApps(); refreshApps()
    }

    private fun showWallpaperDialog(){
        val options=arrayOf("📷 Galerie","⚫ Noir AMOLED","🌑 Dark Gris","🔵 Bleu nuit","✕ Reset")
        android.app.AlertDialog.Builder(this)
          .setTitle("Fond d'écran")
          .setItems(options){ _, which ->
                when(which){
                    0 -> pickImage.launch("image/*")
                    1 -> { prefs.edit().putString("theme","black").remove("wallpaper_uri").apply(); applyWallpaper(); refreshApps()}
                    2 -> { prefs.edit().putString("theme","dark").remove("wallpaper_uri").apply(); applyWallpaper(); refreshApps()}
                    3 -> { prefs.edit().putString("theme","blue").remove("wallpaper_uri").apply(); applyWallpaper(); refreshApps()}
                    4 -> { prefs.edit().remove("wallpaper_uri").apply(); applyWallpaper()}
                }
            }.show()
    }

    private fun applyWallpaper(){
        val wpUri = prefs.getString("wallpaper_uri",null)
        if(wpUri!=null){
            try{
                val uri=Uri.parse(wpUri)
                val input=contentResolver.openInputStream(uri)
                val drawable=android.graphics.drawable.Drawable.createFromStream(input,null)
                rootBg.background=drawable
                input?.close()
                return
            }catch(_:Exception){}
        }
        rootBg.background=GlassUtil.bgLiquid(prefs)
    }

    private fun updateTime(){
        try{
            val now=Date()
            timeView.text=SimpleDateFormat("HH:mm", Locale.FRENCH).format(now)
            dateView.text=SimpleDateFormat("EEEE d MMM", Locale.FRENCH).format(now).uppercase()
            handler.postDelayed({updateTime()}, 30000)
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
        val iconSize=prefs.getInt("iconSize",92)
        val filtered=allApps.filter{ it.first.contains(filter,true)}.take(300)
        var row:LinearLayout?=null
        filtered.forEachIndexed{ index,(label,_,icon) ->
            if(index%4==0){
                row=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL }
                appsContainer.addView(row, LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,0,0,12)})
            }
            val item=LinearLayout(this).apply{
                orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER
                layoutParams=LinearLayout.LayoutParams(0,-2,1f)
                setOnClickListener{ try{ intents[label]?.let{ startActivity(it)}}catch(_:Exception){}}
                setOnLongClickListener{ showWallpaperDialog(); true}
            }
            item.addView(ImageView(this).apply{
                setImageDrawable(icon)
                layoutParams=LinearLayout.LayoutParams(iconSize,iconSize)
                background=GlassUtil.liquidCardSmall(prefs)
                setPadding(8,8,8,8)
            })
            if(prefs.getBoolean("showLabel",true)){
                item.addView(TextView(this).apply{
                    text=label; textSize=prefs.getInt("labelSize",10).toFloat()
                    setTextColor(Color.WHITE)
                    gravity=Gravity.CENTER; maxLines=1
                    setPadding(0,6,0,0)
                })
            }
            row?.addView(item)
        }
    }
    override fun onResume(){ super.onResume(); applyWallpaper(); loadApps(); refreshApps()}
}
