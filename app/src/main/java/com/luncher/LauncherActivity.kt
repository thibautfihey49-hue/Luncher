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
        
        // ✅ TIROIR FERMÉ AU DÉMARRAGE
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
                val query = s?.toString()?.trim() ?: ""
                filterApps(query)
            }
            
            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    private fun filterApps(query: String) {
        val filtered = if (query.isEmpty()) {
            allApps
        } else {
            allApps.filter { 
                it.name.contains(query, ignoreCase = true) ||
                it.packageName.contains(query, ignoreCase = true)
            }
        }
        adapter.setList(filtered)
    }

    private fun setupDrawerToggle() {
        b.toggleBtn.setOnClickListener { toggleDrawer() }
    }

    private fun toggleDrawer() {
        isDrawerOpen = !isDrawerOpen
        if (isDrawerOpen) {
            b.drawerLayout.visibility = View.VISIBLE
            b.toggleBtn.rotation = 180f
            b.searchInput.text.clear() // ✅ Efface la recherche quand on ferme
            filterApps("")
        } else {
            b.drawerLayout.visibility = View.GONE
            b.toggleBtn.rotation = 0f
        }
    }

    private fun loadApps() {
        b.progress.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            allApps = queryApps()
            withContext(Dispatchers.Main) {
                adapter.setList(allApps)
                b.progress.visibility = View.GONE
            }
        }
    }

    private fun queryApps(): List<AppInfo> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolved = packageManager.queryIntentActivities(intent, 0)
        return resolved.sortedBy { it.loadLabel(packageManager).toString() }.map {
            AppInfo(
                name = it.loadLabel(packageManager).toString(),
                packageName = it.activityInfo.packageName,
                icon = it.loadIcon(packageManager)
            )
        }
    }
}
