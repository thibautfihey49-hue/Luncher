package com.luncher; import android.content.*; import android.content.pm.ResolveInfo; import android.net.Uri; import android.os.Build; import android.os.Bundle; import android.provider.Settings; import android.text.Editable; import android.text.TextWatcher; import android.view.View; import androidx.appcompat.app.AppCompatActivity; import androidx.recyclerview.widget.GridLayoutManager; import com.luncher.data.AppInfo; import com.luncher.databinding.ActivityLauncherBinding; import com.luncher.ui.FloatingWindowService; import kotlinx.coroutines.*
class LauncherActivity : AppCompatActivity() {
    private lateinit var b:ActivityLauncherBinding; private lateinit var a:AppAdapter; private val scope=CoroutineScope(Dispatchers.IO); private val REQ=1001
    override fun onCreate(s:Bundle?){
        super.onCreate(s); b=ActivityLauncherBinding.inflate(layoutInflater); setContentView(b.root)
        checkPerm(); setupRV(); setupSearch(); loadApps()
    }
    private fun checkPerm(){if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)){startActivityForResult(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:$packageName")),REQ)}else{startService(Intent(this,FloatingWindowService::class.java))}}
    private fun setupRV(){a=AppAdapter{packageManager.getLaunchIntentForPackage(it.packageName)?.let{startActivity(it)}};b.appsRecycler.apply{adapter=a;layoutManager=GridLayoutManager(this@LauncherActivity,5);setHasFixedSize(true);itemAnimator=null}}
    private fun setupSearch(){b.searchInput.addTextChangedListener(object:TextWatcher{override fun beforeTextChanged(c:CharSequence?,s:Int,cnt:Int,a:Int)=Unit;override fun onTextChanged(c:CharSequence?,s:Int,bef:Int,aft:Int)=a.filter(c.toString());override fun afterTextChanged(e:Editable?)=Unit})}
    private fun loadApps(){b.progress.visibility=View.VISIBLE;scope.launch{val l=queryApps();withContext(Dispatchers.Main){a.setApps(l);b.progress.visibility=View.GONE}}}
    private fun queryApps():List<AppInfo>{val i=Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);val l=packageManager.queryIntentActivities(i,0);return l.sortedBy{it.loadLabel(packageManager).toString()}.map{AppInfo(it.loadLabel(packageManager).toString(),it.activityInfo.packageName,it.loadIcon(packageManager))}}
    override fun onActivityResult(r:Int,res:Int,d:Intent?){super.onActivityResult(r,res,d);if(r==REQ && Build.VERSION.SDK_INT>=Build.VERSION_CODES.M && Settings.canDrawOverlays(this))startService(Intent(this,FloatingWindowService::class.java))}
}
