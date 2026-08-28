package com.luncher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.luncher.data.AppInfo

class AppAdapter(
    private val onAppClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {

    private var apps: List<AppInfo> = emptyList()
    private var allApps: List<AppInfo> = emptyList()

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
        val app = apps[position]
        holder.name.text = app.name
        holder.icon.setImageDrawable(app.icon)
        holder.itemView.setOnClickListener { onAppClick(app) }
    }

    override fun getItemCount(): Int = apps.size

    fun setApps(newApps: List<AppInfo>) {
        allApps = newApps
        apps = newApps
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        val filtered = if (query.isEmpty()) {
            allApps
        } else {
            allApps.filter { it.name.contains(query, ignoreCase = true) }
        }
        apps = filtered
        notifyDataSetChanged()
    }
}
