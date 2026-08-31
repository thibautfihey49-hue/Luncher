package com.luncher

import android.app.Application
import android.util.Log
import java.io.File

class LuncherApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            try {
                val f = File(filesDir, "luncher_debug.txt")
                f.appendText("\n--- CRASH ${System.currentTimeMillis()} ---\n${e.message}\n${e.stackTraceToString()}\n")
                Log.e("LUNCHER_DEBUG", "Crash", e)
            } catch(_:Exception){}
        }
    }
}
