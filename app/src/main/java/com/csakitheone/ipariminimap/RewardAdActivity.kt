package com.csakitheone.ipariminimap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.csakitheone.ipariminimap.data.Temp
import com.csakitheone.ipariminimap.databinding.ActivityRewardAdBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class RewardAdActivity : AppCompatActivity() {

    lateinit var binding: ActivityRewardAdBinding

    var mainRewardedAd: RewardedAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRewardAdBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            Temp.isAdWatched = true
            finish()
        }
    }
}