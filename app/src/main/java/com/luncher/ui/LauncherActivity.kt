package com.luncher.ui

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.luncher.notifications.LuncherNotificationService
import com.luncher.notifications.NotificationStore
import com.luncher.util.GlassUtil
import java.text.SimpleDateFormat
import java.util.*

class LauncherActivity: AppCompatActivity(){
    private lateinit var appsContainer:LinearLayout
    private lateinit var notifContainer:LinearLayout
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
        }
    }

    override fun onCreate(b:Bundle?){
        super.onCreate(b)
        prefs=getSharedPreferences("luncher",0)
        rootBg=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL
            setPadding(28,60,28,24)
            setOnLongClickListener{ showWallpaperDialog(); true}
        }

        timeView=TextView(this).apply{ textSize=48f; gravity=Gravity.CENTER; setTextColor(Color.WHITE); typeface=android.graphics.Typeface.create("sans-serif-medium",0)}
        dateView=TextView(this).apply{ textSize=12f; gravity=Gravity.CENTER; setTextColor(Color.parseColor("#9CA3AF")); setPadding(0,4,0,12)}
        rootBg.addView(timeView); rootBg.addView(dateView)
        updateTime(); applyWallpaper()

        // Top bar
        val topBar=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL}
        topBar.addView(TextView(this).apply{ text=""; layoutParams=LinearLayout.LayoutParams(0,-2,1f)})
        topBar.addView(TextView(this).apply{
            text="⚙️"; textSize=18f; gravity=Gravity.CENTER
            background=GlassUtil.liquidCardSmall(prefs); setPadding(22,14,22,14); setTextColor(Color.WHITE)
            setOnClickListener{ startActivity(Intent(this@LauncherActivity, SettingsActivity::class.java))}
        })
        rootBg.addView(topBar, LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,0,0,12)})

        // === ESPACE NOTIFICATIONS SMS / WHATSAPP / GMAIL ===
        notifContainer=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL
            background=GlassUtil.notifCard(prefs)
            setPadding(16,14,16,14)
            layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,0,0,14)}
        }
        rootBg.addView(notifContainer)

        search=EditText(this).apply{
            hint="Rechercher une application..."; textSize=14f
            setTextColor(Color.WHITE); setHintTextColor(Color.parseColor("#6B7280"))
            background=GlassUtil.searchBar(prefs); setPadding(44,28,44,28)
            layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,0,0,14)}
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
        loadApps(); refreshApps(); refreshNotifFrame()
        checkNotifPermission()
    }

    private fun checkNotifPermission(){
        if(!NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)){
            Toast.makeText(this,"Active l'accès aux notifications pour SMS/WhatsApp/Gmail",Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
    }

    private fun refreshNotifFrame(){
        notifContainer.removeAllViews()
        val list = NotificationStore.notifs.take(3)
        if(list.isEmpty()){
            notifContainer.addView(LinearLayout(this).apply{
                orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL
            }.apply{
                addView(TextView(this@LauncherActivity).apply{ text="🔕 Aucune notification - WhatsApp / SMS / Gmail apparaîtront ici"; textSize=11f; setTextColor(Color.parseColor("#9CA3AF")); layoutParams=LinearLayout.LayoutParams(0,-2,1f)})
                addView(TextView(this@LauncherActivity).apply{
                    text="Activer"; textSize=10f; setTextColor(Color.WHITE); background=GlassUtil.searchBar(prefs).apply{ setColor(Color.parseColor("#3B82F6"))}
                    setPadding(14,8,14,8); setOnClickListener{ checkNotifPermission() }
                })
            })
        } else {
            list.forEach{ notif ->
                val row=LinearLayout(this).apply{
                    orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL
                    setPadding(0,8,0,8)
                    setOnClickListener{ showQuickReplyPopup(notif) }
                }
                val icon=when{
                    notif.pkg.contains("whatsapp") -> "💬"
                    notif.pkg.contains("gm") -> "✉️"
                    else -> "📩"
                }
                row.addView(TextView(this).apply{ text=icon; textSize=14f; setPadding(0,0,12,0)})
                val texts=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; layoutParams=LinearLayout.LayoutParams(0,-2,1f)}
                texts.addView(TextView(this).apply{ text="${notif.title} • ${notif.pkg.take(15)}"; textSize=11f; setTextColor(Color.WHITE); maxLines=1; typeface=android.graphics.Typeface.DEFAULT_BOLD})
                texts.addView(TextView(this).apply{ text=notif.text; textSize=11f; setTextColor(Color.parseColor("#CBD5E1")); maxLines=2})
                row.addView(texts)
                // Bouton réponse rapide priorité 1 en haut à droite
                row.addView(TextView(this).apply{
                    text="↩️"; textSize=14f; setPadding(12,6,12,6)
                    background=GlassUtil.popupCard()
                    setOnClickListener{ showQuickReplyPopup(notif) }
                })
                notifContainer.addView(row)
            }
        }
        handler.postDelayed({refreshNotifFrame()}, 2000)
    }

    private fun showQuickReplyPopup(notif: com.luncher.notifications.LuncherNotif){
        val dialog=android.app.Dialog(this)
        val layout=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL
            background=GlassUtil.popupCard()
            setPadding(20,20,20,20)
        }
        layout.addView(TextView(this).apply{ text="Réponse rapide - ${notif.title}"; setTextColor(Color.WHITE); textSize=14f; typeface=android.graphics.Typeface.DEFAULT_BOLD; setPadding(0,0,0,8)})
        layout.addView(TextView(this).apply{ text=notif.text; setTextColor(Color.parseColor("#9CA3AF")); textSize=12f; setPadding(0,0,0,12)})
        val input=EditText(this).apply{
            hint="Tape ta réponse..."; setHintTextColor(Color.parseColor("#6B7280"))
            setTextColor(Color.WHITE); background=GlassUtil.searchBar(prefs); setPadding(24,20,24,20)
        }
        layout.addView(input)
        val btnRow=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL; setPadding(0,16,0,0); gravity=Gravity.END}
        btnRow.addView(TextView(this).apply{
            text="Annuler"; setTextColor(Color.parseColor("#9CA3AF")); setPadding(18,12,18,12)
            setOnClickListener{ dialog.dismiss() }
        })
        btnRow.addView(TextView(this).apply{
            text=" Envoyer "; setTextColor(Color.WHITE); background=GlassUtil.searchBar(prefs).apply{ setColor(Color.parseColor("#3B82F6"))}
            setPadding(20,12,20,12); layoutParams=LinearLayout.LayoutParams(-2,-2).apply{ setMargins(12,0,0,0)}
            setOnClickListener{
                val txt=input.text.toString()
                if(txt.isNotBlank()){
                    LuncherNotificationService.sendQuickReply(notif, txt)
                    Toast.makeText(this@LauncherActivity,"Réponse envoyée",Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
            }
        })
        layout.addView(btnRow)
        dialog.setContentView(layout)
        dialog.window?.setLayout((resources.displayMetrics.widthPixels*0.85).toInt(), -2)
        dialog.window?.setGravity(Gravity.TOP)
        dialog.show()
    }

    private fun showWallpaperDialog(){
        val options=arrayOf("📷 Galerie","⚫ Noir","🌑 Dark","🔵 Bleu","✕ Reset")
        android.app.AlertDialog.Builder(this).setTitle("Fond d'écran").setItems(options){ _, which ->
            when(which){
                0 -> pickImage.launch("image/*")
                1 -> { prefs.edit().putString("theme","black").remove("wallpaper_uri").apply(); applyWallpaper()}
                2 -> { prefs.edit().putString("theme","dark").remove("wallpaper_uri").apply(); applyWallpaper()}
                3 -> { prefs.edit().putString("theme","blue").remove("wallpaper_uri").apply(); applyWallpaper()}
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
                rootBg.background=android.graphics.drawable.Drawable.createFromStream(input,null)
                input?.close(); return
            }catch(_:Exception){}
        }
        rootBg.background=GlassUtil.bgLiquid(prefs)
    }

    private fun updateTime(){
        try{
            timeView.text=SimpleDateFormat("HH:mm", Locale.FRENCH).format(Date())
            dateView.text=SimpleDateFormat("EEEE d MMM", Locale.FRENCH).format(Date()).uppercase()
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
        val labelSize=prefs.getInt("labelSize",10)
        val filtered=allApps.filter{ it.first.contains(filter,true)}.take(400)
        var row:LinearLayout?=null
        filtered.forEachIndexed{ index,(label,_,icon) ->
            if(index%4==0){
                row=LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL }
                appsContainer.addView(row, LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,0,0,10)})
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
                setPadding(6,6,6,6)
            })
            if(prefs.getBoolean("showLabel",true)){
                item.addView(TextView(this).apply{
                    text=label; textSize=labelSize.toFloat(); setTextColor(Color.WHITE)
                    gravity=Gravity.CENTER; maxLines=1; setPadding(0,6,0,0)
                })
            }
            row?.addView(item)
        }
    }
    override fun onResume(){ super.onResume(); applyWallpaper(); loadApps(); refreshApps(); refreshNotifFrame()}
}
