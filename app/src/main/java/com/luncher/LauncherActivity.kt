package com.luncher

import android.Manifest
import android.app.AlertDialog
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
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
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
    private lateinit var prefs: SharedPreferences
    private var isDrawerOpen = false
    private var allApps = listOf<AppInfo>()
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var timeRunnable: Runnable

    private val PREFS_NAME = "LuncherPrefs"
    private val KEY_WALLPAPER_URI = "wallpaper_uri"
    private val KEY_TEXT_COLOR = "text_color"

    // 🎨 Couleurs disponibles pour le texte
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
                Toast.makeText(this, "✅ Fond d'écran changé !", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openGallery()
        } else {
            Toast.makeText(this, "❌ Permission nécessaire", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(b.root)

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        setupRecycler()
        setupSearch()
        b.toggleBtn.setOnClickListener { toggleDrawer() }
        
        b.drawerLayout.visibility = View.GONE
        b.toggleBtn.rotation = 0f

        setupDateTime()
        setupLongPressActions()
        loadSavedWallpaper()
        loadTextColor()
        loadAllApps()
    }

    // ✅ APPUIS LONGS : fond sur l'accueil, couleur sur l'heure
    private fun setupLongPressActions() {
        // Appui long sur l'espace vide → changer fond d'écran
        b.root.setOnLongClickListener {
            if (b.heureTexte.isPressed || b.dateTexte.isPressed) {
                // L'appui est sur l'heure ou la date → ne rien faire ici
            } else {
                changerFondEcran()
            }
            true
        }

        // Appui long sur l'heure → changer couleur du texte
        b.heureTexte.setOnLongClickListener {
            showColorPickerDialog()
            true
        }

        // Appui long sur la date → changer couleur du texte
        b.dateTexte.setOnLongClickListener {
            showColorPickerDialog()
            true
        }
    }

    // 🎨 CHOISIR LA COULEUR DU TEXTE
    private fun showColorPickerDialog() {
        val currentColor = prefs.getInt(KEY_TEXT_COLOR, Color.BLACK)
        val selectedIndex = colorValues.indexOf(currentColor).coerceAtLeast(0)

        AlertDialog.Builder(this)
            .setTitle("🎨 Couleur du texte")
            .setSingleChoiceItems(colorNames.toTypedArray(), selectedIndex) { dialog, which ->
                val newColor = colorValues[which]
                setTextColor(newColor)
                prefs.edit { putInt(KEY_TEXT_COLOR, newColor) }
                dialog.dismiss()
                Toast.makeText(this, "✅ Couleur changée !", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun setTextColor(color: Int) {
        b.heureTexte.setTextColor(color)
        b.dateTexte.setTextColor(color)
    }

    private fun loadTextColor() {
        val savedColor = prefs.getInt(KEY_TEXT_COLOR, Color.BLACK)
        setTextColor(savedColor)
    }

    private fun changerFondEcran() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                openGallery()
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        pickImageLauncher.launch(Intent.createChooser(intent, "Choisir une image"))
    }

    private fun saveWallpaperUri(uri: Uri) {
        prefs.edit { putString(KEY_WALLPAPER_URI, uri.toString()) }
    }

    private fun loadSavedWallpaper() {
        val uriString = prefs.getString(KEY_WALLPAPER_URI, null)
        uriString?.let { uriStr ->
            try {
                val uri = Uri.parse(uriStr)
                setWallpaperFromUri(uri)
            } catch (e: Exception) {
                prefs.edit { remove(KEY_WALLPAPER_URI) }
            }
        }
    }

    // ✅ FOND PLEIN ÉCRAN, SANS DÉFORMATION
    private fun setWallpaperFromUri(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val drawable = BitmapDrawable(resources, bitmap)
            drawable.setTileModeXY(android.graphics.drawable.Shader.TileMode.CLAMP, android.graphics.drawable.Shader.TileMode.CLAMP)
            drawable.gravity = android.view.Gravity.FILL
            b.root.background = drawable
        } catch (e: Exception) {
            Toast.makeText(this, "⚠️ Impossible de charger l'image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupDateTime() {
        val timeFormat = SimpleDateFormat("HH:mm", Locale.FRANCE)
        val dateFormat = SimpleDateFormat("EEEE d MMMM", Locale.FRANCE)

        fun update() {
            val maintenant = Calendar.getInstance()
            b.heureTexte.text = timeFormat.format(maintenant.time)
            b.dateTexte.text = dateFormat.format(maintenant.time).replaceFirstChar { it.uppercase() }
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
