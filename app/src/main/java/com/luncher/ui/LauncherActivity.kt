package com.luncher.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.luncher.files.FilesActivity
import com.luncher.util.GlassUtil
import com.luncher.widgets.SidebarWidget

class LauncherActivity: AppCompatActivity(){
    private lateinit var list:LinearLayout
    private var all:List<Triple<String,String,android.graphics.drawable.Drawable?>> = emptyList()
    private var intents:Map<String,Intent> = emptyMap()

    override fun onCreate(b:Bundle?){
        super.onCreate(b)
        val root=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; background=GlassUtil.bgLiquid(); setPadding(28,70,28,24)}

        // Title Sidebar comme ton screen 1
        root.addView(TextView(this).apply{ text="Sidebar"; textSize=32f; setTextColor(Color.parseColor("#111827")); gravity=Gravity.CENTER; typeface=android.graphics.Typeface.DEFAULT_BOLD; setPadding(0,0,0,20)})

        // Phone mockup
        val phoneFrame = LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL
            background=GlassUtil.liquidCard()
            setPadding(18,18,18,18)
            layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(40,0,40,0)}
        }
        // notch
        phoneFrame.addView(LinearLayout(this).apply{
            gravity=Gravity.CENTER
            addView(TextView(this@LauncherActivity).apply{ text=""; background=GlassUtil.pill(); layoutParams=LinearLayout.LayoutParams(140,28)})
        })
        phoneFrame.addView(SidebarWidget(this))

        // Liquid Folder demo
        val folder = LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL
            background=GlassUtil.liquidFolder()
            setPadding(20,20,20,20)
            layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(0,16,0,0)}
        }
        folder.addView(TextView(this).apply{ text="Tools & Liquid Folder"; textSize=14f; setTextColor(Color.parseColor("#111827")); typeface=android.graphics.Typeface.DEFAULT_BOLD; setPadding(0,0,0,12)})
        val folderGrid = GridLayout(this).apply{ columnCount=3}
        listOf("📁","📝","📊","🔍","◍","🧹","👁️").forEach{ e ->
            folderGrid.addView(TextView(this).apply{ text=e; textSize=22f; gravity=Gravity.CENTER; background=GlassUtil.liquidCardSmall(); setPadding(0,18,0,18); layoutParams=GridLayout.LayoutParams().apply{ width=0; columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f); setMargins(6,6,6,6)}})
        }
        folder.addView(folderGrid)
        phoneFrame.addView(folder)

        root.addView(phoneFrame, LinearLayout.LayoutParams(-1,0,1f))

        // Dock Liquid
        val dock = LinearLayout(this).apply{
            orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER
            background=GlassUtil.dock()
            setPadding(18,18,18,18)
            layoutParams=LinearLayout.LayoutParams(-1,-2).apply{ setMargins(40,16,40,0)}
        }
        listOf("📞" to Intent(Intent.ACTION_DIAL), "💬" to Intent(Intent.ACTION_MAIN).apply{ type="vnd.android-dir/mms-sms"}, "📁" to Intent(this@LauncherActivity, FilesActivity::class.java), "⚙️" to Intent(this@LauncherActivity, SettingsActivity::class.java)).forEach{ (emoji,intent) ->
            val btn = LinearLayout(this).apply{
                orientation=LinearLayout.VERTICAL; gravity=Gravity.CENTER
                layoutParams=LinearLayout.LayoutParams(0,-2,1f)
                setOnClickListener{ try{ startActivity(intent)}catch(_:Exception){}}
            }
            btn.addView(TextView(this).apply{ text=emoji; textSize=22f; gravity=Gravity.CENTER; background=GlassUtil.liquidCardSmall(); setPadding(0,16,0,16); layoutParams=LinearLayout.LayoutParams(92,92)})
            dock.addView(btn)
        }
        root.addView(dock)

        setContentView(root)
    }

    private fun pill() = GlassUtil.liquidCardSmall().apply{ setColor(Color.BLACK)}
}
