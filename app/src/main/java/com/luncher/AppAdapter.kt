package com.luncher
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
data class AppInfoModel(val label: String, val packageName: String, val className: String, val icon: android.graphics.drawable.Drawable)
class AppAdapter(private val context: Context, private var apps: List<AppInfoModel>, private val onAppClick: (AppInfoModel) -> Unit) : RecyclerView.Adapter<AppAdapter.VH>() {
    private var filtered: List<AppInfoModel> = apps
    class VH(v: View) : RecyclerView.ViewHolder(v) { val icon: ImageView = v.findViewById(R.id.appIcon); val name: TextView = v.findViewById(R.id.appName) }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH { return VH(LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)) }
    override fun onBindViewHolder(holder: VH, position: Int) { val app = filtered[position]; holder.icon.setImageDrawable(app.icon); holder.name.text = app.label; holder.itemView.setOnClickListener { onAppClick(app) } }
    override fun getItemCount() = filtered.size
    fun updateList(newList: List<AppInfoModel>) { apps = newList; filtered = newList; notifyDataSetChanged() }
    fun filter(query: String) { filtered = if (query.isBlank()) apps else apps.filter { it.label.contains(query, ignoreCase = true) || it.packageName.contains(query, ignoreCase = true) }; notifyDataSetChanged() }
    companion object { fun loadAllApps(pm: PackageManager): List<AppInfoModel> { val intent = Intent(Intent.ACTION_MAIN, null).apply { addCategory(Intent.CATEGORY_LAUNCHER) }; val list = pm.queryIntentActivities(intent, 0); return list.mapNotNull { try { val label = it.loadLabel(pm).toString(); val pkg = it.activityInfo.packageName; val cls = it.activityInfo.name; val icon = it.loadIcon(pm); AppInfoModel(label, pkg, cls, icon) } catch (e: Exception) { null } }.sortedBy { it.label.lowercase() } } }
}
