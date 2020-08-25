package com.habits.habits

data class Habit(
    val userId: String,
    val type: String,
    val day: String,
    val submitTime: Long,
    val weight: Double? = null,
    val success: Boolean? = null
)