package com.luncher

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.luncher.data.AppInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class LauncherActivity : AppCompatActivity() {
    
    private lateinit var heureTexte: TextView
    private lateinit var dateTexte: TextView
    private lateinit var dragHandle: View
    private lateinit var drawerLayout: LinearLayout
    private lateinit var searchInput: EditText
    private lateinit var appsRecycler: RecyclerView
    private lateinit var rootLayout: View
    private lateinit var timeContainer: LinearLayout
    private lateinit var permissionOverlay: LinearLayout
    
    private lateinit var adapter: AppAdapter
    private lateinit var prefs: SharedPreferences
    private var isDrawerOpen = false
    private var allApps = listOf<AppInfo>()
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var timeRunnable: Runnable
    private var loadJob: Job? = null
    private val isLoaded = AtomicBoolean(false)
    
    private var startY = 0f
    private var drawerOffset = 0f
    private val screenHeight by lazy { resources.displayMetrics.heightPixels.toFloat() }
    private val touchThreshold = 80f
    
    private var isDraggingTime = false
    private var timeStartX = 0f
    private var timeStartY = 0f
    private var timeViewStartX = 0f
    private var timeViewStartY = 0f
    private val LONG_PRESS_THRESHOLD = 500L
    private var pressStartTime = 0L
    private var hasMovedDuringPress = false

    private val PREFS_NAME = "LuncherPrefs"
    private val KEY_WALLPAPER_URI = "wallpaper_uri"
    private val KEY_TEXT_COLOR = "text_color"
    private val KEY_HOUR_X = "hour_x"
    private val KEY_HOUR_Y = "hour_y"
    private val KEY_PERMISSIONS_DONE = "permissions_done"

    private val colorNames = listOf(
        "⚫ Noir", "⚪ Blanc", "🔴 Rouge", "🟡 Jaune", "🟠 Or",
        "🔵 Cyan", "🟢 Vert", "🟣 Violet", "🟠 Orange", "💗 Rose", "🔵 Bleu"
    )
    private val colorValues = listOf(
        Color.BLACK, Color.WHITE, Color.RED, Color.YELLOW, Color.parseColor("#FFD700"),
        Color.CYAN, Color.GREEN, Color.parseColor("#9C27B0"), Color.parseColor("#FF9800"),
        Color.parseColor("#FF4081"), Color.parseColor("#2196F3")
    )

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                saveWallpaperUri(uri)
                setWallpaperFromUri(uri)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        var allGranted = true
        permissions.entries.forEach { 
            if (!it.value) allGranted = false
        }
        if (allGranted) checkAllPermissionsAndProceed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)
        
        heureTexte = findViewById(R.id.heure_texte)
        dateTexte = findViewById(R.id.date_texte)
        dragHandle = findViewById(R.id.drag_handle)
        drawerLayout = findViewById(R.id.drawer_layout)
        searchInput = findViewById(R.id.search_input)
        appsRecycler = findViewById(R.id.apps_recycler)
        rootLayout = findViewById(R.id.root_layout)
        timeContainer = findViewById(R.id.time_container)
        permissionOverlay = findViewById(R.id.permission_overlay)

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        setupRecycler()
        setupSearch()
        setupDrawerGestures()
        setupDateTime()
        setupTimeGestures()
        setupLongPressActions()
        loadSavedWallpaper()
        loadTextColor()
        loadTimePosition()
        
        if (!prefs.getBoolean(KEY_PERMISSIONS_DONE, false)) {
            showPermissionScreen()
        } else {
            checkOverlayPermission()
        }
    }

    private fun setupTimeGestures() {
        timeContainer.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    pressStartTime = System.currentTimeMillis()
                    hasMovedDuringPress = false
                    isDraggingTime = false
                    timeStartX = event.rawX
                    timeStartY = event.rawY
                    timeViewStartX = view.x
                    timeViewStartY = view.y
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = abs(event.rawX - timeStartX)
                    val deltaY = abs(event.rawY - timeStartY)
                    if (deltaX > 10 || deltaY > 10) {
                        hasMovedDuringPress = true
                        if (!isDraggingTime && System.currentTimeMillis() - pressStartTime >= LONG_PRESS_THRESHOLD) {
                            isDraggingTime = true
                        }
                        if (isDraggingTime) {
                            view.x = timeViewStartX + (event.rawX - timeStartX)
                            view.y = timeViewStartY + (event.rawY - timeStartY)
                        }
                    }
                }
                MotionEvent.ACTION_UP -> {
                    val pressDuration = System.currentTimeMillis() - pressStartTime
                    when {
                        isDraggingTime -> {
                            prefs.edit {
                                putFloat(KEY_HOUR_X, view.x)
                                putFloat(KEY_HOUR_Y, view.y)
                            }
                            Toast.makeText(this, "✅ Position sauvegardée", Toast.LENGTH_SHORT).show()
                        }
                        pressDuration < LONG_PRESS_THRESHOLD && !hasMovedDuringPress -> {
                            showColorPickerDialog()
                        }
                    }
                    isDraggingTime = false
                }
            }
            true
        }
    }

    private fun setupDrawerGestures() {
        dragHandle.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> startY = event.rawY
                MotionEvent.ACTION_UP -> {
                    if (startY - event.rawY > touchThreshold) {
                        openDrawer()
                    }
                }
            }
            true
        }

        drawerLayout.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startY = event.rawY
                    drawerOffset = 0f
                }
                MotionEvent.ACTION_MOVE -> {
                    if (isDrawerOpen) {
                        val deltaY = event.rawY - startY
                        if (deltaY > 0) {
                            drawerOffset = deltaY
                            drawerLayout.translationY = deltaY
                        }
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (drawerOffset > touchThreshold) {
                        closeDrawer()
                    } else if (isDrawerOpen) {
                        resetDrawerPosition()
                    }
                    drawerOffset = 0f
                }
            }
            true
        }
    }

    private fun openDrawer() {
        isDrawerOpen = true
        drawerLayout.visibility = View.VISIBLE
        ObjectAnimator.ofFloat(drawerLayout, View.TRANSLATION_Y, 0f).apply {
            duration = 300
            interpolator = DecelerateInterpolator(1.5f)
            start()
        }
    }

    private fun closeDrawer() {
        isDrawerOpen = false
        ObjectAnimator.ofFloat(drawerLayout, View.TRANSLATION_Y, screenHeight).apply {
            duration = 300
            interpolator = DecelerateInterpolator(1.2f)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    drawerLayout.visibility = View.GONE
                }
            })
            start()
        }
    }

    private fun resetDrawerPosition() {
        ObjectAnimator.ofFloat(drawerLayout, View.TRANSLATION_Y, 0f).apply {
            duration = 150
            start()
        }
    }

    private fun checkAllPermissionsAndProceed() {
        if (!isNotificationListenerEnabled()) {
            startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
            handler.postDelayed({ checkNotificationListener() }, 2000)
        } else {
            checkOverlayPermissionStep()
        }
    }

    private fun checkNotificationListener() {
        if (isNotificationListenerEnabled()) {
            checkOverlayPermissionStep()
        }
    }

    private fun checkOverlayPermissionStep() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            handler.postDelayed({ 
                if (Settings.canDrawOverlays(this)) hidePermissionScreen()
            }, 2000)
        } else {
            hidePermissionScreen()
        }
    }

    private fun checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        }
        if (!isLoaded.get()) loadAllApps()
    }

    private fun showPermissionScreen() {
        permissionOverlay.visibility = View.VISIBLE
        permissionOverlay.findViewById<Button>(R.id.btn_grant_all).setOnClickListener {
            requestAllPermissionsSequentially()
        }
    }

    private fun hidePermissionScreen() {
        permissionOverlay.visibility = View.GONE
        prefs.edit { putBoolean(KEY_PERMISSIONS_DONE, true) }
        if (!isLoaded.get()) loadAllApps()
    }

    private fun requestAllPermissionsSequentially() {
        val permissionsNeeded = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED)
                permissionsNeeded.add(Manifest.permission.READ_MEDIA_IMAGES)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
                permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.READ_SMS)
            permissionsNeeded.add(Manifest.permission.RECEIVE_SMS)
            permissionsNeeded.add(Manifest.permission.SEND_SMS)
        }
        if (permissionsNeeded.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsNeeded.toTypedArray())
            return
        }
        checkAllPermissionsAndProceed()
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val enabled = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return enabled?.contains(packageName) == true
    }

    private fun setupLongPressActions() {
        rootLayout.setOnLongClickListener { changerFondEcran(); true }
    }

    private fun showColorPickerDialog() {
        val currentColor = prefs.getInt(KEY_TEXT_COLOR, Color.WHITE)
        val selectedIndex = colorValues.indexOf(currentColor).coerceAtLeast(0)
        android.app.AlertDialog.Builder(this)
            .setTitle("🎨 Couleur du texte")
            .setSingleChoiceItems(colorNames.toTypedArray(), selectedIndex) { dialog, which ->
                val newColor = colorValues[which]
                heureTexte.setTextColor(newColor)
                dateTexte.setTextColor(newColor)
                prefs.edit { putInt(KEY_TEXT_COLOR, newColor) }
                dialog.dismiss()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun loadTextColor() {
        val savedColor = prefs.getInt(KEY_TEXT_COLOR, Color.WHITE)
        heureTexte.setTextColor(savedColor)
        dateTexte.setTextColor(savedColor)
    }

    private fun loadTimePosition() {
        val x = prefs.getFloat(KEY_HOUR_X, Float.NaN)
        val y = prefs.getFloat(KEY_HOUR_Y, Float.NaN)
        if (!x.isNaN() && !y.isNaN()) {
            timeContainer.x = x
            timeContainer.y = y
        }
    }

    private fun changerFondEcran() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(Intent.ACTION_PICK).setType("image/*")
            pickImageLauncher.launch(Intent.createChooser(intent, "Choisir une image"))
        } else {
            requestPermissionLauncher.launch(arrayOf(permission))
        }
    }

    private fun saveWallpaperUri(uri: Uri) {
        prefs.edit { putString(KEY_WALLPAPER_URI, uri.toString()) }
    }

    private fun loadSavedWallpaper() {
        prefs.getString(KEY_WALLPAPER_URI, null)?.let { uriStr ->
            try { setWallpaperFromUri(Uri.parse(uriStr)) }
            catch (e: Exception) { prefs.edit { remove(KEY_WALLPAPER_URI) } }
        }
    }

    private fun setWallpaperFromUri(uri: Uri) {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream)
                rootLayout.background = BitmapDrawable(resources, bitmap).apply { setFilterBitmap(true) }
            }
        } catch (e: Exception) { }
    }

    private fun setupDateTime() {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.FRANCE)
        val dateFormat = SimpleDateFormat("EEEE d MMMM", Locale.FRANCE)
        fun update() {
            val now = Calendar.getInstance()
            heureTexte.text = timeFormat.format(now.time)
            dateTexte.text = dateFormat.format(now.time).replaceFirstChar { it.uppercase() }
        }
        update()
        timeRunnable = object : Runnable {
            override fun run() {
                update()
                handler.postDelayed(this, 60000)
            }
        }
        handler.post(timeRunnable)
    }

    private fun setupRecycler() {
        adapter = AppAdapter { app ->
            try {
                val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
                if (launchIntent != null) startActivity(launchIntent)
            } catch (e: Exception) { }
        }
        appsRecycler.adapter = adapter
        appsRecycler.layoutManager = GridLayoutManager(this, 4)
    }

    private fun setupSearch() {
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, x: Int, y: Int, z: Int) = Unit
            override fun onTextChanged(s: CharSequence?, x: Int, y: Int, z: Int) {
                adapter.filter(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    private fun loadAllApps() {
        if (isLoaded.get()) return
        loadJob?.cancel()
        isLoaded.set(false)
        loadJob = CoroutineScope(Dispatchers.IO).launch {
            val pm = packageManager
            val apps = mutableListOf<AppInfo>()
            
            val intent = Intent(Intent.ACTION_MAIN, null)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            val resolveInfos = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            
            val selfPackage = packageName
            
            for (ri in resolveInfos) {
                try {
                    val packageName = ri.activityInfo.packageName
                    if (packageName == selfPackage) continue
                    
                    val appInfo = pm.getApplicationInfo(packageName, 0)
                    val name = pm.getApplicationLabel(appInfo).toString().trim()
                    if (name.isEmpty()) continue
                    
                    val icon = pm.getApplicationIcon(packageName)
                    val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    
                    apps.add(AppInfo(name, packageName, icon, isSystemApp))
                } catch (e: Exception) {
                    try {
                        val packageName = ri.activityInfo.packageName
                        val name = ri.loadLabel(pm).toString().trim()
                        if (name.isEmpty() || packageName == selfPackage) continue
                        val icon = ri.loadIcon(pm)
                        apps.add(AppInfo(name, packageName, icon, false))
                    } catch (e2: Exception) {}
                }
            }
            
            val finalList = apps.sortedBy { it.name.lowercase() }
            
            withContext(Dispatchers.Main) {
                allApps = finalList
                adapter.setList(finalList)
                isLoaded.set(true)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(timeRunnable)
        loadJob?.cancel()
    }
}
