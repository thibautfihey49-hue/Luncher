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
    private var allApps: List<AppInfo> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(b.root)

        setupRecycler()
        setupSearch()
        setupDrawerToggle()
        loadAllApplications()

        b.drawerLayout.visibility = View.GONE
        b.toggleBtn.rotation = 0f
    }

    private fun setupRecycler() {
        adapter = AppAdapter { app ->
            packageManager.getLaunchIntentForPackage(app.packageName)?.let {
                startActivity(it)
            }
        }
        b.appsRecycler.apply {
            adapter = this@LauncherActivity.adapter
            layoutManager = GridLayoutManager(this@LauncherActivity, 4)
        }
    }

    private fun setupSearch() {
        b.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val terme = s?.toString()?.trim() ?: ""
                filtrer(terme)
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    private fun filtrer(terme: String) {
        val resultat = if (terme.isEmpty()) {
            allApps
        } else {
            allApps.filter { 
                it.name.contains(terme, ignoreCase = true) ||
                it.packageName.contains(terme, ignoreCase = true)
            }
        }
        adapter.updateListe(resultat)
    }

    private fun setupDrawerToggle() {
        b.toggleBtn.setOnClickListener { toggleDrawer() }
    }

    private fun toggleDrawer() {
        isDrawerOpen = !isDrawerOpen
        if (isDrawerOpen) {
            b.drawerLayout.visibility = View.VISIBLE
            b.toggleBtn.rotation = 180f
            b.searchInput.text.clear()
            filtrer("")
        } else {
            b.drawerLayout.visibility = View.GONE
            b.toggleBtn.rotation = 0f
        }
    }

    private fun loadAllApplications() {
        b.progress.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            allApps = getAllApps()
            withContext(Dispatchers.Main) {
                adapter.updateListe(allApps)
                b.progress.visibility = View.GONE
            }
        }
    }

    // ✅ MÉTHODE LA PLUS COMPLÈTE POSSIBLE
    private fun getAllApps(): List<AppInfo> {
        val pm = packageManager
        val result = mutableListOf<AppInfo>()
        
        // Récupère TOUTES les applications installées
        val packages = pm.getInstalledApplications(0)
        
        for (app in packages) {
            try {
                // Exclut uniquement les apps du système sans icône
                val isSystemApp = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val isUpdatedSystemApp = (app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                
                // Inclut TOUTES les apps utilisateur + les apps système mises à jour
                if (!isSystemApp || isUpdatedSystemApp) {
                    val intent = pm.getLaunchIntentForPackage(app.packageName)
                    if (intent != null) {
                        val name = app.loadLabel(pm).toString()
                        val icon = app.loadIcon(pm)
                        result.add(AppInfo(name, app.packageName, icon))
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
        
        return result.sortedBy { it.name.lowercase() }
    }
}
