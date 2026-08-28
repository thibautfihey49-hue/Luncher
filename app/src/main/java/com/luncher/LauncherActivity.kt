package com.luncher

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.luncher.data.AppInfo
import com.luncher.databinding.ActivityLauncherBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class LauncherActivity : AppCompatActivity() {
    private lateinit var b: ActivityLauncherBinding
    private lateinit var adapter: AppAdapter
    private var isDrawerOpen = false
    private var allApps = listOf<AppInfo>()
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var timeRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(b.root)

        setupRecycler()
        setupSearch()
        b.toggleBtn.setOnClickListener { toggleDrawer() }
        
        b.drawerLayout.visibility = View.GONE
        b.toggleBtn.rotation = 0f

        setupDateTime()
        loadAllApps()
    }

    // ✅ MISE À JOUR HEURE ET DATE EN TEMPS RÉEL
    private fun setupDateTime() {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.FRANCE)
        val dateFormat = SimpleDateFormat("EEEE d MMMM", Locale.FRANCE)

        fun update() {
            val maintenant = Calendar.getInstance()
            b.heureTexte.text = timeFormat.format(maintenant.time)
            b.dateTexte.text = dateFormat.format(maintenant.time).replaceFirstChar { it.uppercase() }
        }

        update() // Première mise à jour immédiate
        
        // Mise à jour chaque minute
        timeRunnable = object : Runnable {
            override fun run() {
                update()
                handler.postDelayed(this, 60000) // 60 secondes
            }
        }
        handler.post(timeRunnable)
    }

    private fun setupRecycler() {
        adapter = AppAdapter { app ->
            packageManager.getLaunchIntentForPackage(app.packageName)?.let { startActivity(it) }
        }
        b.appsRecycler.adapter = adapter
        b.appsRecycler.layoutManager = GridLayoutManager(this, 4)
    }

    private fun setupSearch() {
        b.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, x: Int, y: Int, z: Int) = Unit
            override fun onTextChanged(s: CharSequence?, x: Int, y: Int, z: Int) {
                val q = s?.toString() ?: ""
                adapter.setList(if (q.isEmpty()) allApps else allApps.filter { 
                    it.name.contains(q, ignoreCase = true) 
                })
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    private fun toggleDrawer() {
        isDrawerOpen = !isDrawerOpen
        if (isDrawerOpen) {
            b.drawerLayout.visibility = View.VISIBLE
            b.toggleBtn.rotation = 180f
            b.searchInput.text.clear()
            adapter.setList(allApps)
        } else {
            b.drawerLayout.visibility = View.GONE
            b.toggleBtn.rotation = 0f
        }
    }

    private fun loadAllApps() {
        b.progress.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            allApps = getAllApps()
            withContext(Dispatchers.Main) {
                adapter.setList(allApps)
                b.progress.visibility = View.GONE
            }
        }
    }

    private fun getAllApps(): List<AppInfo> {
        val pm = packageManager
        val uniquePackages = mutableSetOf<String>()
        val result = mutableListOf<AppInfo>()
        
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val launcherApps = pm.queryIntentActivities(intent, 0)
        for (resolveInfo in launcherApps) {
            try {
                val pkg = resolveInfo.activityInfo.packageName
                if (uniquePackages.add(pkg)) {
                    val name = resolveInfo.loadLabel(pm).toString()
                    val icon = resolveInfo.loadIcon(pm)
                    result.add(AppInfo(name, pkg, icon))
                }
            } catch (e: Exception) {}
        }
        
        val installed = pm.getInstalledApplications(0)
        for (appInfo in installed) {
            try {
                val pkg = appInfo.packageName
                val launchIntent = pm.getLaunchIntentForPackage(pkg)
                if (launchIntent != null && uniquePackages.add(pkg)) {
                    val name = appInfo.loadLabel(pm).toString()
                    val icon = appInfo.loadIcon(pm)
                    result.add(AppInfo(name, pkg, icon))
                }
            } catch (e: Exception) {}
        }
        
        return result.sortedBy { it.name.lowercase() }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(timeRunnable)
    }
}
