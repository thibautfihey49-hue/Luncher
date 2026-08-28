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

    private var apps: List<AppInfo> = emptyList()

    inner class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.app_icon)
        val name: TextView = view.findViewById(R.id.app_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
        return AppViewHolder(v)
    }

    override fun onBindViewHolder(h: AppViewHolder, i: Int) {
        val app = apps[i]
        h.name.text = app.name
        h.icon.setImageDrawable(app.icon)
        h.itemView.setOnClickListener { onAppClick(app) }
    }

    override fun getItemCount() = apps.size

    fun setList(list: List<AppInfo>) {
        apps = list
        notifyDataSetChanged()
    }
}
