//package com.habits.habits.repository.impl
//
//import com.habits.habits.Habit
//import com.habits.habits.repository.HabitsRepository
//import org.slf4j.LoggerFactory
//import reactor.core.publisher.Flux
//import reactor.core.publisher.Mono
//
//class HabitsLoggingRepository : HabitsRepository {
//
//  override fun insertOrUpdateHabit(habit: Habit): Mono<Habit> {
//    LOGGER.debug(habit.toString())
//    TODO("Not yet implemented")
//  }
//
//  override fun getAllHabits(): Flux<Habit> {
//    TODO("Not yet implemented")
//  }
//
//  companion object {
//
//    private val LOGGER = LoggerFactory.getLogger(HabitsLoggingRepository::class.java)
//  }
//}