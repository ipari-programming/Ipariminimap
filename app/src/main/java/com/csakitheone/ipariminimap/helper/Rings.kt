package com.csakitheone.ipariminimap.helper

import java.util.*

class Rings {
    companion object {
        val times = mapOf(
            "0. óra" to timeToInt(7),
            "0. szünet" to timeToInt(7, 40),
            "1. óra" to timeToInt(7, 45),
            "1. szünet" to timeToInt(8, 30),
            "2. óra" to timeToInt(8, 40),
            "2. szünet" to timeToInt(9, 25),
            "3. óra" to timeToInt(9, 35),
            "3. szünet" to timeToInt(10, 20),
            "4. óra" to timeToInt(10, 30),
            "4. szünet - nagy szünet" to timeToInt(11, 15),
            "5. óra" to timeToInt(11, 30),
            "5. szünet - büfé következő szünet után zár" to timeToInt(12, 15),
            "6. óra - büfé szünet után zár" to timeToInt(12, 25),
            "6. szünet - büfé hamarosan zár" to timeToInt(13, 10),
            "7. óra" to timeToInt(13, 20),
            "7. szünet" to timeToInt(14, 5),
            "8. óra" to timeToInt(14, 15),
            "8. szünet" to timeToInt(15),
        )

        fun timeToInt(h: Int, m: Int = 0, s: Int = 0) : Int = h * 3600 + m * 60 + s
        fun intToTimeS(t: Int) : String {
            val h = t / 3600
            val m = t % 3600 / 60
            val s = t % 3600 % 60
            val hs = if (h < 10) "0$h" else h.toString()
            val ms = if (m < 10) "0$m" else m.toString()
            val ss = if (s < 10) "0$s" else s.toString()
            return if (hs == "00") "$ms:$ss" else "$hs:$ms:$ss"
        }
        fun calToInt(c: Calendar) : Int = c[Calendar.HOUR_OF_DAY] * 3600 + c[Calendar.MINUTE] * 60 + c[Calendar.SECOND]

        fun getCurrentLesson() : String {
            val now = calToInt(Calendar.getInstance())
            val day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

            if (day == Calendar.SATURDAY || day == Calendar.SUNDAY) return "Jó hétvégét!"
            if (now < timeToInt(7)) return "Jó reggelt!"
            if (now > timeToInt(20)) return "Jó éjt!"
            if (now > timeToInt(15)) return "Szép napot!"

            val index = times.values.indexOfFirst { r -> now < r } - 1
            return times.keys.elementAt(index)
        }

        fun getCurrentLessonValue(): Float {
            val now = calToInt(Calendar.getInstance())
            val day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

            if (day == Calendar.SATURDAY || day == Calendar.SUNDAY || now < timeToInt(7) || now > timeToInt(15)) return -1f

            val index = times.values.indexOfFirst { r -> now < r } - 1
            var value = times.keys.elementAt(index)[0].digitToInt().toFloat()
            if (times.keys.elementAt(index).contains("szünet")) value += .9f
            return value
        }

        fun getTimeUntilNext() : String? {
            val now = calToInt(Calendar.getInstance())
            if (times.values.any { r -> now < r }) {
                val next = times.values.first { r -> now < r }
                return  intToTimeS(next - now)
            }
            return null
        }

        fun isLessonStart() : Boolean {
            val now = calToInt(Calendar.getInstance())
            if (times.values.any { r -> now < r }) {
                val next = times.values.first { r -> now < r }
                return getCurrentLesson().contains(". óra") && next - now == timeToInt(0, 45, 0)
            }
            return false
        }

        fun isLessonEnd() : Boolean {
            val now = calToInt(Calendar.getInstance())
            if (times.values.any { r -> now < r }) {
                val next = times.values.first { r -> now < r }
                return getCurrentLesson().contains(". óra") && next - now == timeToInt(0, 0, 1)
            }
            return false
        }

        fun getLessonIndex() : Int {
            val now = calToInt(Calendar.getInstance())
            return if (now > timeToInt(7) && now < timeToInt(15)) getCurrentLesson().split('.')[0].toInt() else -1
        }
    }
}