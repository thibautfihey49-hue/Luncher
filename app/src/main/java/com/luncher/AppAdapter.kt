package com.luncher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.luncher.data.AppInfo

class AppAdapter(private val onAppClick: (AppInfo) -> Unit) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {
    
    private var fullList = listOf<AppInfo>()
    private var filteredList = listOf<AppInfo>()

    inner class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.app_icon)
        val name: TextView = view.findViewById(R.id.app_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = filteredList[position]
        holder.name.text = app.name
        holder.icon.setImageDrawable(app.icon)
        holder.itemView.setOnClickListener { onAppClick(app) }
    }

    override fun getItemCount() = filteredList.size

    fun setList(list: List<AppInfo>) {
        fullList = list
        filteredList = list
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        filteredList = if (query.isEmpty()) fullList 
            else fullList.filter { it.name.contains(query, ignoreCase = true) }
        notifyDataSetChanged()
    }
}
