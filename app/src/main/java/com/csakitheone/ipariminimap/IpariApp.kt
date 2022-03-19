package com.csakitheone.ipariminimap

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.csakitheone.ipariminimap.data.Prefs
import com.google.android.material.color.DynamicColors

class IpariApp: Application() {
    override fun onCreate() {
        super.onCreate()
        Prefs.init(this)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        /*
        if (Prefs.getIsUsingDynamicColors()) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }
        */
    }
}