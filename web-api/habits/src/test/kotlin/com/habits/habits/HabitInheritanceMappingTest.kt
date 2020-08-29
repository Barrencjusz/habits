package com.habits.habits

import com.habits.habits.config.MongoDBContainerTestConfiguration
import com.habits.habits.config.RandomPortInitializer
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.InstanceOfAssertFactories
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.insert
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.test.context.ContextConfiguration
import reactor.test.StepVerifier
import java.time.LocalDate
import java.time.LocalTime

@SpringBootTest
@ContextConfiguration(initializers = [RandomPortInitializer::class])
class HabitInheritanceMappingTest(
    @Autowired private val mongoOperations: ReactiveMongoOperations
) {

  @Test
  fun `should insert and find entity with custom converters`() {
    mongoOperations.insertAll(
        listOf(
            SuccessHabit(
                userId = "1",
                typeId = "1",
                date = LocalDate.now(),
                success = true
            ),
            WeightControlHabit(
                userId = "1",
                typeId = "2",
                date = LocalDate.now(),
                time = LocalTime.now(),
                weight = 88.9
            )
        )
    )
        .blockLast()

    val habit = mongoOperations.findAll(Habit::class.java)

    StepVerifier.create(habit)
        .assertNext {
          assertThat(it)
              .asInstanceOf(InstanceOfAssertFactories.type(SuccessHabit::class.java))
              .extracting { it.success }
              .isEqualTo(true)
        }
        .assertNext {
          assertThat(it)
              .asInstanceOf(InstanceOfAssertFactories.type(WeightControlHabit::class.java))
              .extracting { it.weight }
              .isEqualTo(88.9) // FIXME should probably use isCloseTo but the method is not available
        }
        .expectComplete()
        .verify()
  }

  @TestConfiguration
  @Import(MongoDBContainerTestConfiguration::class)
  class Config
}

