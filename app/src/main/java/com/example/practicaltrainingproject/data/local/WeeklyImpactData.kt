package com.example.practicaltrainingproject.data.local

data class WeeklyImpactData(
    val counts: Map<String, Int> = mapOf(
        "Mon" to 0, "Tue" to 0, "Wed" to 0,
        "Thu" to 0, "Fri" to 0, "Sat" to 0, "Sun" to 0
    )
)