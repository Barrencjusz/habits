package com.habits.habits.web

import com.habits.habits.Habit
import com.habits.habits.repository.HabitsRepository
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin
class HabitsController(
    private val habitsRepository: HabitsRepository
) {

  private val habitDefinitions = mapOf(
      "1" to "alcohol",
      "2" to "weight",
      "3" to "familyTime",
      "4" to "noNut",
      "5" to "scratching",
      "6" to "coldShower",
      "7" to "swear",
      "8" to "noGaming"
  )

  @PutMapping
  fun putHabit(@RequestBody habit: Habit) = habitsRepository.save(habit)
      .then()

  @PostMapping("batch")
  fun postHabits(@RequestBody habits: List<Habit>) = habitsRepository.insertOrUpdateHabits(habits)

  @DeleteMapping
  fun deleteHabit(@RequestBody habit: Habit) = habitsRepository.delete(habit)

  @GetMapping
  fun getAllHabits() = habitsRepository.getAllHabits()

  @GetMapping("grouped")
  fun getGroupedHabits() = habitsRepository.getAllHabits()
      .groupBy { it.typeId }
      .flatMap { groupedHabits ->
        groupedHabits.collectList()
            .map { groupedHabits.key() to it }
      }
      .collectMap({ habitDefinitions[it.first] }, { it.second })

  @GetMapping("history")
  fun getHabitsHistoryForUser() = habitsRepository.getHabitsHistoryForUser("1")
      .collectList()
      .map { habits ->
        habits.map {
          it.toMutableMap()
              .apply { replace("type", habitDefinitions[it["type"]]) }
        }
      }

  @GetMapping("success")
  fun getHabitsSuccessPercentage() = habitsRepository.getHabitsSuccessPercentage("1")
      .collectList()
      .map { habits ->
        habits.groupBy(
            { (it["_id"] as Map<String, Any>)["type"] as String },
            { Pair((it["_id"] as Map<String, Any>)["success"] as Boolean, it["count"] as Int) }
        )
            .mapValues { it.value.associateBy({ it.first }, { it.second }) }
            .entries.associateBy({ habitDefinitions[it.key] }, {
              it.value.getOrDefault(true, 0)
                  .toDouble() / (it.value.getOrDefault(false, 0) + it.value.getOrDefault(true, 0)
                  .toDouble()) * 100
            })

      }

  @GetMapping("streak/{habitId}") // FIXME change 2020-08-23 alcohol to failed
  fun getHabitStreaks(@PathVariable habitId: String) = habitsRepository.getStreaksForUserHabit("1", habitId)
}