package com.kanha.arrowflow

import android.app.Activity
import android.os.Bundle
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.view.*
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import kotlin.math.min
import kotlin.math.max
import kotlin.random.Random

class MainActivity : Activity() {
    private var inGame = false
    private var dailyMode = false

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        showHome()
    }

    private fun showHome() {
        inGame = false
        dailyMode = false
        setContentView(HomeView(this, onPlay = { showGame(false) }, onDaily = { showGame(true) }))
    }

    private fun showGame(daily: Boolean) {
        inGame = true
        dailyMode = daily
        window.setBackgroundDrawable(ColorDrawable(Color.rgb(246, 247, 251)))
        setContentView(GameView(this, daily))
    }

    override fun onBackPressed() {
        if (inGame) showHome() else super.onBackPressed()
    }
}

class GameView(private val ctx: Context, private val dailyMode: Boolean = false) : View(ctx) {
    private val save = SaveManager(ctx)
    private val vm = GameViewModel(
        save,
        if (dailyMode) DailyChallengeManager.puzzleForToday() else null
    )
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var boardTop = 0f
    private var cell = 0f
    private var hintId: Int? = null
    private var complete = false
    private var selectedId: Int? = null
    private var selectedProgress = 1f
    private var selectedStart = 0L
    private var shakeUntil = 0L
    private var shakeX = 0f
    private var particles = mutableListOf<Particle>()
    private var completionStarted = false

    private data class Particle(
        var x: Float, var y: Float, var vx: Float, var vy: Float,
        var life: Float, val radius: Float
    )

    private fun theme() = ThemeManager.get(save.selectedTheme)

    init {
        if (dailyMode) save.syncDaily(DailyChallengeManager.todayKey())
    }

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        val t = theme()
        c.drawColor(t.background)
        updateAnimation()

        val offset = shakeX
        text(c, if (dailyMode) "DAILY CHALLENGE" else "ARROW FLOW", 28f + offset, 36f, t.ink, 22f)
        text(c, if (dailyMode) DailyChallengeManager.todayKey() else "Level " + vm.level, 28f + offset, 70f, t.ink, 16f)
        text(c, "Coins " + vm.coins, width - 120f + offset, 45f, t.ink, 16f)

        val n = vm.size()
        cell = min(width - 56f, (height - 245f).coerceAtLeast(260f)) / n
        boardTop = 105f
        val left = (width - cell * n) / 2f + offset

        for (r in 0 until n) for (col in 0 until n) {
            paint.color = t.surface
            c.drawRoundRect(
                left + col * cell + 2, boardTop + r * cell + 2,
                left + (col + 1) * cell - 2, boardTop + (r + 1) * cell - 2,
                12f, 12f, paint
            )
        }
        vm.state().forEach { drawArrow(c, it, left, boardTop, t) }
        drawParticles(c)

        button(c, 24f, height - 110f, 145f, 56f, "Undo", t)
        button(c, 164f, height - 110f, 145f, 56f, "Hint", t)
        button(c, 304f, height - 110f, 145f, 56f, "Reset", t)

        if (complete) {
            paint.color = if (t.name == "Dark") 0xEE14161E.toInt() else 0xEEFFFFFF.toInt()
            c.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            text(c, if (dailyMode) "DAILY COMPLETE!" else "LEVEL COMPLETE!",
                width / 2f - 120f, height / 2f - 35f, t.accent, 26f)
            text(c, "★".repeat(vm.stars()), width / 2f - 35f, height / 2f + 10f,
                Color.rgb(245, 175, 45), 28f)
            if (!dailyMode) button(c, width / 2f - 90f, height / 2f + 55f, 180f, 58f, "Next Level", t)
        }
    }

    private fun updateAnimation() {
        val now = System.currentTimeMillis()
        if (selectedId != null) {
            val elapsed = (now - selectedStart).coerceAtLeast(0L)
            selectedProgress = min(1f, elapsed / 180f)
            if (selectedProgress >= 1f) selectedId = null
            postInvalidateOnAnimation()
        }
        if (shakeUntil > now) {
            val phase = (shakeUntil - now) / 180f
            shakeX = kotlin.math.sin((1f - phase) * 35f) * 8f * phase
            postInvalidateOnAnimation()
        } else shakeX = 0f

        if (particles.isNotEmpty()) {
            val dt = 1f / 60f
            particles.forEach {
                it.x += it.vx * dt
                it.y += it.vy * dt
                it.vy += 180f * dt
                it.life -= dt
            }
            particles = particles.filter { it.life > 0f }.toMutableList()
            postInvalidateOnAnimation()
        }
    }

    private fun drawArrow(c: Canvas, a: ArrowPiece, l: Float, top: Float, t: GameTheme) {
        val x = l + a.col * cell
        val y = top + a.row * cell
        val cx = x + cell / 2
        val cy = y + cell / 2
        var dx = 0f
        var dy = 0f

        if (a.id == selectedId) {
            val p = 1f - selectedProgress
            when (a.direction) {
                Direction.UP -> dy = -cell * .55f * p
                Direction.DOWN -> dy = cell * .55f * p
                Direction.LEFT -> dx = -cell * .55f * p
                Direction.RIGHT -> dx = cell * .55f * p
            }
        }

        paint.color = when {
            a.id == hintId -> Color.rgb(245, 175, 45)
            a.state == ArrowState.BLOCKED -> Color.rgb(160, 166, 180)
            else -> t.accent
        }
        val s = cell * .27f
        val p = Path()
        val px = cx + dx
        val py = cy + dy
        when (a.direction) {
            Direction.UP -> {
                p.moveTo(px, py - s); p.lineTo(px + s, py); p.lineTo(px + s * .38f, py)
                p.lineTo(px + s * .38f, py + s); p.lineTo(px - s * .38f, py + s)
                p.lineTo(px - s * .38f, py); p.lineTo(px - s, py); p.close()
            }
            Direction.DOWN -> {
                p.moveTo(px, py + s); p.lineTo(px + s, py); p.lineTo(px + s * .38f, py)
                p.lineTo(px + s * .38f, py - s); p.lineTo(px - s * .38f, py - s)
                p.lineTo(px - s * .38f, py); p.lineTo(px - s, py); p.close()
            }
            Direction.LEFT -> {
                p.moveTo(px - s, py); p.lineTo(px, py - s); p.lineTo(px, py - s * .38f)
                p.lineTo(px + s, py - s * .38f); p.lineTo(px + s, py + s * .38f)
                p.lineTo(px, py + s * .38f); p.lineTo(px, py + s); p.close()
            }
            Direction.RIGHT -> {
                p.moveTo(px + s, py); p.lineTo(px, py - s); p.lineTo(px, py - s * .38f)
                p.lineTo(px - s, py - s * .38f); p.lineTo(px - s, py + s * .38f)
                p.lineTo(px, py + s * .38f); p.lineTo(px, py + s); p.close()
            }
        }
        c.drawPath(p, paint)
    }

    private fun drawParticles(c: Canvas) {
        particles.forEach {
            paint.color = Color.rgb(245, 175, 45)
            paint.alpha = (255f * it.life.coerceIn(0f, 1f)).toInt()
            c.drawCircle(it.x, it.y, it.radius, paint)
        }
        paint.alpha = 255
    }

    private fun spawnConfetti() {
        val cx = width / 2f
        val cy = height / 2f
        particles.clear()
        repeat(42) {
            val angle = Random.nextFloat() * Math.PI * 2
            val speed = 80f + Random.nextFloat() * 240f
            particles += Particle(
                cx, cy,
                kotlin.math.cos(angle).toFloat() * speed,
                kotlin.math.sin(angle).toFloat() * speed - 80f,
                1.3f, 3f + Random.nextFloat() * 4f
            )
        }
    }

    private fun button(c: Canvas, x: Float, y: Float, w: Float, h: Float, label: String, t: GameTheme) {
        paint.color = t.surface
        c.drawRoundRect(x, y, x + w, y + h, 18f, 18f, paint)
        text(c, label, x + 18, y + 35, t.ink, 15f)
    }

    private fun text(c: Canvas, s: String, x: Float, y: Float, color: Int, size: Float) {
        paint.color = color
        paint.textSize = size
        paint.typeface = Typeface.create("sans", Typeface.BOLD)
        paint.style = Paint.Style.FILL
        c.drawText(s, x, y, paint)
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (e.action != MotionEvent.ACTION_UP) return true
        val x = e.x
        val y = e.y

        if (complete) {
            if (!dailyMode && y > height / 2f + 45f) {
                vm.next()
                complete = false
                particles.clear()
                invalidate()
            }
            return true
        }

        if (y > height - 125f) {
            when {
                x < 154f -> { vm.undo(); hintId = null }
                x < 310f -> hintId = vm.hint()
                else -> { vm.reset(); hintId = null; particles.clear() }
            }
            invalidate()
            return true
        }

        val n = vm.size()
        val left = (width - cell * n) / 2f
        if (y >= boardTop && y < boardTop + cell * n && x >= left && x < left + cell * n) {
            val col = ((x - left) / cell).toInt()
            val row = ((y - boardTop) / cell).toInt()
            val a = vm.state().firstOrNull { it.row == row && it.col == col }
            if (a != null) {
                hintId = null
                if (vm.tap(a.id)) {
                    selectedId = a.id
                    selectedProgress = 0f
                    selectedStart = System.currentTimeMillis()
                    vibrate()
                    if (vm.complete()) {
                        complete = true
                        if (!completionStarted) {
                            completionStarted = true
                            spawnConfetti()
                        }
                        if (dailyMode && !save.dailyCompleted) {
                            save.dailyCompleted = true
                            save.dailyStreak += 1
                            save.coins += 25
                        }
                    }
                } else {
                    shakeUntil = System.currentTimeMillis() + 180L
                    vibrate(25)
                }
                invalidate()
            }
        }
        return true
    }

    private fun vibrate(ms: Long = 12) {
        if (!save.haptics) return
        val v = ctx.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (v?.hasVibrator() == true) {
            v.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }
}
