package com.luncher

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.luncher.util.ThemeUtil
import com.thibautfihey.luncher.ThemeSettingsActivity

class LauncherActivity : AppCompatActivity() {
    private lateinit var listView: LinearLayout
    private lateinit var search: EditText
    private var allApps: List<Pair<String, Intent>> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            val root = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(ThemeUtil.getBg(this@LauncherActivity))
                setPadding(24,80,24,24)
            }

            val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            header.addView(TextView(this).apply {
                text = "Luncher"; textSize = 28f; setTextColor(Color.BLACK)
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            })
            header.addView(TextView(this).apply {
                text = "🔧"; textSize = 24f; setPadding(20,20,20,20)
                setOnClickListener { openThemes() }
            })
            root.addView(header)

            search = EditText(this).apply {
                hint = "Search apps..."
                setBackgroundResource(android.R.drawable.editbox_background)
                addTextChangedListener(object: android.text.TextWatcher{
                    override fun afterTextChanged(s: android.text.Editable?) { filterApps(s.toString()) }
                    override fun beforeTextChanged(s: CharSequence?, a:Int,b:Int,c:Int){}
                    override fun onTextChanged(s: CharSequence?, a:Int,b:Int,c:Int){}
                })
            }
            root.addView(search)

            val scroll = ScrollView(this)
            listView = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            scroll.addView(listView)
            root.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

            val bottom = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            bottom.addView(createBottomBtn("Phone", "com.luncher.PhoneAppActivity"))
            bottom.addView(createBottomBtn("SMS", "com.luncher.SmsAppActivity"))
            bottom.addView(createBottomBtn("Files", "com.luncher.FileManagerActivity"))
            root.addView(bottom)

            root.setOnLongClickListener { openThemes(); true }
            setContentView(root)
            loadAllApps()
            filterApps("")
        } catch(e:Exception){
            ThemeUtil.log(this, "Launcher crash ${e.message}")
            setContentView(TextView(this).apply{ text="Launcher crash ${e.message}"; setTextColor(Color.RED) })
        }
    }

    private fun createBottomBtn(name:String, className:String): Button {
        return Button(this).apply {
            text = name
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply{ setMargins(10,0,10,0) }
            setOnClickListener {
                try {
                    val i = Intent().setClassName(packageName, className)
                    startActivity(i)
                } catch(e:Exception){
                    Toast.makeText(this@LauncherActivity, e.message, Toast.LENGTH_SHORT).show()
                    ThemeUtil.log(this@LauncherActivity, "bottom $name err ${e.message}")
                }
            }
        }
    }

    private fun loadAllApps(){
        try{
            val pm = packageManager
            val intent = Intent(Intent.ACTION_MAIN, null).apply{ addCategory(Intent.CATEGORY_LAUNCHER) }
            allApps = pm.queryIntentActivities(intent, 0).mapNotNull {
                val launch = pm.getLaunchIntentForPackage(it.activityInfo.packageName) ?: return@mapNotNull null
                it.loadLabel(pm).toString() to launch
            }.sortedBy{ it.first.lowercase() }
        }catch(e:Exception){ ThemeUtil.log(this, "loadAllApps ${e.message}") }
    }

    private fun filterApps(q:String){
        try{
            listView.removeAllViews()
            allApps.filter{ it.first.contains(q, true) }.take(100).forEach { (label, launch) ->
                listView.addView(TextView(this).apply {
                    text = label; textSize = 16f; setPadding(20,30,20,30); setTextColor(Color.BLACK)
                    setOnClickListener { try{ startActivity(launch) }catch(e:Exception){ Toast.makeText(this@LauncherActivity, e.message, Toast.LENGTH_SHORT).show() } }
                })
            }
        }catch(e:Exception){ ThemeUtil.log(this, "filterApps ${e.message}") }
    }

    private fun openThemes(){
        try{ startActivity(Intent(this, ThemeSettingsActivity::class.java)) }
        catch(e:Exception){ Toast.makeText(this, "Theme err ${e.message}", Toast.LENGTH_LONG).show() }
    }

    override fun onResume() {
        super.onResume()
        try{ findViewById<View>(android.R.id.content)?.setBackgroundColor(ThemeUtil.getBg(this)) }catch(_:Exception){}
    }
}
