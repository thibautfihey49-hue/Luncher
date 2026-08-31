package com.thibautfihey.luncher
import android.content.Context
import android.content.Intent
object ThemeLauncher {
    fun open(ctx: Context) {
        ctx.startActivity(Intent(ctx, ThemeSettingsActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
    }
}
