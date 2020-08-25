package com.habits.habits.repository

import com.habits.habits.Habit
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface HabitsRepository {

  fun insertOrUpdateHabit(habit: Habit): Mono<Habit>
  fun insertOrUpdateHabits(habits: List<Habit>): Flux<Habit>
  fun getAllHabits(): Flux<Habit>
  fun getHabitsHistoryForUser(userId: String): Flux<Map<*, *>>
  fun getHabitsSuccessPercentage(userId: String): Flux<Map<*, *>>
  fun getStreaksForUserHabit(userId: String, habitId: String): Flux<Map<*, *>>
}


