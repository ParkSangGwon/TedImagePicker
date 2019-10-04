package gun0912.tedimagepicker.extenstion

import android.graphics.Color
import kotlin.math.min
import kotlin.math.round

fun Int.manipulate(factor: Float = 1f, alpha: Float = 1f): Int {
    val a = (255f * alpha).toInt()
    val r = round((Color.red(this) * factor)).toInt()
    val g = round((Color.green(this) * factor)).toInt()
    val b = round((Color.blue(this) * factor)).toInt()

    return Color.argb(a,
        min(r, 255),
        min(g, 255),
        min(b, 255)
    )
}