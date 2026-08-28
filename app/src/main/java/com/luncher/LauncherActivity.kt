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
            allApps = getAllInstalledApps()
            withContext(Dispatchers.Main) {
                adapter.updateListe(allApps)
                b.progress.visibility = View.GONE
            }
        }
    }

    // ✅ MÉTHODE QUI RÉCUPÈRE TOUTES LES APPS
    private fun getAllInstalledApps(): List<AppInfo> {
        val pm = packageManager
        val apps = mutableListOf<AppInfo>()
        
        // Récupère TOUTES les applications installées
        val installedPackages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        
        for (appInfo in installedPackages) {
            try {
                // Vérifie que l'app a une activité de lancement
                val launchIntent = pm.getLaunchIntentForPackage(appInfo.packageName)
                if (launchIntent != null) {
                    val nom = appInfo.loadLabel(pm).toString()
                    val icone = appInfo.loadIcon(pm)
                    apps.add(
                        AppInfo(
                            name = nom,
                            packageName = appInfo.packageName,
                            icon = icone
                        )
                    )
                }
            } catch (e: Exception) {
                // Ignore les apps qui ne peuvent pas être chargées
            }
        }
        
        // Tri par ordre alphabétique
        return apps.sortedBy { it.name.lowercase() }
    }
}
