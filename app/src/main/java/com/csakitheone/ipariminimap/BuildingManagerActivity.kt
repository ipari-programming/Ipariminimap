package com.csakitheone.ipariminimap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.csakitheone.ipariminimap.data.Prefs

class BuildingManagerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_building_manager)

        if (!Prefs.getIsAdmin()) finish()
    }
}