package com.luncher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.luncher.data.AppInfo

class AppAdapter(private val click: (AppInfo) -> Unit) : RecyclerView.Adapter<AppAdapter.Holder>() {
    private var list = emptyList<AppInfo>()
    
    class Holder(v: View) : RecyclerView.ViewHolder(v) {
        val icon: ImageView = v.findViewById(R.id.app_icon)
        val name: TextView = v.findViewById(R.id.app_name)
    }
    
    override fun onCreateViewHolder(p: ViewGroup, t: Int) = 
        Holder(LayoutInflater.from(p.context).inflate(R.layout.item_app, p, false))
    
    override fun onBindViewHolder(h: Holder, i: Int) {
        val app = list[i]
        h.name.text = app.name
        h.icon.setImageDrawable(app.icon)
        h.itemView.setOnClickListener { click(app) }
    }
    
    override fun getItemCount() = list.size
    
    fun setList(new: List<AppInfo>) {
        list = new
        notifyDataSetChanged()
    }
}
