package com.csakitheone.ipariminimap.helper

import android.content.res.Resources
import android.util.TypedValue

class Helper {
    companion object {
        val Number.toPx get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            Resources.getSystem().displayMetrics)
    }
}