package com.habits.habits

import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate
import java.time.LocalTime

@Document("habit")
interface Habit {
  val userId: String
  val typeId: String
  val date: LocalDate
  val time: LocalTime?
}

data class SuccessHabit(
    override val userId: String,
    override val typeId: String,
    override val date: LocalDate,
    override val time: LocalTime? = null,
    val success: Boolean
) : Habit

data class WeightControlHabit(
    override val userId: String,
    override val typeId: String,
    override val date: LocalDate,
    override val time: LocalTime?,
    val weight: Double
) : Habit