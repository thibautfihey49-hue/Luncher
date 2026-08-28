package com.luncher; import android.view.*; import android.widget.*; import androidx.recyclerview.widget.RecyclerView; import com.luncher.data.AppInfo
class AppAdapter(private val cb:(AppInfo)->Unit):RecyclerView.Adapter<AppAdapter.VH>(){
    private var all=listOf<AppInfo>(); private var filtered=all
    override fun onCreateViewHolder(p:ViewGroup,t:Int):VH=VH(LayoutInflater.from(p.context).inflate(R.layout.item_app,p,false))
    override fun onBindViewHolder(h:VH,i:Int)=h.bind(filtered[i])
    override fun getItemCount()=filtered.size
    fun setApps(a:List<AppInfo>){all=a;filtered=a;notifyDataSetChanged()}
    fun filter(q:String){val s=q.lowercase();filtered=if(s.isEmpty())all else all.filter{it.name.lowercase().contains(s)};notifyDataSetChanged()}
    inner class VH(v:View):RecyclerView.ViewHolder(v){private val i=v.findViewById<ImageView>(R.id.app_icon);private val n=v.findViewById<TextView>(R.id.app_name);fun bind(a:AppInfo){n.text=a.name;i.setImageDrawable(a.icon);v.setOnClickListener{cb(a)}}}
}
