package com.csakitheone.ipariminimap.view

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.preference.PreferenceManager
import com.csakitheone.ipariminimap.R
import java.util.*
import kotlin.concurrent.timerTask
import kotlin.math.round
import kotlin.math.roundToInt

class GameView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    lateinit var activity: Activity

    var onGameStarted: (() -> Unit)? = null
    var onGameEnded: (() -> Unit)? = null
    var onGameTick: ((score: Int) -> Unit)? = null

    var framerate = 30
    // default, light, death
    var gameState: String = "default"
    var isGameRunning: Boolean = false
    var speed: Float = 0f
    var distance: Float = 0f
    var playerOffset: Float = 0f
    var inputX: Float = -1f
    var inputY: Float = -1f
    var nextObstacleTime = -1f

    var playerX: Float = -1f
    var playerY: Float = -1f
    var obstacles: MutableList<GameObstacle> = mutableListOf()

    val startingHeight = 300
    val obstacleSpawnPeriod = 500f

    val paintBackground = Paint().apply {
        style = Paint.Style.FILL
        // HALVÁNYABB: color = Color.argb(255, 193, 239, 252)
        color = Color.argb(255, 51, 153, 255)
    }

    val paintDeath = Paint().apply {
        style = Paint.Style.FILL
        color = Color.argb(255, 255, 255, 255)
    }

    val paintInput = Paint().apply {
        color = Color.argb(64, 255, 255, 255)
    }

    val paintDefault = Paint().apply {
        strokeWidth = 5f
        style = Paint.Style.FILL
    }

    val assetBridge = BitmapFactory.decodeResource(resources, R.drawable.game_asset_bridge, BitmapFactory.Options().apply { inScaled = false })
    val assetBridgeBounds = Rect(0, 0, 1858, 543)
    var assetBridgeProperties = Rect()
    val assetNet = BitmapFactory.decodeResource(resources, R.drawable.game_asset_net, BitmapFactory.Options().apply { inScaled = false })
    val assetNetBounds = Rect(0, 0, 900, 460)
    var assetNetProperties = Rect()

    fun getDeltaTime() : Float {
        return 1000f / framerate
    }

    fun getScore() : Int {
        return round(distance / 100).toInt()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (playerX == -1f) playerX = measuredWidth / 2f
        if (playerY == -1f) playerY = startingHeight.toFloat()

        assetBridgeProperties.set(
            -50,
            startingHeight - distance.toInt(),
            measuredWidth + 50,
            startingHeight + ((measuredWidth + 100) * 543 / 1858f).toInt() - distance.toInt()
        )

        assetNetProperties.set(
            -80,
            (measuredHeight * .75f).toInt() - ((measuredWidth + 160) * 460 / 900f / 2).toInt(),
            measuredWidth + 80,
            (measuredHeight * .75f).toInt() + ((measuredWidth + 160) * 460 / 900f / 2).toInt()
        )

        canvas.apply {
            if (gameState == "default") {
                // Background
                drawRect(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), paintBackground)
                drawBitmap(assetBridge, assetBridgeBounds, assetBridgeProperties, paintDefault)
                // Player
                drawCircle(playerX, playerY, 20f, paintDefault)
                // Obstacles
                for (o in obstacles) {
                    drawRect(o.x - o.w / 2, o.y - o.h / 2, o.x + o.w / 2, o.y + o.h / 2, paintDefault)
                }
            }
            else if (gameState == "light") {
                drawRect(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), paintDeath)
            }
            else if (gameState == "death") {
                // Background
                drawRect(0f, 0f, measuredWidth.toFloat(), measuredHeight.toFloat(), paintBackground)
                drawBitmap(assetNet, assetNetBounds, assetNetProperties, paintDefault)
                // Player
                drawCircle(playerX, playerY, 20f, paintDefault)
            }
        }
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        inputX = e.x
        inputY = e.y
        if (!isGameRunning && gameState == "default") startGame()
        return true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = 100
        val desiredHeight = 100
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val width: Int
        val height: Int

        width = if (widthMode == MeasureSpec.EXACTLY) {
            widthSize
        } else if (widthMode == MeasureSpec.AT_MOST) {
            Math.min(desiredWidth, widthSize)
        } else {
            desiredWidth
        }
        height = if (heightMode == MeasureSpec.EXACTLY) {
            heightSize
        } else if (heightMode == MeasureSpec.AT_MOST) {
            Math.min(desiredHeight, heightSize)
        } else {
            desiredHeight
        }
        setMeasuredDimension(width, height)
    }

    fun startGame() {
        gameState = "default"
        isGameRunning = true
        onGameStarted?.invoke()
        speed = measuredHeight / 150f
        distance = 0f
        ValueAnimator.ofFloat(0f, -150f).apply {
            duration = 300
            addUpdateListener {
                playerOffset = it.animatedValue as Float
            }
            start()
        }
        ValueAnimator.ofFloat(-150f, measuredHeight / 4f).apply {
            startDelay = 300
            duration = 60000
            addUpdateListener {
                playerOffset = it.animatedValue as Float
            }
            start()
        }
        obstacles = mutableListOf()
        nextObstacleTime = obstacleSpawnPeriod
        tick()
    }

    private fun endGame() {
        isGameRunning = false
        playerX = measuredWidth / 2f
        activity.runOnUiThread {
            ValueAnimator.ofFloat(0f, .8f).apply {
                duration = 2000
                addUpdateListener {
                    gameState = if (currentPlayTime < 200) "light"
                    else "death"
                    playerY = measuredHeight * animatedValue as Float
                    invalidate()
                }
                start()
            }
        }

        onGameEnded?.invoke()
    }

    private fun addObstacle() {
        nextObstacleTime = obstacleSpawnPeriod
        obstacles.add(GameObstacle.random(measuredWidth, measuredHeight))
        obstacles.removeAll { it.y < 0 }
    }

    private fun testCollision() {
        for (o in obstacles) {
            if (o.isPointInside(playerX, playerY)) {
                endGame()
                return
            }
        }
    }

    private fun tick() {
        distance += speed
        if (speed < measuredHeight / 5f) speed += .02f

        playerX = if (distance < 1000) (inputX - (measuredWidth / 2)) * (distance / 1000) + (measuredWidth / 2)
        else inputX
        playerY = startingHeight + playerOffset

        if (nextObstacleTime < 0) addObstacle()
        else nextObstacleTime -= speed
        for (i in 0 until obstacles.size) {
            obstacles[i].y -= speed
        }

        testCollision()

        invalidate()
        if (isGameRunning) onGameTick?.invoke(getScore())

        Timer().schedule(timerTask {
            if (isGameRunning) tick()
        }, 1000 / framerate.toLong())
    }

}

data class GameObstacle(
    var x: Float,
    var y: Float,
    var w: Float,
    var h: Float
) {
    fun isPointInside(px: Float, py: Float) : Boolean {
        return px > x - w / 2 && px < x + w / 2 &&
                py > y - h / 2 && py < y + h / 2
    }

    companion object {
        fun random(screenWidth: Int, screenHeight: Int) : GameObstacle {
            return GameObstacle(
                (0..screenWidth).random().toFloat(),
                screenHeight.toFloat() * 1.2f,
                (100..screenWidth / 2).random().toFloat(),
                (50..200).random().toFloat()
            )
        }
    }
}