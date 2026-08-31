package com.luncher
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.luncher.util.ThemeUtil
class SmsAppActivity:AppCompatActivity(){
    override fun onCreate(b:Bundle?){ super.onCreate(b)
        val bg=ThemeUtil.getBg(this); setContentView(TextView(this).apply{ text="SMS\nFonction à implémenter"; setPadding(40,100,40,40); setBackgroundColor(bg); setTextColor(ThemeUtil.getTextColor(bg))}) }
}
