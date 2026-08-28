package com.luncher

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
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

class LauncherActivity : AppCompatActivity() {
    private lateinit var b: ActivityLauncherBinding
    private lateinit var adapter: AppAdapter
    private var isDrawerOpen = false
    private var allApps = listOf<AppInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(b.root)

        setupRecycler()
        setupSearch()
        b.toggleBtn.setOnClickListener { toggleDrawer() }
        
        b.drawerLayout.visibility = View.GONE
        b.toggleBtn.rotation = 0f

        loadAllApps()
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

    // ✅ MÉTHODE QUI RÉCUPÈRE VRAIMENT TOUTES LES APPS
    private fun getAllApps(): List<AppInfo> {
        val pm = packageManager
        val result = mutableListOf<AppInfo>()
        
        // Récupère TOUTES les applications installées
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        
        for (appInfo in packages) {
            try {
                // Vérifie si cette application a une activité de lancement
                val launchIntent = pm.getLaunchIntentForPackage(appInfo.packageName)
                if (launchIntent != null) {
                    val nom = appInfo.loadLabel(pm).toString()
                    val icone = appInfo.loadIcon(pm)
                    result.add(AppInfo(nom, appInfo.packageName, icone))
                }
            } catch (e: Exception) {
                // Ignore les erreurs
            }
        }
        
        // Tri alphabétique
        return result.sortedBy { it.name.lowercase() }
    }
}
