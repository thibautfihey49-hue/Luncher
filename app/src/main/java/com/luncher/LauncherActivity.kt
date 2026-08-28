package com.luncher

import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.button.MaterialButton
import com.luncher.data.AppInfo
import com.luncher.databinding.ActivityLauncherBinding
import com.luncher.ui.FloatingWindowService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LauncherActivity : AppCompatActivity() {
    private lateinit var b: ActivityLauncherBinding
    private lateinit var a: AppAdapter
    private val scope = CoroutineScope(Dispatchers.IO)
    
    private var isDrawerOpen = false
    private var drawerMaxHeight = 0
    private val drawerMinHeight = 160
    
    private val REQ_OVERLAY = 1001
    private val REQ_SMS = 1002
    private val REQ_POST_NOTIFICATIONS = 1004
    
    private val permissionsToRequest = mutableListOf<String>()
    private var currentPermissionIndex = 0

    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        b = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(b.root)
        setupDrawer()
        setupRV()
        setupSearch()
        checkAndRequestPermissionsSequentially()
    }

    private fun setupDrawer() {
        val displayMetrics = resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels
        drawerMaxHeight = (screenHeight * 0.85).toInt()
        b.toggleDrawerBtn.setOnClickListener { toggleDrawer() }
        var startY = 0f
        b.drawerLayout.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> { startY = event.rawY; false }
                MotionEvent.ACTION_MOVE -> {
                    val delta = startY - event.rawY
                    if (delta > 50 && !isDrawerOpen) { openDrawer(); true }
                    else if (delta < -50 && isDrawerOpen) { closeDrawer(); true }
                    else false
                }
                else -> false
            }
        }
    }

    private fun toggleDrawer() = if (isDrawerOpen) closeDrawer() else openDrawer()
    private fun openDrawer() {
        isDrawerOpen = true
        val p = b.drawerLayout.layoutParams as ConstraintLayout.LayoutParams
        p.matchConstraintMaxHeight = drawerMaxHeight
        b.drawerLayout.layoutParams = p
        b.appsRecycler.visibility = View.VISIBLE
        b.toggleDrawerBtn.setIconResource(R.drawable.ic_keyboard_arrow_down)
    }
    private fun closeDrawer() {
        isDrawerOpen = false
        b.appsRecycler.visibility = View.GONE
        val p = b.drawerLayout.layoutParams as ConstraintLayout.LayoutParams
        p.matchConstraintMaxHeight = drawerMinHeight
        b.drawerLayout.layoutParams = p
        b.toggleDrawerBtn.setIconResource(R.drawable.ic_keyboard_arrow_up)
    }

    private fun checkAndRequestPermissionsSequentially() {
        permissionsToRequest.clear()
        currentPermissionIndex = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this))
            permissionsToRequest.add("SYSTEM_ALERT_WINDOW")
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(android.Manifest.permission.READ_SMS)
            permissionsToRequest.add(android.Manifest.permission.RECEIVE_SMS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            permissionsToRequest.add(android.Manifest.permission.POST_NOTIFICATIONS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 && !isNotificationListenerEnabled())
            permissionsToRequest.add("NOTIFICATION_LISTENER")
        if (permissionsToRequest.isEmpty()) { startFloatingWindowService(); loadApps() }
        else showPermissionIntroDialog()
    }

    private fun showPermissionIntroDialog() {
        AlertDialog.Builder(this)
            .setTitle("🔧 Configuration requise")
            .setMessage("Luncher Pro a besoin de quelques permissions.\n\nNous allons vous guider étape par étape.")
            .setPositiveButton("Commencer ▶️") { _, _ -> requestNextPermission() }
            .setCancelable(false)
            .show()
    }

    private fun requestNextPermission() {
        if (currentPermissionIndex >= permissionsToRequest.size) {
            AlertDialog.Builder(this)
                .setTitle("✅ Tout est prêt !")
                .setMessage("👉 Glissez vers le haut ou cliquez sur le bouton en bas pour ouvrir le tiroir !")
                .setPositiveButton("OK") { _, _ -> startFloatingWindowService(); loadApps() }
                .setCancelable(false)
                .show()
            return
        }
        when (permissionsToRequest[currentPermissionIndex]) {
            "SYSTEM_ALERT_WINDOW" -> AlertDialog.Builder(this)
                .setTitle("Étape ${currentPermissionIndex + 1}")
                .setMessage("📱 Afficher par-dessus les autres apps\n\nCochez Luncher Pro ✅")
                .setPositiveButton("Accorder") { _, _ ->
                    val i = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                    startActivityForResult(i, REQ_OVERLAY)
                }.show()
            android.Manifest.permission.READ_SMS -> AlertDialog.Builder(this)
                .setTitle("Étape ${currentPermissionIndex + 1}")
                .setMessage("💬 Accès aux SMS")
                .setPositiveButton("Accorder") { _, _ ->
                    ActivityCompat.requestPermissions(this,
                        arrayOf(android.Manifest.permission.READ_SMS, android.Manifest.permission.RECEIVE_SMS), REQ_SMS)
                }.show()
            android.Manifest.permission.POST_NOTIFICATIONS -> AlertDialog.Builder(this)
                .setTitle("Étape ${currentPermissionIndex + 1}")
                .setMessage("🔔 Afficher les notifications")
                .setPositiveButton("Accorder") { _, _ ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), REQ_POST_NOTIFICATIONS)
                }.show()
            "NOTIFICATION_LISTENER" -> AlertDialog.Builder(this)
                .setTitle("Étape ${currentPermissionIndex + 1}")
                .setMessage("🔔 Accès aux notifications\n\nActivez Luncher Pro ✅")
                .setPositiveButton("Accorder") { _, _ ->
                    startActivityForResult(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"), 1005)
                }.show()
        }
    }

    override fun onActivityResult(rq: Int, rc: Int, d: Intent?) {
        super.onActivityResult(rq, rc, d)
        if (rq == REQ_OVERLAY && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this))
            permissionGranted()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED })
            permissionGranted()
    }

    override fun onResume() {
        super.onResume()
        if (currentPermissionIndex > 0 && currentPermissionIndex < permissionsToRequest.size) {
            if (permissionsToRequest[currentPermissionIndex] == "NOTIFICATION_LISTENER" && isNotificationListenerEnabled())
                permissionGranted()
        }
    }

    private fun permissionGranted() {
        currentPermissionIndex++
        requestNextPermission()
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val enabledListeners = Settings.Secure.getString(contentResolver, Settings.Secure.Settings.Secure.ENABLED_NOTIFICATION_LISTENERS)
        val myListener = "$packageName/.data.NotificationListener"
        return enabledListeners != null && enabledListeners.contains(myListener)
    }

    private fun startFloatingWindowService() {
        startService(Intent(this, FloatingWindowService::class.java))
    }

    private fun setupRV() {
        a = AppAdapter { appInfo ->
            packageManager.getLaunchIntentForPackage(appInfo.packageName)?.let {
                startActivity(it)
                closeDrawer()
            }
        }
        b.appsRecycler.apply {
            adapter = a
            layoutManager = GridLayoutManager(this@LauncherActivity, 5)
            setHasFixedSize(true)
        }
    }

    private fun setupSearch() {
        b.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(c: CharSequence?, s: Int, cnt: Int, a: Int) = Unit
            override fun onTextChanged(c: CharSequence?, s: Int, b: Int, aft: Int) {
                a.filter(c?.toString() ?: "")
            }
            override fun afterTextChanged(e: Editable?) = Unit
        })
    }

    private fun loadApps() {
        b.progress.visibility = View.VISIBLE
        scope.launch {
            val apps = queryApps()
            withContext(Dispatchers.Main) {
                a.setApps(apps)
                b.progress.visibility = View.GONE
            }
        }
    }

    private fun queryApps(): List<AppInfo> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos = packageManager.queryIntentActivities(intent, 0)
        return resolveInfos
            .sortedBy { it.loadLabel(packageManager).toString() }
            .map {
                AppInfo(
                    name = it.loadLabel(packageManager).toString(),
                    packageName = it.activityInfo.packageName,
                    icon = it.loadIcon(packageManager)
                )
            }
    }
}
