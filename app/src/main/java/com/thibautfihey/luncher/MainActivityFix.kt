package com.thibautfihey.luncher
import android.view.View
import android.view.ViewGroup

fun attachThemeButton(root: View) {
    fun walk(v: View) {
        // si vue en haut à droite (x > 70% écran, y < 15%)
        try {
            val loc = IntArray(2)
            v.getLocationOnScreen(loc)
            val x = loc[0]
            val y = loc[1]
            if (x > 600 && y < 400 && v.isClickable) {
                v.setOnClickListener {
                    val ctx = v.context
                    ctx.startActivity(android.content.Intent(ctx, com.thibautfihey.luncher.ThemeSettingsActivity::class.java))
                }
            }
            // aussi si c'est une ImageView/ImageButton dans le header
            if (v is ViewGroup) {
                for (i in 0 until v.childCount) walk(v.getChildAt(i))
            } else {
                // brute force: toute vue en haut droite devient cliquable
                if (y < 300 && x > 500) {
                    v.isClickable = true
                    v.setOnClickListener {
                        val ctx = v.context
                        ctx.startActivity(android.content.Intent(ctx, com.thibautfihey.luncher.ThemeSettingsActivity::class.java))
                    }
                }
            }
        } catch(_:Exception){}
    }
    walk(root)
}
