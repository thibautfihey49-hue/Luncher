package com.luncher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: android.graphics.drawable.Drawable,
    val isSystemApp: Boolean = false
)

class AppAdapter(private val onAppClick: (AppInfo) -> Unit) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {

    private var apps: List<AppInfo> = emptyList()
    private var filteredApps: List<AppInfo> = emptyList()

    inner class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.app_icon)
        val name: TextView = view.findViewById(R.id.app_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = filteredApps[position]
        holder.name.text = app.name
        holder.icon.setImageDrawable(app.icon)
        holder.itemView.setOnClickListener { onAppClick(app) }
    }

    override fun getItemCount() = filteredApps.size

    fun setList(newApps: List<AppInfo>) {
        apps = newApps
        filteredApps = newApps
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        filteredApps = if (query.isEmpty()) {
            apps
        } else {
            apps.filter { it.name.contains(query, ignoreCase = true) }
        }
        notifyDataSetChanged()
    }
}
