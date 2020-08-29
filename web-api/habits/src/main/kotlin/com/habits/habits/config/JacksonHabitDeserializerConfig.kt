package com.habits.habits.config

import com.habits.habits.Habit
import com.habits.habits.PresentPropertyPolymorphicDeserializer

import org.springframework.boot.jackson.JsonComponent
import org.springframework.context.annotation.Configuration

@Configuration
class JacksonHabitDeserializerConfig {

  @JsonComponent
  class FooBarDeserializer : PresentPropertyPolymorphicDeserializer<Habit>(Habit::class.java)
}