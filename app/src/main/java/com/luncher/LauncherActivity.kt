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
import kotlin.math.abs

class LauncherActivity : AppCompatActivity() {
    private lateinit var wallpaperView: ImageView
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
        override fun run() {
            updateTime()
            timeHandler.postDelayed(this, 1000L)
        }
    }
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            try { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
            prefs.edit().putString("wallpaper_uri", uri.toString()).apply()
            loadWallpaper()
        }
    }
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { _ ->
        checkAndShowPermissions()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)
        prefs = getSharedPreferences("luncher_prefs", Context.MODE_PRIVATE)
        root = findViewById(R.id.root)
        wallpaperView = findViewById(R.id.wallpaperView)
        timeView = findViewById(R.id.timeView)
        dateView = findViewById(R.id.dateView)
        timeContainer = findViewById(R.id.timeContainer)
        drawerContainer = findViewById(R.id.drawerContainer)
        appRecycler = findViewById(R.id.appRecycler)
        searchBar = findViewById(R.id.searchBar)
        drawerHandle = findViewById(R.id.drawerHandle)
        drawerSearchHandle = findViewById(R.id.drawerSearchHandle)
        permissionOverlay = findViewById(R.id.permissionOverlay)
        restoreTimePosition()
        restoreTimeColor()
        loadWallpaper()
        setupTimeDragging()
        setupColorPicker()
        setupWallpaperLongPress()
        setupDrawer()
        setupRecycler()
        setupPermissionOverlay()
        timeHandler.post(timeRunnable)
        checkAndShowPermissions()
        loadApps()
    }

    override fun onResume() { super.onResume(); loadApps(); loadWallpaper(); checkAndShowPermissions() }
    override fun onDestroy() { super.onDestroy(); timeHandler.removeCallbacks(timeRunnable) }

    private fun updateTime() {
        val cal = Calendar.getInstance()
        val timeFmt = SimpleDateFormat("HH:mm", Locale.FRENCH)
        val dateFmt = SimpleDateFormat("EEEE d MMMM", Locale.FRENCH)
        timeView.text = timeFmt.format(cal.time)
        dateView.text = dateFmt.format(cal.time).replaceFirstChar { it.uppercase() }
    }

    private fun loadWallpaper() {
        val uriStr = prefs.getString("wallpaper_uri", null)
        if (uriStr != null) {
            try {
                wallpaperView.setImageURI(Uri.parse(uriStr))
                wallpaperView.setBackgroundColor(Color.TRANSPARENT)
                return
            } catch (_: Exception) {}
        }
        wallpaperView.setImageDrawable(null)
        wallpaperView.setBackgroundColor(Color.parseColor("#0A0A0A"))
    }

    private fun setupWallpaperLongPress() {
        val longListener = View.OnLongClickListener {
            if (drawerContainer.visibility != View.VISIBLE) pickImageLauncher.launch("image/*")
            true
        }
        root.setOnLongClickListener(longListener)
        wallpaperView.setOnLongClickListener(longListener)
    }

    private fun setupTimeDragging() {
        var dX = 0f
        var dY = 0f
        var startX = 0f
        var startY = 0f
        timeContainer.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = v.x - event.rawX
                    dY = v.y - event.rawY
                    startX = event.rawX
                    startY = event.rawY
                    v.parent.requestDisallowInterceptTouchEvent(true)
                }
                MotionEvent.ACTION_MOVE -> {
                    if (v.getTag(R.id.drawerHandle) == true) {
                        v.x = event.rawX + dX
                        v.y = event.rawY + dY
                    }
                }
                MotionEvent.ACTION_UP -> {
                    v.parent.requestDisallowInterceptTouchEvent(false)
                    if (v.getTag(R.id.drawerHandle) == true) {
                        val ratioX = (v.x + v.width / 2f) / root.width.toFloat()
                        val ratioY = (v.y + v.height / 2f) / root.height.toFloat()
                        prefs.edit().putFloat("time_x_ratio", ratioX).putFloat("time_y_ratio", ratioY).apply()
                    }
                    v.setTag(R.id.drawerHandle, false)
                }
            }
            false
        }
        timeContainer.setOnLongClickListener { view ->
            view.setTag(R.id.drawerHandle, true)
            view.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS)
            Toast.makeText(this, "Deplace l'heure", Toast.LENGTH_SHORT).show()
            true
        }
    }

    private fun restoreTimePosition() {
        root.post {
            val rx = prefs.getFloat("time_x_ratio", 0.5f)
            val ry = prefs.getFloat("time_y_ratio", 0.22f)
            val w = root.width
            val h = root.height
            if (w > 0 && h > 0) {
                timeContainer.x = rx * w - timeContainer.width / 2f
                timeContainer.y = ry * h - timeContainer.height / 2f
            }
        }
    }

    private val colors = intArrayOf(Color.WHITE, Color.parseColor("#FFEB3B"), Color.parseColor("#FF9800"), Color.parseColor("#F44336"), Color.parseColor("#E91E63"), Color.parseColor("#9C27B0"), Color.parseColor("#2196F3"), Color.parseColor("#00BCD4"), Color.parseColor("#4CAF50"), Color.BLACK, Color.parseColor("#9E9E9E"))
    private val colorNames = arrayOf("Blanc","Jaune","Orange","Rouge","Rose","Violet","Bleu","Turquoise","Vert","Noir","Gris")

    private fun setupColorPicker() {
        val listener = View.OnClickListener { showColorChooser() }
        timeView.setOnClickListener(listener)
        dateView.setOnClickListener(listener)
    }

    private fun showColorChooser() {
        AlertDialog.Builder(this).setTitle("Couleur du texte").setItems(colorNames) { _, which ->
            val c = colors[which]
            timeView.setTextColor(c)
            dateView.setTextColor(c)
            prefs.edit().putInt("time_color", c).apply()
        }.show()
    }

    private fun restoreTimeColor() {
        val c = prefs.getInt("time_color", Color.WHITE)
        timeView.setTextColor(c)
        dateView.setTextColor(c)
    }

    private fun setupDrawer() {
        var startY = 0f
        drawerHandle.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) startY = event.rawY
            if (event.action == MotionEvent.ACTION_UP) {
                if (startY - event.rawY > 100) openDrawer()
            }
            true
        }
        root.setOnTouchListener { _, event ->
            if (drawerContainer.visibility == View.VISIBLE) {
                false
            } else {
                if (event.action == MotionEvent.ACTION_DOWN) startY = event.rawY
                if (event.action == MotionEvent.ACTION_UP) {
                    val diff = startY - event.rawY
                    val fromBottom = root.height - event.rawY < 200
                    if (diff > 120 && fromBottom) openDrawer()
                }
                false
            }
        }
        var searchStartY = 0f
        drawerSearchHandle.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) searchStartY = event.rawY
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawY - searchStartY > 100) closeDrawer()
            }
            true
        }
        val gd = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(e1: MotionEvent?, e2: MotionEvent, vx: Float, vy: Float): Boolean {
                if (vy > 800) {
                    closeDrawer()
                    return true
                }
                return false
            }
        })
        searchBar.setOnTouchListener { _, event ->
            gd.onTouchEvent(event)
            if (event.action == MotionEvent.ACTION_DOWN) searchStartY = event.rawY
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawY - searchStartY > 120) {
                    closeDrawer()
                    return true
                }
            }
            false
        }
    }

    private fun openDrawer() {
        drawerContainer.visibility = View.VISIBLE
        drawerContainer.alpha = 0f
        drawerContainer.animate().alpha(1f).setDuration(200).start()
        searchBar.requestFocus()
        loadApps()
    }

    private fun closeDrawer() {
        drawerContainer.animate().alpha(0f).setDuration(180).withEndAction {
            drawerContainer.visibility = View.GONE
            searchBar.setText("")
        }.start()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        currentFocus?.let { imm.hideSoftInputFromWindow(it.windowToken, 0) }
    }

    private fun setupRecycler() {
        appRecycler.layoutManager = GridLayoutManager(this, 4)
        adapter = AppAdapter(this, emptyList()) { app ->
            try {
                val intent = Intent().apply {
                    component = android.content.ComponentName(app.packageName, app.className)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
                closeDrawer()
            } catch (_: Exception) {
                try {
                    val launch = packageManager.getLaunchIntentForPackage(app.packageName)
                    if (launch != null) {
                        startActivity(launch)
                        closeDrawer()
                    }
                } catch (_: Exception) {}
            }
        }
        appRecycler.adapter = adapter
        searchBar.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { adapter.filter(s?.toString() ?: "") }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    private fun loadApps() {
        try {
            val apps = AppAdapter.loadAllApps(packageManager)
            if (this::adapter.isInitialized) {
                adapter.updateList(apps)
                val q = searchBar.text?.toString() ?: ""
                if (q.isNotBlank()) adapter.filter(q)
            }
        } catch (_: Exception) {}
    }

    private fun setupPermissionOverlay() {
        val btnGrant = permissionOverlay.findViewById<View>(R.id.btnGrantAll)
        val btnContinue = permissionOverlay.findViewById<View>(R.id.btnContinue)
        btnGrant.setOnClickListener { requestAllPermissions() }
        btnContinue.setOnClickListener {
            permissionOverlay.visibility = View.GONE
            prefs.edit().putBoolean("permissions_asked", true).apply()
        }
    }

    private fun checkAndShowPermissions(): Boolean {
        val hasAsked = prefs.getBoolean("permissions_asked", false)
        val overlayPerm = Settings.canDrawOverlays(this)
        val notifPerm = isNotificationListenerEnabled()
        val storageOk = if (android.os.Build.VERSION.SDK_INT >= 33) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
        val smsOk = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
        val allOk = overlayPerm && notifPerm && storageOk && smsOk
        if (!allOk && !hasAsked) permissionOverlay.visibility = View.VISIBLE else if (allOk) permissionOverlay.visibility = View.GONE
        return allOk
    }

    private fun requestAllPermissions() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(intent)
        }
        if (!isNotificationListenerEnabled()) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            startActivity(intent)
        }
        val perms = mutableListOf<String>()
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            perms.add(Manifest.permission.READ_MEDIA_IMAGES)
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            perms.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        perms.add(Manifest.permission.READ_SMS)
        perms.add(Manifest.permission.RECEIVE_SMS)
        permissionLauncher.launch(perms.toTypedArray())
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners") ?: return false
        return flat.contains(packageName)
    }

    override fun onBackPressed() {
        if (drawerContainer.visibility == View.VISIBLE) closeDrawer()
    }
}

