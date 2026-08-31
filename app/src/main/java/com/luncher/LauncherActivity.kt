package com.luncher

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.luncher.util.ThemeUtil
import com.thibautfihey.luncher.ThemeSettingsActivity

class LauncherActivity : AppCompatActivity() {
    private lateinit var listView: LinearLayout
    private lateinit var root: LinearLayout
    private var allApps: List<Pair<String, Intent>> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUI()
        loadAllApps()
        filterApps("")
    }

    private fun buildUI(){
        val bg = ThemeUtil.getBg(this)
        val txt = ThemeUtil.getTextColor(bg)

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
            setPadding(40, 80, 40, 20)
        }

        // Titre Luncher comme avant
        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        header.addView(TextView(this).apply {
            text = "Luncher"; textSize = 32f; setTextColor(txt); typeface = android.graphics.Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        })
        header.addView(TextView(this).apply {
            text = "🔧"; textSize = 26f; setPadding(20,10,10,10)
            setOnClickListener { openThemes() }
        })
        root.addView(header)

        // Search bar comme avant - arrondi
        val search = EditText(this).apply {
            hint = "Search apps..."
            setHintTextColor(Color.parseColor("#AAAAAA"))
            setTextColor(Color.BLACK)
            setBackgroundResource(android.R.drawable.editbox_background)
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.RECTANGLE
                cornerRadius = 60f
                setColor(Color.WHITE)
                setStroke(2, Color.parseColor("#E5E5E5"))
            }
            setPadding(40,30,40,30)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply{ setMargins(0,30,0,30) }
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

        val bottom = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(0,20,0,0) }
        bottom.addView(createBottomBtn("Phone", "com.luncher.PhoneAppActivity", txt))
        bottom.addView(createBottomBtn("SMS", "com.luncher.SmsAppActivity", txt))
        bottom.addView(createBottomBtn("Files", "com.luncher.FileManagerActivity", txt))
        root.addView(bottom)

        root.setOnLongClickListener { openThemes(); true }
        setContentView(root)
    }

    private fun createBottomBtn(name:String, cls:String, txt:Int): Button {
        return Button(this).apply {
            text = name; setTextColor(Color.parseColor("#6A5ACD"))
            setBackgroundResource(android.R.drawable.btn_default)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply{ setMargins(10,0,10,0) }
            setOnClickListener { try{ startActivity(Intent().setClassName(packageName, cls)) }catch(_:Exception){} }
        }
    }

    private fun loadAllApps(){
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply{ addCategory(Intent.CATEGORY_LAUNCHER) }
        allApps = pm.queryIntentActivities(intent, 0).mapNotNull {
            val launch = pm.getLaunchIntentForPackage(it.activityInfo.packageName) ?: return@mapNotNull null
            it.loadLabel(pm).toString() to launch
        }.sortedBy{ it.first.lowercase() }
    }

    private fun filterApps(q:String){
        val bg = ThemeUtil.getBg(this)
        val txt = ThemeUtil.getTextColor(bg)
        listView.removeAllViews()
        allApps.filter{ it.first.contains(q, true) }.take(100).forEach { (label, launch) ->
            listView.addView(TextView(this).apply {
                text = label; textSize = 18f; setPadding(20,28,20,28); setTextColor(txt)
                setOnClickListener { try{ startActivity(launch) }catch(_:Exception){} }
            })
        }
    }

    private fun openThemes(){ startActivity(Intent(this, ThemeSettingsActivity::class.java)) }

    override fun onResume() {
        super.onResume()
        // RECONSTRUIT tout pour appliquer le thème
        buildUI()
        filterApps("")
    }
}
