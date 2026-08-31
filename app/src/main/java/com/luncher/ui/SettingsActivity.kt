package com.luncher.ui
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.luncher.util.GlassUtil
class SettingsActivity: AppCompatActivity(){
    override fun onCreate(b:Bundle?){
        super.onCreate(b)
        val root=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; background=GlassUtil.bgLiquid(null); setPadding(50,120,50,50)}
        root.addView(TextView(this).apply{ text="Settings OK"; textSize=22f; setTextColor(Color.WHITE)})
        setContentView(root)
    }
}
