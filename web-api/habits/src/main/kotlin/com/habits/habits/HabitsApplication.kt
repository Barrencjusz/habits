package com.habits.habits

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HabitsApplication

fun main(args: Array<String>) {
  runApplication<HabitsApplication>(*args)
}
