package com.luncher

import android.content.Intent
import android.content.pm.PackageManager
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try{
            val root = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(ThemeUtil.getBg(this@LauncherActivity))
                setPadding(24,80,24,24)
            }

            // header
            val header = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
            header.addView(TextView(this).apply {
                text = "Luncher"; textSize = 28f; setTextColor(Color.BLACK)
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            })
            val wrench = TextView(this).apply {
                text = "🔧"; textSize = 24f; setPadding(20,20,20,20)
                isClickable = true
                setOnClickListener { openThemes() }
            }
            header.addView(wrench)
            root.addView(header)

            search = EditText(this).apply {
                hint = "Search apps..."
                setBackgroundResource(android.R.drawable.editbox_background)
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply{setMargins(0,20,0,20)}
                addTextChangedListener(object: android.text.TextWatcher{
                    override fun afterTextChanged(s: android.text.Editable?) { filterApps(s.toString()) }
                    override fun beforeTextChanged(s: CharSequence?, a:Int,b:Int,c:Int){}
                    override fun onTextChanged(s: CharSequence?, a:Int,b:Int,c:Int){}
                })
            }
            root.addView(search)

            val scroll = ScrollView(this).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f)
            }
            listView = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            scroll.addView(listView)
            root.addView(scroll)

            // bottom bar
            val bottom = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0,20,0,0)
            }
            listOf("Phone" to PhoneAppActivity::class.java, "SMS" to SmsAppActivity::class.java, "Files" to FileManagerActivity::class.java).forEach { (name, cls) ->
                bottom.addView(Button(this).apply {
                    text = name; layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply{setMargins(10,0,10,0)}
                    setOnClickListener { try{ startActivity(Intent(this@LauncherActivity, cls)) }catch(e:Exception){ Toast.makeText(this@LauncherActivity, e.message, Toast.LENGTH_SHORT).show() } }
                })
            }
            root.addView(bottom)

            root.setOnLongClickListener { openThemes(); true }

            setContentView(root)
            loadApps("")
            Toast.makeText(this, "🔧 = themes (appui long aussi)", Toast.LENGTH_SHORT).show()

        }catch(e:Exception){
            ThemeUtil.log(this, "Launcher onCreate crash ${e.message}\n${e.stackTraceToString()}")
            val tv = TextView(this).apply{ text = "Launcher crash:\n${e.message}\n\nRegarde files/luncher_debug.txt"; setTextColor(Color.RED); setPadding(20,20,20,20)}
            setContentView(tv)
        }
    }

    private var allApps: List<Pair<String, Intent>> = emptyList()
    private fun loadApps(filter:String){ filterApps(filter) }
    private fun filterApps(q:String){
        try{
            if(allApps.isEmpty()){
                val pm = packageManager
                val intent = Intent(Intent.ACTION_MAIN, null).apply{ addCategory(Intent.CATEGORY_LAUNCHER) }
                allApps = pm.queryIntentActivities(intent, 0).map{
                    it.loadLabel(pm).toString() to pm.getLaunchIntentForPackage(it.activityInfo.packageName)!!
                }.filter{ it.second != null }.sortedBy{ it.first.lowercase() }
            }
            listView.removeAllViews()
            allApps.filter{ it.first.contains(q, true) }.take(100).forEach { (label, launch) ->
                val tv = TextView(this).apply {
                    text = label; textSize = 16f; setPadding(20,30,20,30); setTextColor(Color.BLACK)
                    setOnClickListener { try{ startActivity(launch) }catch(e:Exception){ Toast.makeText(this@LauncherActivity, "Err ${e.message}", Toast.LENGTH_SHORT).show() } }
                }
                listView.addView(tv)
            }
        }catch(e:Exception){ ThemeUtil.log(this, "filterApps err ${e.message}") }
    }

    private fun openThemes(){
        try{ startActivity(Intent(this, ThemeSettingsActivity::class.java)) }
        catch(e:Exception){ Toast.makeText(this, "Theme err: ${e.message}", Toast.LENGTH_LONG).show(); ThemeUtil.log(this, "openThemes ${e.message}") }
    }

    override fun onResume() {
        super.onResume()
        try{ findViewById<View>(android.R.id.content)?.setBackgroundColor(ThemeUtil.getBg(this)) }catch(_:Exception){}
    }
}
