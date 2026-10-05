package com.kanha.arrowflow

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View

class HomeView(
    context: Context,
    private val onPlay: () -> Unit,
    private val onDaily: () -> Unit
) : View(context) {
    private val save = SaveManager(context)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var showingLevels = false

    private fun theme() = ThemeManager.get(save.selectedTheme)

    override fun onDraw(c: Canvas) {
        val t = theme()
        c.drawColor(t.background)
        if (showingLevels) { drawLevels(c, t); return }

        text(c, "ARROW FLOW", 28f, 58f, t.ink, 30f, true)
        text(c, "Plan the path. Clear the board.", 28f, 88f, t.ink, 15f, false)

        card(c, 24f, 120f, width - 48f, 118f, t.surface)
        text(c, "Continue", 46f, 158f, t.ink, 18f, true)
        text(c, "Level " + save.level, 46f, 190f, t.ink, 28f, true)
        pill(c, width - 132f, 150f, 92f, 44f, "PLAY", t.accent, Color.WHITE)

        card(c, 24f, 258f, width - 48f, 100f, t.surface)
        text(c, "Daily Challenge", 46f, 294f, t.ink, 18f, true)
        text(c, if (save.dailyCompleted) "Completed • Streak " + save.dailyStreak else "Today's puzzle is ready",
            46f, 326f, t.ink, 14f, false)
        pill(c, width - 142f, 286f, 102f, 44f, "DAILY", t.accent, Color.WHITE)

        pill(c, 28f, 375f, 150f, 48f, "LEVELS", t.surface, t.ink)
        text(c, "Campaign", 194f, 407f, t.ink, 14f, false)

        text(c, "Theme", 28f, 455f, t.ink, 15f, true)
        pill(c, 28f, 473f, 150f, 48f, save.selectedTheme, t.surface, t.ink)
        text(c, "Tap theme to switch", 194f, 504f, t.ink, 13f, false)

        text(c, "Coins " + save.coins, 28f, 555f, t.ink, 15f, true)
        text(c, "Offline ready", 28f, 585f, t.ink, 14f, false)
    }

    private fun drawLevels(c: Canvas, t: GameTheme) {
        text(c, "CAMPAIGN", 24f, 52f, t.ink, 28f, true)
        text(c, "Unlocked levels", 24f, 82f, t.ink, 14f, false)
        text(c, "Tap a level to play", 24f, 104f, t.ink, 14f, false)

        val maxLevel = minOf(60, maxOf(1, save.level + 2))
        val columns = 4
        val gap = 12f
        val cardW = (width - 48f - gap * (columns - 1)) / columns
        for (level in 1..maxLevel) {
            val index = level - 1
            val row = index / columns
            val col = index % columns
            val x = 24f + col * (cardW + gap)
            val y = 125f + row * 68f
            val unlocked = level <= save.level
            card(c, x, y, cardW, 56f, t.surface)
            text(c, level.toString(), x + cardW / 2f - 7f, y + 36f,
                if (unlocked) t.accent else Color.rgb(160, 166, 180), 18f, true)
        }
        pill(c, 24f, height - 68f, 100f, 46f, "BACK", t.surface, t.ink)
    }

    private fun card(c: Canvas, x: Float, y: Float, w: Float, h: Float, color: Int) {
        paint.color = color
        paint.style = Paint.Style.FILL
        c.drawRoundRect(x, y, x + w, y + h, 24f, 24f, paint)
    }

    private fun pill(c: Canvas, x: Float, y: Float, w: Float, h: Float, label: String, bg: Int, fg: Int) {
        paint.color = bg
        paint.style = Paint.Style.FILL
        c.drawRoundRect(x, y, x + w, y + h, h / 2, h / 2, paint)
        text(c, label, x + 18, y + h * .66f, fg, 14f, true)
    }

    private fun text(c: Canvas, s: String, x: Float, y: Float, color: Int, size: Float, bold: Boolean) {
        paint.color = color
        paint.textSize = size
        paint.typeface = Typeface.create("sans", if (bold) Typeface.BOLD else Typeface.NORMAL)
        paint.style = Paint.Style.FILL
        c.drawText(s, x, y, paint)
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (e.action != MotionEvent.ACTION_UP) return true

        if (showingLevels) {
            if (e.y > height - 90f) {
                showingLevels = false
                invalidate()
                return true
            }
            val columns = 4
            val gap = 12f
            val cardW = (width - 48f - gap * (columns - 1)) / columns
            val col = ((e.x - 24f) / (cardW + gap)).toInt()
            val row = ((e.y - 125f) / 68f).toInt()
            if (col in 0..3 && row >= 0) {
                val level = row * columns + col + 1
                if (level <= save.level) {
                    save.level = level
                    onPlay()
                }
            }
            return true
        }

        when {
            e.y in 120f..238f -> onPlay()
            e.y in 258f..358f -> onDaily()
            e.y in 365f..435f -> { showingLevels = true; invalidate() }
            e.y in 465f..535f -> {
                save.selectedTheme = ThemeManager.next(save.selectedTheme).name
                invalidate()
            }
        }
        return true
    }
}
