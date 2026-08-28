package com.luncher

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.luncher.data.AppInfo
import com.luncher.databinding.ActivityLauncherBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class LauncherActivity : AppCompatActivity() {
    private lateinit var b: ActivityLauncherBinding
    private lateinit var adapter: AppAdapter
    private lateinit var messagesAdapter: MessagesAdapter
    private lateinit var prefs: SharedPreferences
    private var isDrawerOpen = false
    private var allApps = listOf<AppInfo>()
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var timeRunnable: Runnable
    
    private var floatingWindow: View? = null
    private var isMessageBoxOpen = false
    private lateinit var windowManager: WindowManager

    private val PREFS_NAME = "LuncherPrefs"
    private val KEY_WALLPAPER_URI = "wallpaper_uri"
    private val KEY_TEXT_COLOR = "text_color"
    private val KEY_HOUR_X = "hour_x"
    private val KEY_HOUR_Y = "hour_y"

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
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (!allGranted) {
            Toast.makeText(this, "⚠️ Certaines permissions sont manquantes", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(b.root)

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        setupRecycler()
        setupSearch()
        b.toggleBtn.setOnClickListener { toggleDrawer() }
        b.messagesBtn.setOnClickListener { toggleMessageBox() }
        
        b.drawerLayout.visibility = View.GONE
        b.toggleBtn.rotation = 0f

        setupDateTime()
        setupDraggableTime()
        setupLongPressActions()
        loadSavedWallpaper()
        loadTextColor()
        loadTimePosition()
        loadAllApps()
        setupMessagesObserver()
        checkAllPermissions()
    }

    private fun checkAllPermissions() {
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
        }
        
        if (permissionsNeeded.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsNeeded.toTypedArray())
        }
        
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(intent)
        }
        
        if (!isNotificationListenerEnabled()) {
            val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            startActivity(intent)
            Toast.makeText(this, "✅ Active l'autorisation de notifications pour Luncher", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun isNotificationListenerEnabled(): Boolean {
        val enabledListeners = Settings.Secure.getString(contentResolver, 
            Settings.Secure.ENABLED_NOTIFICATION_LISTENERS)
        return enabledListeners?.contains(packageName) == true
    }

    private fun setupDraggableTime() {
        var dX = 0f
        var dY = 0f
        
        b.timeContainer.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    dX = view.x - event.rawX
                    dY = view.y - event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    view.x = event.rawX + dX
                    view.y = event.rawY + dY
                    true
                }
                MotionEvent.ACTION_UP -> {
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
            b.timeContainer.x = x
            b.timeContainer.y = y
        }
    }

    private fun setupLongPressActions() {
        b.root.setOnLongClickListener {
            changerFondEcran()
            true
        }

        b.heureTexte.setOnLongClickListener {
            showColorPickerDialog()
            true
        }

        b.dateTexte.setOnLongClickListener {
            showColorPickerDialog()
            true
        }
    }

    private fun showColorPickerDialog() {
        val currentColor = prefs.getInt(KEY_TEXT_COLOR, Color.WHITE)
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
        val savedColor = prefs.getInt(KEY_TEXT_COLOR, Color.WHITE)
        setTextColor(savedColor)
    }

    private fun changerFondEcran() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openGallery()
        } else {
            requestPermissionLauncher.launch(arrayOf(permission))
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

    private fun setWallpaperFromUri(uri: Uri) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val drawable = BitmapDrawable(resources, bitmap)
            drawable.setFilterBitmap(true)
            drawable.gravity = android.view.Gravity.FILL
            b.root.background = drawable
        } catch (e: Exception) {
            Toast.makeText(this, "⚠️ Image trop grande ou illisible", Toast.LENGTH_SHORT).show()
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

    private fun setupMessagesObserver() {
        messagesAdapter = MessagesAdapter(
            onDismiss = { msg -> dismissMessage(msg) },
            onReply = { msg -> replyToMessage(msg) },
            onOpen = { msg -> openMessageApp(msg) }
        )
        
        CoroutineScope(Dispatchers.Main).launch {
            NotificationListener.messagesFlow.collect { messages ->
                messagesAdapter.setList(messages)
            }
        }
    }

    private fun toggleMessageBox() {
        if (isMessageBoxOpen) closeMessageBox() else openMessageBox()
    }

    private fun openMessageBox() {
        if (floatingWindow != null) return
        isMessageBoxOpen = true
        b.messagesBtn.rotation = 45f
        
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        floatingWindow = inflater.inflate(R.layout.floating_message_box, null)
        
        val layoutParams = WindowManager.LayoutParams(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        layoutParams.gravity = Gravity.TOP
        
        floatingWindow?.findViewById<ImageView>(R.id.close_btn)?.setOnClickListener { closeMessageBox() }
        
        val recycler = floatingWindow?.findViewById<RecyclerView>(R.id.messages_list)
        recycler?.adapter = messagesAdapter
        recycler?.layoutManager = LinearLayoutManager(this)
        
        windowManager.addView(floatingWindow, layoutParams)
    }

    private fun closeMessageBox() {
        isMessageBoxOpen = false
        b.messagesBtn.rotation = 0f
        floatingWindow?.let { windowManager.removeView(it) }
        floatingWindow = null
    }

    private fun dismissMessage(msg: Message) {
        val current = NotificationListener.messagesFlow.value.toMutableList()
        current.removeAll { it.id == msg.id }
        NotificationListener.messagesFlow.value = current
        if (current.isEmpty()) closeMessageBox()
    }

    private fun replyToMessage(msg: Message) {
        when (msg.type) {
            "SMS" -> {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${msg.sender}"))
                startActivity(intent)
            }
            "WHATSAPP" -> {
                try {
                    val num = msg.sender.replace(Regex("[^0-9]"), "")
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$num"))
                    startActivity(intent)
                } catch (e: Exception) {
                    val intent = packageManager.getLaunchIntentForPackage("com.whatsapp")
                    intent?.let { startActivity(it) }
                }
            }
            "GMAIL" -> {
                val intent = packageManager.getLaunchIntentForPackage("com.google.android.gm")
                intent?.let { startActivity(it) }
            }
        }
    }

    private fun openMessageApp(msg: Message) {
        val intent = packageManager.getLaunchIntentForPackage(msg.packageName)
        intent?.let { startActivity(it) }
        closeMessageBox()
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
        floatingWindow?.let { windowManager.removeView(it) }
    }
}
