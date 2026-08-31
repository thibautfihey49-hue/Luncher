package com.luncher
import android.app.Application
import android.util.Log
import java.io.File
class LuncherApp: Application(){
    override fun onCreate(){
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler{_,e->
            try{ File(filesDir,"luncher_debug.txt").appendText("\n---CRASH ${System.currentTimeMillis()}---\n${e.stackTraceToString()}\n"); Log.e("LUNCHER_DEBUG","",e)}catch(_:Exception){}
        }
    }
}
