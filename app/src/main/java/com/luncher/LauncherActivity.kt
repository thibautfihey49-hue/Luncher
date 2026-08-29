
package com.luncher
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LauncherActivity : AppCompatActivity() {
    private lateinit var wallpaperView: ImageView
    private lateinit var drawerWallpaperView: ImageView
    private lateinit var timeView: TextView
    private lateinit var dateView: TextView
    private lateinit var timeContainer: View
    private lateinit var root: ConstraintLayout
    private lateinit var drawerContainer: View
    private lateinit var appRecycler: RecyclerView
    private lateinit var searchBar: EditText
    private lateinit var drawerHandle: View
    private lateinit var drawerSearchHandle: View
    private lateinit var permissionOverlay: View
    private lateinit var prefs: SharedPreferences
    private lateinit var adapter: AppAdapter
    private val timeHandler = Handler(Looper.getMainLooper())
    private val timeRunnable = object : Runnable {
        override fun run() { updateTime(); timeHandler.postDelayed(this, 1000L) }
    }
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            try { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
            prefs.edit().putString("wallpaper_uri", uri.toString()).apply()
            loadWallpaper()
        }
    }
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { _ -> checkAndShowPermissions() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)
        prefs = getSharedPreferences("luncher_prefs", Context.MODE_PRIVATE)
        root = findViewById(R.id.root)
        wallpaperView = findViewById(R.id.wallpaperView)
        drawerWallpaperView = findViewById(R.id.drawerWallpaperView)
        timeView = findViewById(R.id.timeView)
        dateView = findViewById(R.id.dateView)
        timeContainer = findViewById(R.id.timeContainer)
        drawerContainer = findViewById(R.id.drawerContainer)
        appRecycler = findViewById(R.id.appRecycler)
        searchBar = findViewById(R.id.searchBar)
        drawerHandle = findViewById(R.id.drawerHandle)
        drawerSearchHandle = findViewById(R.id.drawerSearchHandle)
        permissionOverlay = findViewById(R.id.permissionOverlay)
        restoreTimePosition(); restoreTimeColor(); loadWallpaper(); setupTimeDragging(); setupColorPicker(); setupWallpaperLongPress(); setupDrawer(); setupRecycler(); setupPermissionOverlay(); timeHandler.post(timeRunnable); checkAndShowPermissions(); loadApps()
    }
    override fun onResume() { super.onResume(); loadApps(); loadWallpaper(); checkAndShowPermissions() }
    override fun onDestroy() { super.onDestroy(); timeHandler.removeCallbacks(timeRunnable) }
    private fun updateTime() {
        val cal = Calendar.getInstance()
        timeView.text = SimpleDateFormat("HH:mm", Locale.FRENCH).format(cal.time)
        dateView.text = SimpleDateFormat("EEEE d MMMM", Locale.FRENCH).format(cal.time).replaceFirstChar { it.uppercase() }
    }
    private fun loadWallpaper() {
        val uriStr = prefs.getString("wallpaper_uri", null)
        var loaded = false
        if (uriStr != null) {
            try {
                val uri = Uri.parse(uriStr)
                wallpaperView.setImageURI(uri)
                wallpaperView.setBackgroundColor(Color.TRANSPARENT)
                drawerWallpaperView.setImageURI(uri)
                drawerWallpaperView.setBackgroundColor(Color.TRANSPARENT)
                loaded = true
            } catch (_: Exception) { loaded = false }
        }
        if (loaded == false) {
            wallpaperView.setImageDrawable(null); wallpaperView.setBackgroundColor(Color.parseColor("#0A0A0A"))
            drawerWallpaperView.setImageDrawable(null); drawerWallpaperView.setBackgroundColor(Color.parseColor("#0A0A0A"))
        }
    }
    private fun setupWallpaperLongPress() {
        val listener = object : View.OnLongClickListener {
            override fun onLongClick(v: View): Boolean {
                if (drawerContainer.visibility != View.VISIBLE) pickImageLauncher.launch("image/*")
                return true
            }
        }
        root.setOnLongClickListener(listener)
        wallpaperView.setOnLongClickListener(listener)
    }
    private fun setupTimeDragging() {
        var dX = 0f; var dY = 0f
        timeContainer.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> { dX = v.x - event.rawX; dY = v.y - event.rawY; v.parent.requestDisallowInterceptTouchEvent(true) }
                    MotionEvent.ACTION_MOVE -> { if (v.getTag(R.id.drawerHandle) == true) { v.x = event.rawX + dX; v.y = event.rawY + dY } }
                    MotionEvent.ACTION_UP -> {
                        v.parent.requestDisallowInterceptTouchEvent(false)
                        if (v.getTag(R.id.drawerHandle) == true) {
                            val rx = (v.x + v.width / 2f) / root.width.toFloat()
                            val ry = (v.y + v.height / 2f) / root.height.toFloat()
                            prefs.edit().putFloat("time_x_ratio", rx).putFloat("time_y_ratio", ry).apply()
                        }
                        v.setTag(R.id.drawerHandle, false)
                    }
                }
                return false
            }
        })
        timeContainer.setOnLongClickListener(object : View.OnLongClickListener {
            override fun onLongClick(view: View): Boolean {
                view.setTag(R.id.drawerHandle, true)
                view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
                Toast.makeText(this@LauncherActivity, "Deplace l'heure", Toast.LENGTH_SHORT).show()
                return true
            }
        })
    }
    private fun restoreTimePosition() { root.post { val rx = prefs.getFloat("time_x_ratio", 0.5f); val ry = prefs.getFloat("time_y_ratio", 0.22f); if (root.width > 0 && root.height > 0) { timeContainer.x = rx * root.width - timeContainer.width / 2f; timeContainer.y = ry * root.height - timeContainer.height / 2f } } }
    private val colors = intArrayOf(Color.WHITE, Color.parseColor("#FFEB3B"), Color.parseColor("#FF9800"), Color.parseColor("#F44336"), Color.parseColor("#E91E63"), Color.parseColor("#9C27B0"), Color.parseColor("#2196F3"), Color.parseColor("#00BCD4"), Color.parseColor("#4CAF50"), Color.BLACK, Color.parseColor("#9E9E9E"))
    private val colorNames = arrayOf("Blanc","Jaune","Orange","Rouge","Rose","Violet","Bleu","Turquoise","Vert","Noir","Gris")
    private fun setupColorPicker() { val l = View.OnClickListener { showColorChooser() }; timeView.setOnClickListener(l); dateView.setOnClickListener(l) }
    private fun showColorChooser() { AlertDialog.Builder(this).setTitle("Couleur du texte").setItems(colorNames) { _, which -> val c = colors[which]; timeView.setTextColor(c); dateView.setTextColor(c); prefs.edit().putInt("time_color", c).apply() }.show() }
    private fun restoreTimeColor() { val c = prefs.getInt("time_color", Color.WHITE); timeView.setTextColor(c); dateView.setTextColor(c) }

    private fun setupDrawer() {
        // 1. Handle agrandi cliquable
        drawerHandle.setOnClickListener { openDrawer() }
        drawerHandle.setOnTouchListener(object : View.OnTouchListener {
            var startY = 0f
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (event.action == MotionEvent.ACTION_DOWN) startY = event.rawY
                if (event.action == MotionEvent.ACTION_UP) {
                    if (startY - event.rawY > 50) openDrawer() else openDrawer()
                }
                return false
            }
        })
        // 2. Swipe UP depuis n'importe où dans le tiers inférieur = ouverture facile
        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                if (e1 == null || e2 == null) return false
                val diffY = e1.y - e2.y
                val diffX = e1.x - e2.x
                // swipe vers le haut, plus vertical que horizontal, vitesse suffisante
                if (diffY > 100 && Math.abs(diffY) > Math.abs(diffX) && Math.abs(velocityY) > 300) {
                    // seulement si on part du bas 60% de l'écran ou si tiroir fermé
                    if (drawerContainer.visibility != View.VISIBLE && e1.y > root.height * 0.3f) {
                        openDrawer()
                        return true
                    }
                }
                if (drawerContainer.visibility == View.VISIBLE && diffY < -100 && Math.abs(velocityY) > 300) {
                    closeDrawer()
                    return true
                }
                return false
            }
        })
        root.setOnTouchListener(object : View.OnTouchListener {
            var startY = 0f
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                gestureDetector.onTouchEvent(event)
                if (event.action == MotionEvent.ACTION_DOWN) startY = event.rawY
                if (event.action == MotionEvent.ACTION_UP) {
                    val diff = startY - event.rawY
                    // OUverture facile : swipe de 80px depuis le bas 70% de l'écran
                    if (drawerContainer.visibility != View.VISIBLE && diff > 80 && startY > root.height * 0.3f) {
                        openDrawer()
                        return true
                    }
                }
                return false
            }
        })
        drawerSearchHandle.setOnTouchListener(object : View.OnTouchListener {
            var sy = 0f
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (event.action == MotionEvent.ACTION_DOWN) sy = event.rawY
                if (event.action == MotionEvent.ACTION_UP) { if (event.rawY - sy > 80) closeDrawer() }
                return true
            }
        })
        searchBar.setOnTouchListener(object : View.OnTouchListener {
            var sy = 0f
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                gestureDetector.onTouchEvent(event)
                if (event.action == MotionEvent.ACTION_DOWN) sy = event.rawY
                if (event.action == MotionEvent.ACTION_UP && event.rawY - sy > 80) { closeDrawer(); return true }
                return false
            }
        })
    }

    private fun openDrawer() {
        drawerContainer.visibility = View.VISIBLE
        drawerContainer.alpha = 0f
        drawerContainer.translationY = 200f
        drawerContainer.animate().alpha(1f).translationY(0f).setDuration(250).start()
        searchBar.requestFocus()
        loadApps()
    }
    private fun closeDrawer() {
        drawerContainer.animate().alpha(0f).translationY(200f).setDuration(200).withEndAction { drawerContainer.visibility = View.GONE; searchBar.setText("") }.start()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        currentFocus?.let { imm.hideSoftInputFromWindow(it.windowToken, 0) }
    }
    private fun setupRecycler() {
        appRecycler.layoutManager = GridLayoutManager(this, 4)
        adapter = AppAdapter(this, emptyList()) { app ->
            try { val intent = Intent().apply { component = android.content.ComponentName(app.packageName, app.className); addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }; startActivity(intent); closeDrawer() } catch (_: Exception) { try { val l = packageManager.getLaunchIntentForPackage(app.packageName); if (l != null) { startActivity(l); closeDrawer() } } catch (_: Exception) {} }
        }
        appRecycler.adapter = adapter
        searchBar.addTextChangedListener(object : android.text.TextWatcher { override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}; override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { adapter.filter(s?.toString() ?: "") }; override fun afterTextChanged(s: android.text.Editable?) {} })
    }
    private fun loadApps() { try { val apps = AppAdapter.loadAllApps(packageManager); if (this::adapter.isInitialized) { adapter.updateList(apps); val q = searchBar.text?.toString() ?: ""; if (q.isNotBlank()) adapter.filter(q) } } catch (_: Exception) {} }
    private fun setupPermissionOverlay() { val btnGrant = permissionOverlay.findViewById<View>(R.id.btnGrantAll); val btnContinue = permissionOverlay.findViewById<View>(R.id.btnContinue); btnGrant.setOnClickListener { requestAllPermissions() }; btnContinue.setOnClickListener { permissionOverlay.visibility = View.GONE; prefs.edit().putBoolean("permissions_asked", true).apply() } }
    private fun checkAndShowPermissions(): Boolean { val hasAsked = prefs.getBoolean("permissions_asked", false); val overlayPerm = Settings.canDrawOverlays(this); val notifPerm = isNotificationListenerEnabled(); val storageOk = if (android.os.Build.VERSION.SDK_INT >= 33) ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED else ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED; val smsOk = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED; val allOk = overlayPerm && notifPerm && storageOk && smsOk; if (hasAsked == false && allOk == false) permissionOverlay.visibility = View.VISIBLE else if (allOk) permissionOverlay.visibility = View.GONE; return allOk }
    private fun requestAllPermissions() { if (Settings.canDrawOverlays(this) == false) { startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))) }; if (isNotificationListenerEnabled() == false) { startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }; val perms = mutableListOf<String>(); if (android.os.Build.VERSION.SDK_INT >= 33) { perms.add(Manifest.permission.READ_MEDIA_IMAGES); perms.add(Manifest.permission.POST_NOTIFICATIONS) } else { perms.add(Manifest.permission.READ_EXTERNAL_STORAGE) }; perms.add(Manifest.permission.READ_SMS); perms.add(Manifest.permission.RECEIVE_SMS); permissionLauncher.launch(perms.toTypedArray()) }
    private fun isNotificationListenerEnabled(): Boolean { val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners") ?: return false; return flat.contains(packageName) }
    override fun onBackPressed() { if (drawerContainer.visibility == View.VISIBLE) closeDrawer() }
}

