package com.habits.habits.config

import com.habits.habits.WeightControlHabit
import org.bson.Document
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.Jsr310Converters
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import java.time.LocalTime
import java.util.Date

@Configuration
class MongoDBConvertersConfig {

  @Bean
  fun customConversions() = MongoCustomConversions(
      listOf(
          WeightControlHabitReadConverter(),
          WeightControlHabitWriteConverter()
      )
  )

  class WeightControlHabitReadConverter : Converter<Document, WeightControlHabit> {

    override fun convert(source: Document) = WeightControlHabit(
        userId = source["userId"] as String,
        typeId = source["typeId"] as String,
        date = source["date"].let { Jsr310Converters.DateToLocalDateConverter.INSTANCE.convert(it as Date) },
        time = source["time"].let { Jsr310Converters.DateToLocalTimeConverter.INSTANCE.convert(it as Date) },
        weight = source["custom"].let { it as List<Map<String, Any>> }
            .single { it["property"] == "weight" }
            .getValue("value") as Double
    )
  }

  class WeightControlHabitWriteConverter : Converter<WeightControlHabit, Document> {

    override fun convert(source: WeightControlHabit) = Document().apply {
      put("userId", source.userId)
      put("typeId", source.typeId)
      put("date", source.date)
      put("time", source.time)
      put(
          "custom", listOf(
          Document().apply {
            put("property", "weight")
            put("value", source.weight)
          }
      )
      )
      put("_class", WeightControlHabit::class.qualifiedName)
    }
  }
}