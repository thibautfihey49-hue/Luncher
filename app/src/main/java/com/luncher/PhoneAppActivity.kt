
package com.luncher
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PhoneAppActivity : AppCompatActivity() {
    private lateinit var numberInput: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_phone)
            numberInput = findViewById(R.id.numberInput)
            val btnCall = findViewById<Button>(R.id.btnCall)
            // chiffres
            val ids = mapOf(R.id.btn0 to "0", R.id.btn1 to "1", R.id.btn2 to "2", R.id.btn3 to "3", R.id.btn4 to "4", R.id.btn5 to "5", R.id.btn6 to "6", R.id.btn7 to "7", R.id.btn8 to "8", R.id.btn9 to "9", R.id.btnStar to "*", R.id.btnHash to "#")
            for((id,digit) in ids){ findViewById<TextView>(id)?.setOnClickListener{ numberInput.append(digit) } }
            findViewById<View>(R.id.btnDelete)?.setOnClickListener{
                val txt = numberInput.text.toString()
                if(txt.isNotEmpty()) numberInput.setText(txt.dropLast(1))
            }
            btnCall.setOnClickListener{
                try{
                    val num = numberInput.text.toString()
                    if(num.isBlank()) return@setOnClickListener
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)!=PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), 1)
                        return@setOnClickListener
                    }
                    startActivity(Intent(Intent.ACTION_CALL, Uri.parse("tel:"+num)))
                }catch(e:Exception){
                    try{ startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+numberInput.text.toString()))) }catch(_:Exception){}
                }
            }
        } catch(e:Exception){
            val tv = TextView(this)
            tv.text = "Phone erreur: "+e.message
            setContentView(tv)
        }
    }
}
