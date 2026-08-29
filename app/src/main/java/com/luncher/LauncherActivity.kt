package com.luncher

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.luncher.data.AppInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class LauncherActivity : AppCompatActivity() {
    
    private lateinit var heureTexte: TextView
    private lateinit var dateTexte: TextView
    private lateinit var toggleBtn: ImageView
    private lateinit var drawerLayout: LinearLayout
    private lateinit var searchInput: EditText
    private lateinit var appsRecycler: RecyclerView
    private lateinit var rootLayout: LinearLayout
    private lateinit var timeContainer: LinearLayout
    private lateinit var statusText: TextView
    private lateinit var permissionOverlay: LinearLayout
    
    private lateinit var adapter: AppAdapter
    private lateinit var prefs: SharedPreferences
    private var isDrawerOpen = false
    private var allApps = listOf<AppInfo>()
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var timeRunnable: Runnable

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
            if (!it.value) {
                allGranted = false
                Toast.makeText(this, "⚠️ Permission refusée : ${it.key}", Toast.LENGTH_LONG).show()
            }
        }
        if (allGranted) {
            checkAllPermissionsAndProceed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)
        
        heureTexte = findViewById(R.id.heure_texte)
        dateTexte = findViewById(R.id.date_texte)
        toggleBtn = findViewById(R.id.toggle_btn)
        drawerLayout = findViewById(R.id.drawer_layout)
        searchInput = findViewById(R.id.search_input)
        appsRecycler = findViewById(R.id.apps_recycler)
        rootLayout = findViewById(R.id.root_layout)
        timeContainer = findViewById(R.id.time_container)
        statusText = findViewById(R.id.status_text)
        permissionOverlay = findViewById(R.id.permission_overlay)

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        setupRecycler()
        setupSearch()
        toggleBtn.setOnClickListener { toggleDrawer() }
        drawerLayout.visibility = View.GONE
        toggleBtn.rotation = 0f

        setupDateTime()
        setupDraggableTime()
        setupLongPressActions()
        loadSavedWallpaper()
        loadTextColor()
        loadTimePosition()
        
        if (!prefs.getBoolean(KEY_PERMISSIONS_DONE, false)) {
            showPermissionScreen()
        } else {
            loadAllApps()
        }
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
        loadAllApps()
    }

    private fun requestAllPermissionsSequentially() {
        val permissionsNeeded = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_MEDIA_IMAGES)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
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
        
        if (!isNotificationListenerEnabled()) {
            Toast.makeText(this, "👉 ÉTAPE 1/2 : Accès aux notifications", Toast.LENGTH_LONG).show()
            val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            startActivity(intent)
        } else {
            hidePermissionScreen()
        }
        
        handler.postDelayed({ checkAllPermissionsAndProceed() }, 2500)
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), packageName)
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun checkAllPermissionsAndProceed() {
        val hasNotifAccess = isNotificationListenerEnabled()
        
        if (hasNotifAccess) {
            Toast.makeText(this, "✅ Toutes les permissions accordées !", Toast.LENGTH_LONG).show()
            hidePermissionScreen()
        } else {
            val missing = mutableListOf<String>()
            if (!hasNotifAccess) missing.add("• Accès aux notifications")
            if (missing.isNotEmpty()) {
                Toast.makeText(this, "⚠️ Il manque :\n${missing.joinToString("\n")}\n\nAppuyez à nouveau", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val enabledListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return enabledListeners?.contains(packageName) == true
    }

    private fun setupDraggableTime() {
        var dX = 0f
        var dY = 0f
        
        timeContainer.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    dX = view.x - event.rawX
                    dY = view.y - event.rawY
                    true
                }
                android.view.MotionEvent.ACTION_MOVE -> {
                    view.x = event.rawX + dX
                    view.y = event.rawY + dY
                    true
                }
                android.view.MotionEvent.ACTION_UP -> {
                    prefs.edit {
                        putFloat(KEY_HOUR_X, view.x)
                        putFloat(KEY_HOUR_Y, view.y)
                    }
                    true
                }
                else -> false
            }
        }
    }
    
    private fun loadTimePosition() {
        val x = prefs.getFloat(KEY_HOUR_X, Float.NaN)
        val y = prefs.getFloat(KEY_HOUR_Y, Float.NaN)
        if (!x.isNaN() && !y.isNaN()) {
            timeContainer.x = x
            timeContainer.y = y
        }
    }

    private fun setupLongPressActions() {
        rootLayout.setOnLongClickListener {
            changerFondEcran()
            true
        }
        heureTexte.setOnLongClickListener { showColorPickerDialog(); true }
        dateTexte.setOnLongClickListener { showColorPickerDialog(); true }
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

    private fun changerFondEcran() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
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
                if (launchIntent != null) {
                    startActivity(launchIntent)
                } else {
                    Toast.makeText(this, "Impossible d'ouvrir ${app.name}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Erreur : ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        appsRecycler.adapter = adapter
        appsRecycler.layoutManager = LinearLayoutManager(this)
    }

    private fun setupSearch() {
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, x: Int, y: Int, z: Int) = Unit
            override fun onTextChanged(s: CharSequence?, x: Int, y: Int, z: Int) {
                val q = s?.toString() ?: ""
                adapter.filter(q)
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    private fun toggleDrawer() {
        isDrawerOpen = !isDrawerOpen
        if (isDrawerOpen) {
            drawerLayout.visibility = View.VISIBLE
            toggleBtn.rotation = 180f
        } else {
            drawerLayout.visibility = View.GONE
            toggleBtn.rotation = 0f
        }
    }

    private fun loadAllApps() {
        statusText.text = "🔄 Chargement des applications..."
        
        CoroutineScope(Dispatchers.IO).launch {
            val pm = packageManager
            val apps = mutableListOf<AppInfo>()
            
            val mainIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            val resolveInfos = pm.queryIntentActivities(mainIntent, PackageManager.MATCH_ALL)
            
            Log.d("LUNCHER", "Apps trouvées: ${resolveInfos.size}")
            
            for (ri in resolveInfos) {
                try {
                    val packageName = ri.activityInfo.packageName
                    if (packageName == this@LauncherActivity.packageName) continue
                    
                    val appInfo = pm.getApplicationInfo(packageName, 0)
                    val name = appInfo.loadLabel(pm).toString().trim()
                    
                    if (name.isEmpty() || name.startsWith(".")) continue
                    
                    val icon: Drawable = appInfo.loadIcon(pm)
                    val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    val isUpdatedSystemApp = (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                    
                    apps.add(AppInfo(name, packageName, icon, isSystemApp && !isUpdatedSystemApp))
                } catch (e: Exception) {
                    Log.e("LUNCHER", "Erreur", e)
                }
            }
            
            val userApps = apps.filter { !it.isSystemApp }.sortedBy { it.name.lowercase() }
            val systemApps = apps.filter { it.isSystemApp }.sortedBy { it.name.lowercase() }
            val finalList = userApps + systemApps
            
            withContext(Dispatchers.Main) {
                allApps = finalList
                adapter.setList(finalList)
                statusText.text = "✅ ${finalList.size} applications\n📱 ${userApps.size} installées par vous"
                handler.postDelayed({ statusText.visibility = View.GONE }, 4000)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(timeRunnable)
    }
}
