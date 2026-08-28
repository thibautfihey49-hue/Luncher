package com.luncher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.luncher.data.AppInfo

class AppAdapter(
    private val onAppClick: (AppInfo) -> Unit
) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {
    
    private var allApps = listOf<AppInfo>()
    private var filtered = listOf<AppInfo>()

    inner class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.app_icon)
        val name: TextView = view.findViewById(R.id.app_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = filtered[position]
        holder.icon.setImageDrawable(app.icon)
        holder.name.text = app.name
        holder.itemView.setOnClickListener { onAppClick(app) }
    }

    override fun getItemCount(): Int = filtered.size

    fun setList(list: List<AppInfo>) {
        allApps = list
        filtered = list
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        filtered = if (query.isEmpty()) allApps else allApps.filter { 
            it.name.contains(query, ignoreCase = true) 
        }
        notifyDataSetChanged()
    }
}
