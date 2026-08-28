package com.luncher

import android.content.Intent
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
        loadApps()

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

    private fun loadApps() {
        b.progress.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            allApps = getAppsOfficialMethod()
            withContext(Dispatchers.Main) {
                adapter.updateListe(allApps)
                b.progress.visibility = View.GONE
            }
        }
    }

    // ✅ LA MÉTHODE OFFICIELLE — COMME TOUS LES LANCEURS
    private fun getAppsOfficialMethod(): List<AppInfo> {
        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        
        // ✅ CECI EST LA SEULE BONNE MÉTHODE
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        
        val result = mutableListOf<AppInfo>()
        
        for (resolveInfo in resolveInfos) {
            try {
                val nom = resolveInfo.loadLabel(pm).toString()
                val paquet = resolveInfo.activityInfo.packageName
                val icone = resolveInfo.loadIcon(pm)
                
                result.add(AppInfo(nom, paquet, icone))
            } catch (e: Exception) {
                // Ignorer
            }
        }
        
        // Tri alphabétique
        return result.sortedBy { it.name.lowercase() }
    }
}
