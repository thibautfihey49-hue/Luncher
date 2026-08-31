package com.luncher.util
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
data class UltraTheme(val id:String,val name:String,val desc:String,val bgType:Int,val bgColors:List<String>,val bgOrientation:GradientDrawable.Orientation=GradientDrawable.Orientation.TL_BR,val textColor:String,val secondaryText:String,val accent:String,val accent2:String,val searchShape:Int,val searchBg:String,val searchTextColor:String,val listStyle:Int,val listCorner:Float,val listSpacing:Int,val fontFamily:Int,val fontSize:Float,val fontBold:Boolean,val bottomStyle:Int,val wrenchIcon:String,val bottomIcons:List<String>,val statusBarLight:Boolean)
object ThemeUtil{
    const val PREFS="luncher"
    val themes=listOf(
        UltraTheme("og","Original Luncher","Blanc clean",0,listOf("#F5F5F7"),GradientDrawable.Orientation.TOP_BOTTOM,"#1A1A1A","#888888","#6A5ACD","#8A7CFF",0,"#FFFFFF","#000000",0,60f,0,0,18f,false,0,"🔧",listOf("📞","💬","📁"),true),
        UltraTheme("amoled","AMOLED", "Noir OLED",0,listOf("#000000"),GradientDrawable.Orientation.TOP_BOTTOM,"#FFFFFF","#888","#FFFFFF","#333",0,"#111111","#FFFFFF",0,60f,0,0,18f,false,0,"⚙️",listOf("📞","💬","📁"),false),
        UltraTheme("midnight","Midnight Gradient","Dégradé nuit",1,listOf("#0F0C29","#302B63"),GradientDrawable.Orientation.TL_BR,"#FFFFFF","#A0A0C0","#00DBDE","#FC00FF",0,"#1A1A3A","#FFFFFF",0,60f,0,0,18f,false,0,"🌙",listOf("📞","💬","📁"),false)
    )
    fun get(c:Context):UltraTheme{ val id=c.getSharedPreferences(PREFS,Context.MODE_PRIVATE).getString("theme_id","og")!!; return themes.find{it.id==id}?:themes[0] }
    fun save(c:Context,t:UltraTheme){ c.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit().putString("theme_id",t.id).apply() }
    fun drawable(t:UltraTheme):GradientDrawable = if(t.bgType==0) GradientDrawable().apply{ setColor(Color.parseColor(t.bgColors[0]))} else GradientDrawable(t.bgOrientation, t.bgColors.map{Color.parseColor(it)}.toIntArray())
    fun typeface(t:UltraTheme):Typeface = Typeface.DEFAULT
}
