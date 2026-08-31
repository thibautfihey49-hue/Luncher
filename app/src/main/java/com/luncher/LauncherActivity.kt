
package com.luncher
import android.content.Intent
import android.os.Bundle
import android.content.Intent
import com.thibautfihey.luncher.ThemeSettingsActivity
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LauncherActivity : AppCompatActivity() {
    private var allApps: List<AppInfo> = emptyList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_launcher)
 try { window.decorView.post { try { com.thibautfihey.luncher.attachThemeButton(window.decorView.rootView); findViewById<android.view.View>(android.R.id.content)?.setOnClickListener { startActivity(Intent(this, ThemeSettingsActivity::class.java)) } } catch(e:Exception){} } } catch(e:Exception){}
            val recycler = findViewById<RecyclerView>(R.id.recyclerApps)
            val search = findViewById<EditText>(R.id.searchBar)
            recycler.layoutManager = GridLayoutManager(this, 4)
            val adapter = AppAdapter(mutableListOf()) { app ->
                try { startActivity(packageManager.getLaunchIntentForPackage(app.packageName)) } catch (_: Exception) {}
            }
            recycler.adapter = adapter
            findViewById<View>(R.id.btnPhone)?.setOnClickListener { try { startActivity(Intent(this, PhoneAppActivity::class.java)) } catch (_: Exception) {} }
            findViewById<View>(R.id.btnSms)?.setOnClickListener { try { startActivity(Intent(this, SmsAppActivity::class.java)) } catch (_: Exception) {} }
            findViewById<View>(R.id.btnFiles)?.setOnClickListener { try { startActivity(Intent(this, FileManagerActivity::class.java)) } catch (_: Exception) {} }
            search.addTextChangedListener(object : android.text.TextWatcher {
                override fun afterTextChanged(s: android.text.Editable?) {
                    val q = s.toString()
                    if (q.isBlank()) adapter.update(allApps)
                    else adapter.update(allApps.filter { it.label.contains(q, true) })
                }
                override fun beforeTextChanged(a: CharSequence?, b: Int, c: Int, d: Int) {}
                override fun onTextChanged(a: CharSequence?, b: Int, c: Int, d: Int) {}
            })
            Thread {
                try {
                    val pm = packageManager
                    val intent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
                    val resolve = pm.queryIntentActivities(intent, 0)
                    val apps = resolve.mapNotNull {
                        try {
                            val label = it.loadLabel(pm).toString()
                            val pkg = it.activityInfo.packageName
                            // ne cache plus rien sauf nous meme
                            if (pkg == packageName) null else AppInfo(label, pkg, it.activityInfo.name, it.loadIcon(pm))
                        } catch (_: Exception) { null }
                    }.sortedBy { it.label.lowercase() }
                    runOnUiThread {
                        allApps = apps
                        adapter.update(apps)
                        Toast.makeText(this, "${apps.size} apps trouvees", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    runOnUiThread { Toast.makeText(this, "Erreur apps: " + e.message, Toast.LENGTH_LONG).show() }
                }
            }.start()
        } catch (e: Exception) {
            val tv = TextView(this)
            tv.text = "Launcher erreur: " + e.message
            setContentView(tv)
        }
    }
}
