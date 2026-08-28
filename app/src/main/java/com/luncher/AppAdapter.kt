package com.luncher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.luncher.data.AppInfo

class AppAdapter(private val onAppClick: (AppInfo) -> Unit) :
    RecyclerView.Adapter<AppAdapter.AppViewHolder>() {

    private var allApps = listOf<AppInfo>()
    private var filteredApps = listOf<AppInfo>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(filteredApps[position])
    }

    override fun getItemCount(): Int = filteredApps.size

    fun setApps(apps: List<AppInfo>) {
        allApps = apps
        filteredApps = apps
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        val q = query.lowercase()
        filteredApps = if (q.isEmpty()) allApps
        else allApps.filter { it.name.lowercase().contains(q) }
        notifyDataSetChanged()
    }

    inner class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: ImageView = itemView.findViewById(R.id.app_icon)
        private val name: TextView = itemView.findViewById(R.id.app_name)

        fun bind(app: AppInfo) {
            name.text = app.name
            icon.setImageDrawable(app.icon)
            itemView.setOnClickListener { onAppClick(app) }
        }
    }
}
