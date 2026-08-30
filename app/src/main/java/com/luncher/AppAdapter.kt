package com.luncher
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

data class AppInfo(val label:String, val packageName:String, val className:String, val icon: Drawable)

class AppAdapter(private var apps: MutableList<AppInfo>, private val onClick:(AppInfo)->Unit): RecyclerView.Adapter<AppAdapter.H>() {
    class H(v: View): RecyclerView.ViewHolder(v){
        val icon: ImageView = v.findViewById(R.id.appIcon)
        val name: TextView = v.findViewById(R.id.appName)
    }
    override fun onCreateViewHolder(p: ViewGroup, t: Int): H {
        val v = LayoutInflater.from(p.context).inflate(R.layout.item_app_drawer, p, false)
        return H(v)
    }
    override fun getItemCount()=apps.size
    override fun onBindViewHolder(h:H, pos:Int){
        val a = apps[pos]
        h.name.text = a.label
        // Icon set direct, pas de resize coûteux
        h.icon.setImageDrawable(a.icon)
        h.itemView.setOnClickListener{ onClick(a) }
    }
    fun update(newList: List<AppInfo>){
        val diff = DiffUtil.calculateDiff(object: DiffUtil.Callback(){
            override fun getOldListSize()=apps.size
            override fun getNewListSize()=newList.size
            override fun areItemsTheSame(o:Int,n:Int)=apps[o].packageName==newList[n].packageName
            override fun areContentsTheSame(o:Int,n:Int)=apps[o].label==newList[n].label
        }, true)
        apps = newList.toMutableList()
        diff.dispatchUpdatesTo(this)
    }
}
