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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(b.root)

        setupRecycler()
        setupSearch()
        setupDrawerToggle()
        loadApps()
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
            override fun beforeTextChanged(s: CharSequence?, x: Int, y: Int, z: Int) = Unit
            override fun onTextChanged(s: CharSequence?, x: Int, y: Int, z: Int) {
                adapter.search(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    private fun setupDrawerToggle() {
        b.toggleBtn.setOnClickListener { toggleDrawer() }
    }

    private fun toggleDrawer() {
        isDrawerOpen = !isDrawerOpen
        if (isDrawerOpen) {
            b.drawerLayout.visibility = View.VISIBLE
            b.toggleBtn.rotation = 180f
        } else {
            b.drawerLayout.visibility = View.GONE
            b.toggleBtn.rotation = 0f
        }
    }

    private fun loadApps() {
        b.progress.visibility = View.VISIBLE
        CoroutineScope(Dispatchers.IO).launch {
            val list = queryApps()
            withContext(Dispatchers.Main) {
                adapter.setList(list)
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
