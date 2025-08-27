package com.example.practicaltrainingproject.data.local

data class HourlyImpactData(
    val counts: Map<String, Int> = (0..23).associate { it.toString() to 0 }
)

