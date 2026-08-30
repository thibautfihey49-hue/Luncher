
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
        val v = LayoutInflater.from(p.context).inflate(R.layout.item_app, p, false)
        return H(v)
    }
    override fun getItemCount(): Int = apps.size
    override fun onBindViewHolder(h: H, pos: Int) {
        try{
            val app = apps[pos]
            h.label?.text = app.label
            try{ h.icon?.setImageDrawable(app.icon) }catch(_:Exception){}
            h.itemView.setOnClickListener{ try{ onClick(app) }catch(_:Exception){} }
        }catch(_:Exception){}
    }
    fun update(newApps: List<AppInfo>){
        apps = newApps.toMutableList()
        try{ notifyDataSetChanged() }catch(_:Exception){}
    }
}
