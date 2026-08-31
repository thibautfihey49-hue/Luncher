package com.luncher.widgets

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.luncher.util.GlassUtil
import java.text.SimpleDateFormat
import java.util.*

class SidebarWidget(ctx: Context): LinearLayout(ctx){
    init{
        orientation=VERTICAL
        val dateFormat = SimpleDateFormat("MMM", Locale.ENGLISH)
        val day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH).toString()
        val month = dateFormat.format(Date())

        val topRow = LinearLayout(context).apply{ orientation=HORIZONTAL}
        // Date card
        val dateCard = LinearLayout(context).apply{
            orientation=VERTICAL
            background=GlassUtil.liquidCardSmall()
            setPadding(24,20,24,20)
            layoutParams=LayoutParams(0,-2,1f).apply{ setMargins(0,0,8,0)}
        }
        dateCard.addView(TextView(context).apply{ text=month; textSize=12f; setTextColor(Color.parseColor("#6B7280"))})
        dateCard.addView(TextView(context).apply{ text="$day  Fri"; textSize=20f; setTextColor(Color.parseColor("#111827")); typeface=android.graphics.Typeface.DEFAULT_BOLD})
        val storageRow = LinearLayout(context).apply{ orientation=HORIZONTAL; setPadding(0,16,0,0)}
        storageRow.addView(TextView(context).apply{ text="💾  8.7/12G"; textSize=11f; setTextColor(Color.parseColor("#111827"))})
        dateCard.addView(storageRow)
        val battRow = LinearLayout(context).apply{ orientation=HORIZONTAL; setPadding(0,6,0,0)}
        battRow.addView(TextView(context).apply{ text="🔋  67%"; textSize=11f; setTextColor(Color.parseColor("#111827"))})
        dateCard.addView(battRow)

        // Quote card
        val quoteCard = LinearLayout(context).apply{
            orientation=VERTICAL
            background=GlassUtil.liquidCardSmall()
            setPadding(24,20,24,20)
            layoutParams=LayoutParams(0,-2,1f).apply{ setMargins(8,0,0,0)}
        }
        quoteCard.addView(TextView(context).apply{ text="Laughter is the\nbest medicine."; textSize=12f; setTextColor(Color.parseColor("#111827"))})
        quoteCard.addView(TextView(context).apply{ text="😜"; textSize=32f; gravity=Gravity.END; setPadding(0,20,0,0)})

        topRow.addView(dateCard); topRow.addView(quoteCard)
        addView(topRow)

        // Color dots grid like screenshot
        val grid = LinearLayout(context).apply{ orientation=VERTICAL; background=GlassUtil.liquidFolder(); setPadding(20,20,20,20); layoutParams=LayoutParams(-1,-2).apply{ setMargins(0,16,0,0)}}
        val colors = listOf("#9CA3FF","#FF9CA8","#C99CFF","#9CA3AF","#FFE082","#A8D5BA","#FFF0A0","#8C9EFF")
        for(r in 0..1){
            val row = LinearLayout(context).apply{ orientation=HORIZONTAL; gravity=Gravity.CENTER}
            for(c in 0..3){
                val dot = TextView(context).apply{
                    layoutParams=LayoutParams(88,88).apply{ setMargins(12,12,12,12)}
                    background=GlassUtil.colorDot(colors[r*4+c])
                }
                row.addView(dot)
            }
            grid.addView(row)
        }
        addView(grid)
    }
}
