package com.luncher
import android.app.Application
class LuncherApp: Application() {
    override fun onCreate() { super.onCreate() }
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        // Vide cache dès que système a besoin RAM
        if(level >= TRIM_MEMORY_MODERATE) { System.gc() }
    }
}
