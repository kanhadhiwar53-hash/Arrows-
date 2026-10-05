package com.kanha.arrowflow

import android.content.Context

class SaveManager(context: Context) {
    private val p = context.getSharedPreferences("arrow_flow", Context.MODE_PRIVATE)

    var level: Int
        get() = p.getInt("level", 1)
        set(v) { p.edit().putInt("level", v).apply() }

    var coins: Int
        get() = p.getInt("coins", 0)
        set(v) { p.edit().putInt("coins", v).apply() }

    var selectedTheme: String
        get() = p.getString("theme", "Classic") ?: "Classic"
        set(v) { p.edit().putString("theme", v).apply() }

    var sound: Boolean
        get() = p.getBoolean("sound", true)
        set(v) { p.edit().putBoolean("sound", v).apply() }

    var haptics: Boolean
        get() = p.getBoolean("haptics", true)
        set(v) { p.edit().putBoolean("haptics", v).apply() }

    var dailyDate: String
        get() = p.getString("daily_date", "") ?: ""
        set(v) { p.edit().putString("daily_date", v).apply() }

    var dailyCompleted: Boolean
        get() = p.getBoolean("daily_completed", false)
        set(v) { p.edit().putBoolean("daily_completed", v).apply() }

    var dailyStreak: Int
        get() = p.getInt("daily_streak", 0)
        set(v) { p.edit().putInt("daily_streak", v).apply() }

    var totalCompleted: Int
        get() = p.getInt("total_completed", 0)
        set(v) { p.edit().putInt("total_completed", v).apply() }

    var bestStars: Int
        get() = p.getInt("best_stars", 0)
        set(v) { p.edit().putInt("best_stars", v).apply() }

    var achievements: String
        get() = p.getString("achievements", "") ?: ""
        set(v) { p.edit().putString("achievements", v).apply() }

    fun hasAchievement(id: String): Boolean =
        achievements.split(",").any { it == id }

    fun unlockAchievement(id: String) {
        if (!hasAchievement(id)) {
            achievements = listOf(achievements, id).filter { it.isNotBlank() }.joinToString(",")
        }
    }

    fun syncDaily(date: String) {
        if (dailyDate != date) {
            dailyDate = date
            dailyCompleted = false
        }
    }
}
