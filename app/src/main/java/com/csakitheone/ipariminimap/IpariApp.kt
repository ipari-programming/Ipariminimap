package com.csakitheone.ipariminimap

import android.app.Application
import com.csakitheone.ipariminimap.data.Prefs
import com.google.android.material.color.DynamicColors

class IpariApp: Application() {
    override fun onCreate() {
        super.onCreate()
        Prefs.init(this)

        if (Prefs.getIsUsingDynamicColors()) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }
    }
}