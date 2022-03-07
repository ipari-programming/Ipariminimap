package com.csakitheone.ipariminimap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.csakitheone.ipariminimap.data.Prefs
import com.csakitheone.ipariminimap.data.Temp
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_reward_ad.*

class RewardAdActivity : AppCompatActivity() {
    var mainRewardedAd: RewardedAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reward_ad)

        val adCount = Prefs.getAdCount()

        if (adCount < 10) rewardAdText.text = "${rewardAdText.text}\nKövetkező kitűzőig: ${10 - adCount}"
        else if (adCount < 100) rewardAdText.text = "${rewardAdText.text}\nKövetkező kitűzőig: ${100 - adCount}"

        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTestDeviceIds(listOf("24E9E518AB9DBE2924B9B93F22361702"))
                .build()
        )
        MobileAds.initialize(this)

        RewardedAd.load(this, "ca-app-pub-5995992409743558/3639482674", AdRequest.Builder().build(), object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(p0: LoadAdError) {
                finish()
            }
            override fun onAdLoaded(rewardedAd: RewardedAd) {
                mainRewardedAd = rewardedAd
                showRewardedAd()
            }
        })
    }

    fun showRewardedAd() {
        if (mainRewardedAd == null) {
            Toast.makeText(this, "Próbáld meg egy kicsit később", Toast.LENGTH_SHORT).show()
            return
        }
        mainRewardedAd!!.show(this) {
            Prefs.increaseAdCount()
            val adCount = Prefs.getAdCount()

            Temp.isAdWatched = true

            finish()
        }
    }
}