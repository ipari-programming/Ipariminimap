package com.csakitheone.ipariminimap

import android.animation.ValueAnimator
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.SharedMemory
import android.view.View
import android.view.View.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_secret_game.*
import kotlin.math.round


class SecretGameActivity : AppCompatActivity() {

    lateinit var prefs : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_secret_game)

        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTestDeviceIds(listOf("24E9E518AB9DBE2924B9B93F22361702"))
                .build()
        )
        MobileAds.initialize(this)

        gameBanner.loadAd(AdRequest.Builder().build())

        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        gameTextDistance.text = "Rekord: ${prefs.getInt("game_score", 0)}"

        gameView.activity = this

        gameView.onGameStarted = {
            gameBtnRestart.visibility = GONE
            ValueAnimator.ofFloat(1f, 0f).apply {
                duration = 1000
                addUpdateListener {
                    gameTextTitle.alpha = it.animatedValue as Float
                }
                start()
            }
        }
        gameView.onGameEnded = {
            runOnUiThread {
                gameBtnRestart.alpha = 0f
                gameBtnRestart.visibility = VISIBLE
                ValueAnimator.ofFloat(0f, 1f).apply {
                    startDelay = 500
                    duration = 1500
                    addUpdateListener {
                        gameTextTitle.alpha = it.animatedValue as Float
                        gameBtnRestart.alpha = it.animatedValue as Float
                    }
                    start()
                }
                if (gameView.getScore() > prefs.getInt("game_score", 0)) {
                    gameTextDistance.text = "Új rekord: ${gameView.getScore()}"
                    prefs.edit {
                        putInt("game_score", gameView.getScore())
                        apply()
                    }
                }
                else {
                    gameTextDistance.text = "${gameView.getScore()}\nRekord: ${prefs.getInt("game_score", 0)}"
                }
            }
        }
        gameView.onGameTick = {
            runOnUiThread {
                gameTextDistance.text = it.toString()
            }
        }
    }

    fun btnRestartClick(view: View) {
        if (gameBtnRestart.alpha == 1f) {
            finish()
            startActivity(Intent(this, SecretGameActivity::class.java))
        }
    }
}