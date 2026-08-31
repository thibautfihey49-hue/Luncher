package com.luncher
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.luncher.util.ThemeUtil
class PhoneAppActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply{ text="Phone OK"; setPadding(40,40,40,40); setBackgroundColor(ThemeUtil.getBg(this@PhoneAppActivity)) })
    }
}
