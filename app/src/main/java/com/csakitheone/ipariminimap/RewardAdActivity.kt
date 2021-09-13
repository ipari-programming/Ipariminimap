package com.csakitheone.ipariminimap

import android.app.Activity
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.preference.PreferenceManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_reward_ad.*

class RewardAdActivity : AppCompatActivity() {
    lateinit var prefs: SharedPreferences
    lateinit var mainRewardedAd: RewardedAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reward_ad)

        prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val adCount = prefs.getInt("mainRewardedAd.watchCount", 0)

        if (adCount < 10) rewardAdText.text = "${rewardAdText.text}\nKövetkező kitűzőig: ${10 - adCount}"
        else if (adCount < 100) rewardAdText.text = "${rewardAdText.text}\nKövetkező kitűzőig: ${100 - adCount}"

        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTestDeviceIds(listOf("24E9E518AB9DBE2924B9B93F22361702"))
                .build()
        )
        MobileAds.initialize(this)

        mainRewardedAd = RewardedAd(this, "ca-app-pub-5995992409743558/3639482674")
        mainRewardedAd.loadAd(AdRequest.Builder().build(), object : RewardedAdLoadCallback() {
            override fun onRewardedAdFailedToLoad(p0: LoadAdError?) {
                finish()
            }
            override fun onRewardedAdLoaded() {
                showRewardedAd()
            }
        })
    }

    fun showRewardedAd() {
        if (mainRewardedAd.isLoaded) {
            val activityContext: Activity = this
            val adCallback = object: RewardedAdCallback() {
                override fun onUserEarnedReward(@NonNull reward: RewardItem) {
                    prefs.edit().apply {
                        putInt("mainRewardedAd.watchCount", prefs.getInt("mainRewardedAd.watchCount", 0) + 1)
                        apply()
                    }
                    if (prefs.getInt("mainRewardedAd.watchCount", 0) > 9) Badge.userAdd(this@RewardAdActivity, Badge.BADGE_TAMOGATO.toString())
                    if (prefs.getInt("mainRewardedAd.watchCount", 0) > 99) Badge.userAdd(this@RewardAdActivity, Badge.BADGE_BEFEKTETO.toString())
                }
                override fun onRewardedAdClosed() {
                    finish()
                }
            }
            mainRewardedAd.show(activityContext, adCallback)
        }
        else {
            Toast.makeText(this, "Próbáld meg egy kicsit később", Toast.LENGTH_SHORT).show()
            mainRewardedAd.loadAd(AdRequest.Builder().build(), object : RewardedAdLoadCallback() {})
        }
    }
}