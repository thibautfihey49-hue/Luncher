package com.luncher
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.luncher.util.ThemeUtil
import com.thibautfihey.luncher.ThemeSettingsActivity

class LauncherActivity: AppCompatActivity(){
    private lateinit var listView:LinearLayout
    private var allApps:List<Pair<String,Intent>> = emptyList()
    private var currentFilter=""

    override fun onCreate(savedInstanceState: Bundle?){ super.onCreate(savedInstanceState); buildAndLoad() }

    private fun buildAndLoad(){
        try{
            val bg = ThemeUtil.getBg(this)
            val txt = ThemeUtil.getTextColor(bg)
            val root = LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; setBackgroundColor(bg); setPadding(40,90,40,20) }

            val header = LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL }
            header.addView(TextView(this).apply{ text="Luncher"; textSize=34f; setTextColor(txt); typeface=android.graphics.Typeface.DEFAULT_BOLD; layoutParams=LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f)})
            header.addView(TextView(this).apply{ text="🔧"; textSize=28f; setPadding(20,10,10,10); setOnClickListener{ openThemes() }})
            root.addView(header)

            val search = EditText(this).apply{
                hint="Search apps..."; setHintTextColor(Color.parseColor("#BBBBBB")); setTextColor(Color.BLACK)
                background=GradientDrawable().apply{ shape=GradientDrawable.RECTANGLE; cornerRadius=60f; setColor(Color.WHITE); setStroke(2,Color.parseColor("#E0E0E0"))}
                setPadding(45,32,45,32)
                layoutParams=LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT).apply{ setMargins(0,35,0,35)}
                addTextChangedListener(object:android.text.TextWatcher{
                    override fun afterTextChanged(s:android.text.Editable?){ currentFilter=s.toString(); filterApps(currentFilter)}
                    override fun beforeTextChanged(s:CharSequence?,a:Int,b:Int,c:Int){}
                    override fun onTextChanged(s:CharSequence?,a:Int,b:Int,c:Int){}
                })
            }
            root.addView(search)

            val scroll = ScrollView(this)
            listView = LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL }
            scroll.addView(listView)
            root.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0,1f))

            val bottom = LinearLayout(this).apply{ orientation=LinearLayout.HORIZONTAL; setPadding(0,30,0,0)}
            bottom.addView(createBottom("Phone","com.luncher.PhoneAppActivity"))
            bottom.addView(createBottom("SMS","com.luncher.SmsAppActivity"))
            bottom.addView(createBottom("Files","com.luncher.FileManagerActivity"))
            root.addView(bottom)

            root.setOnLongClickListener{ openThemes(); true }
            setContentView(root)
            loadApps()
            filterApps(currentFilter)
        }catch(e:Exception){ ThemeUtil.log(this,"build err ${e.message}"); setContentView(TextView(this).apply{ text="Err ${e.message}"})}
    }

    private fun createBottom(name:String, cls:String):Button = Button(this).apply{
        text=name; setTextColor(Color.parseColor("#6A5ACD")); layoutParams=LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1f).apply{ setMargins(12,0,12,0)}
        setOnClickListener{ try{ startActivity(Intent().setClassName(packageName,cls))}catch(e:Exception){ Toast.makeText(this@LauncherActivity,e.message,Toast.LENGTH_SHORT).show()}}
    }

    private fun loadApps(){
        try{
            val pm=packageManager
            val i=Intent(Intent.ACTION_MAIN,null).apply{ addCategory(Intent.CATEGORY_LAUNCHER)}
            allApps=pm.queryIntentActivities(i,0).mapNotNull{ pm.getLaunchIntentForPackage(it.activityInfo.packageName)?.let{ launch -> it.loadLabel(pm).toString() to launch } }.sortedBy{ it.first.lowercase()}
        }catch(e:Exception){ ThemeUtil.log(this,"loadApps ${e.message}")}
    }

    private fun filterApps(q:String){
        val txt=ThemeUtil.getTextColor(ThemeUtil.getBg(this))
        listView.removeAllViews()
        allApps.filter{ it.first.contains(q,true)}.take(150).forEach{ (label,launch) ->
            listView.addView(TextView(this).apply{
                text=label; textSize=18f; setPadding(20,30,20,30); setTextColor(txt)
                setOnClickListener{ try{ startActivity(launch)}catch(e:Exception){ Toast.makeText(this@LauncherActivity,"${e.message}",Toast.LENGTH_SHORT).show()}}
            })
        }
    }

    private fun openThemes(){ startActivity(Intent(this, ThemeSettingsActivity::class.java)) }
    override fun onResume(){ super.onResume(); buildAndLoad(); filterApps(currentFilter) }
}
