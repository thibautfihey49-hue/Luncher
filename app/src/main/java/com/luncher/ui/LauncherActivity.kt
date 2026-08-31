package com.luncher.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.luncher.files.FilesActivity
import com.luncher.util.GlassUtil
import com.luncher.widgets.SidebarWidget
import java.text.SimpleDateFormat
import java.util.*

class LauncherActivity: AppCompatActivity(){
    private lateinit var appsContainer:LinearLayout
    private lateinit var search:EditText
    private var allApps:List<Triple<String,String,android.graphics.drawable.Drawable?>> = emptyList()
    private var intents:Map<String,Intent> = emptyMap()
    private var filter=""

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        val root = ScrollView(this).apply{ isVerticalScrollBarEnabled=false }
        val main = LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; background=GlassUtil.bgLiquid(); setPadding(24,60,24,24)}

        // HEADER : bouton paramètres en haut à droite
        val header = LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL}
        header.addView(TextView(this).apply{ text=""; layoutParams=LinearLayout.LayoutParams(0,-2,1f)})
        header.addView(TextView(this).apply{
            text="⚙️"; textSize=20f; gravity=Gravity.CENTER
            background=GlassUtil.liquidCard(); setPadding(28,18,28,18)
            setOnClickListener{ startActivity(Intent(this@LauncherActivity, SettingsActivity::class.java))}
        })
        main.addView(header)

        // SIDEBAR WIDGETS
        main.addView(TextView(this).apply{ text="Sidebar"; textSize=30f; setTextColor(Color.parseColor("#111827")); gravity=Gravity.CENTER; typeface=android.graphics.Typeface.create("serif", android.graphics.Typeface.BOLD_ITALIC); setPadding(0,10,0,16)})
        main.addView(SidebarWidget(this))

        // Tools & Liquid Folder
        val toolsCard = LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL; background=GlassUtil.liquidCard()
            setPadding(20,20,20,20); layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,16,0,0)}
        }
        toolsCard.addView(TextView(this).apply{ text="Tools & Liquid Folder"; textSize=14f; setTextColor(Color.parseColor("#111827")); typeface=android.graphics.Typeface.create("serif", android.graphics.Typeface.BOLD_ITALIC); setPadding(0,0,0,12)})
        val grid = GridLayout(this).apply{ columnCount=3}
        listOf(
            "📁" to "Files" to { startActivity(Intent(this@LauncherActivity, FilesActivity::class.java))},
            "📝" to "Notes" to { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://keep.google.com")))},
            "📊" to "Stats" to { startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))},
            "🔍" to "Search" to { search.requestFocus()},
            "◍" to "Theme" to { startActivity(Intent(this@LauncherActivity, SettingsActivity::class.java))},
            "🧹" to "Clean" to { startActivity(Intent(Intent.ACTION_VIEW).apply{ type="*/*"})},
            "👁️" to "Hide" to {}
        ).forEach{ (pair, action) ->
            val (emoji,_) = pair
            val cell = LinearLayout(this).apply{
                orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER
                background=GlassUtil.liquidCardSmall(); setPadding(0,20,0,20)
                layoutParams=GridLayout.LayoutParams().apply{ width=0; columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f); setMargins(8,8,8,8)}
                setOnClickListener{ action()}
            }
            cell.addView(TextView(this).apply{ text=emoji; textSize=26f; gravity=Gravity.CENTER})
            grid.addView(cell)
        }
        toolsCard.addView(grid)
        main.addView(toolsCard)

        // BARRE DE RECHERCHE SUR L'ACCUEIL
        search = EditText(this).apply{
            hint="Rechercher une application..."; setHintTextColor(Color.parseColor("#8A8FA3")); setTextColor(Color.parseColor("#111827")); textSize=15f
            background=GlassUtil.searchBar(); setPadding(48,32,48,32)
            layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,24,0,16)}
            typeface=android.graphics.Typeface.create("sans-serif",0)
        }
        search.addTextChangedListener(object: android.text.TextWatcher{
            override fun afterTextChanged(s: android.text.Editable?){ filter=s.toString(); refreshApps()}
            override fun beforeTextChanged(a:CharSequence?,b:Int,c:Int,d:Int){}
            override fun onTextChanged(a:CharSequence?,b:Int,c:Int,d:Int){}
        })
        main.addView(search)

        // TOUTES LES APPS SCROLLABLE
        appsContainer = LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL}
        main.addView(appsContainer)

        // DOCK LIQUID en bas
        val dock = LinearLayout(this).apply{
            orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER
            background=GlassUtil.dock(); setPadding(16,16,16,16)
            layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,20,0,0)}
        }
        listOf(
            "📞" to Intent(Intent.ACTION_DIAL),
            "💬" to Intent(Intent.ACTION_MAIN).apply{ type="vnd.android-dir/mms-sms"},
            "📁" to Intent(this@LauncherActivity, FilesActivity::class.java),
            "⚙️" to Intent(this@LauncherActivity, SettingsActivity::class.java)
        ).forEach{ (emoji,intent) ->
            val b = TextView(this).apply{
                text=emoji; textSize=22f; gravity=Gravity.CENTER
                background=GlassUtil.liquidCardSmall(); setPadding(0,18,0,18)
                layoutParams=LinearLayout.LayoutParams(0,-2,1f).apply{ setMargins(8,0,8,0)}
                setOnClickListener{ try{ startActivity(intent)}catch(_:Exception){}}
            }
            dock.addView(b)
        }
        main.addView(dock)

        root.addView(main)
        setContentView(root)
        loadApps(); refreshApps()
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
                    Triple(label, pkg, it.loadIcon(pm))
                }catch(_:Exception){null}
            }.sortedBy{it.first.lowercase()}
            intents=map
        }catch(_:Exception){}
    }

    private fun refreshApps(){
        appsContainer.removeAllViews()
        val filtered = allApps.filter{ it.first.contains(filter,true)}.take(200)
        // Grille 4 colonnes liquid
        var row: LinearLayout? = null
        filtered.forEachIndexed{ index, (label,_,icon) ->
            if(index%4==0){
                row = LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER}
                appsContainer.addView(row, LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,0,0,12)})
            }
            val item = LinearLayout(this).apply{
                orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER
                layoutParams=LinearLayout.LayoutParams(0,-2,1f).apply{ setMargins(6,0,6,0)}
                setOnClickListener{ try{ intents[label]?.let{ startActivity(it)}}catch(_:Exception){}}
            }
            item.addView(ImageView(this).apply{
                setImageDrawable(icon)
                layoutParams=LinearLayout.LayoutParams(96,96)
                background=GlassUtil.liquidCardSmall()
                setPadding(12,12,12,12)
            })
            item.addView(TextView(this).apply{
                text=label; textSize=10f; setTextColor(Color.parseColor("#111827")); gravity=Gravity.CENTER
                maxLines=1; setPadding(0,6,0,0)
                typeface=android.graphics.Typeface.create("sans-serif-medium",0)
            })
            row?.addView(item)
        }
        // remplir dernière ligne vide
        val rem = filtered.size % 4
        if(rem!=0){ repeat(4-rem){ row?.addView(LinearLayout(this).apply{ layoutParams=LinearLayout.LayoutParams(0,-2,1f)})}}
        if(filtered.isEmpty()){
            appsContainer.addView(TextView(this).apply{ text="Aucune app trouvée"; setTextColor(Color.parseColor("#9CA3AF")); gravity=Gravity.CENTER; setPadding(0,20,0,20)})
        }
    }
    override fun onResume(){ super.onResume(); loadApps(); refreshApps()}
}
