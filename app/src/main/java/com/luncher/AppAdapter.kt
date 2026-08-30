package com.luncher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
class AppAdapter(private var apps: MutableList<AppInfo>, private val onClick:(AppInfo)->Unit): RecyclerView.Adapter<AppAdapter.H>(){
    inner class H(v:View): RecyclerView.ViewHolder(v){ val icon:ImageView=v.findViewById(R.id.appIcon); val name:TextView=v.findViewById(R.id.appName) }
    override fun onCreateViewHolder(p:ViewGroup, t:Int): H { val v=LayoutInflater.from(p.context).inflate(R.layout.item_app_drawer, p, false); return H(v) }
    override fun getItemCount()=apps.size
    override fun onBindViewHolder(h:H, i:Int){ val a=apps[i]; h.icon.setImageDrawable(a.icon); h.name.text=a.label; h.itemView.setOnClickListener{ onClick(a) } }
    fun update(newList:List<AppInfo>){ apps=newList.toMutableList(); notifyDataSetChanged() }
}
