package com.example.practicaltrainingproject.presentation.ui.dashboardScreen.components.utils

/**
 * Calculates comprehensive safety score based on multiple factors
 * Considers fall history, monitoring consistency, and recent activity
 */

class SafetyScoreCalculator {

    companion object {
        fun calculateSafetyScore(
            fallCount: Int,
            isMonitoring: Boolean,
            activeDays: Int,
            recentActivity: Boolean
        ): Int {
            var score = 100

            // Deduct points for falls (10 points per fall, max 50 points deduction)
            val fallDeduction = (fallCount * 10).coerceAtMost(50)
            score -= fallDeduction

            // Bonus for active monitoring
            if (isMonitoring) {
                score += 5
            }

            // Bonus for consistent usage (active days)
            val usageBonus = (activeDays * 2).coerceAtMost(20)
            score += usageBonus

            // Small bonus for recent activity (shows system is working)
            if (recentActivity) {
                score += 3
            }

            return score.coerceIn(0, 100)
        }

        fun getSafetyLevel(score: Int): String {
            return when {
                score >= 90 -> "Excellent"
                score >= 75 -> "Good"
                score >= 60 -> "Fair"
                score >= 40 -> "Needs Attention"
                else -> "Critical"
            }
        }
    }
}