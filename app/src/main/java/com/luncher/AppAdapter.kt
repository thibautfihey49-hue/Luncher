package com.luncher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppAdapter(private var apps: MutableList<AppInfo>, private val onClick: (AppInfo)->Unit) : RecyclerView.Adapter<AppAdapter.H>() {
    class H(v: View): RecyclerView.ViewHolder(v){
        val icon: ImageView? = v.findViewById(R.id.appIcon)
        val label: TextView? = v.findViewById(R.id.appLabel)
    }
    override fun onCreateViewHolder(p: ViewGroup, t: Int): H {
        return try {
            val v = LayoutInflater.from(p.context).inflate(R.layout.item_app, p, false)
            H(v)
        } catch(e:Exception){
            val tv = TextView(p.context)
            tv.layoutParams = ViewGroup.LayoutParams(200,200)
            H(tv)
        }
    }
    override fun getItemCount() = apps.size
    override fun onBindViewHolder(h: H, pos: Int) {
        try{
            val app = apps[pos]
            h.label?.text = app.label
            try{ h.icon?.setImageDrawable(app.icon) }catch(_:Exception){}
            h.itemView.setOnClickListener{ try{ onClick(app) }catch(_:Exception){} }
        }catch(_:Exception){}
    }
    fun update(newApps: List<AppInfo>){
        try{
            apps = newApps.toMutableList()
            notifyDataSetChanged()
        }catch(_:Exception){}
    }
}
