package com.habits.habits.repository

import com.habits.habits.Habit
import com.mongodb.client.result.UpdateResult
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface HabitsRepository {

  fun save(habit: Habit): Mono<UpdateResult>
  fun insertOrUpdateHabits(habits: List<Habit>): Flux<Habit>
  fun getAllHabits(): Flux<Habit>
  fun getHabitsHistoryForUser(userId: String): Flux<Map<*, *>>
  fun getHabitsSuccessPercentage(userId: String): Flux<Map<*, *>>
  fun getStreaksForUserHabit(userId: String, habitId: String): Flux<Map<*, *>>
}


