package com.habits.habits.config

import com.habits.habits.SuccessHabit
import com.habits.habits.WeightControlHabit
import org.bson.Document
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.Jsr310Converters
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import java.util.Date

@Configuration
class MongoDBConvertersConfig {

  @Bean
  fun customConversions() = MongoCustomConversions(
      listOf(
          SuccessHabitReadConverter(),
          SuccessHabitWriteConverter(),
          WeightControlHabitReadConverter(),
          WeightControlHabitWriteConverter()
      )
  )

  class SuccessHabitReadConverter : Converter<Document, SuccessHabit> {

    override fun convert(source: Document): SuccessHabit? {
      val id = extractId(source)
      return SuccessHabit(
          userId = id["userId"] as String,
          typeId = id["typeId"] as String,
          date = id["date"].let { Jsr310Converters.DateToLocalDateConverter.INSTANCE.convert(it as Date) },
          time = source["time"]?.let { Jsr310Converters.DateToLocalTimeConverter.INSTANCE.convert(it as Date) },
          success = source["success"] as Boolean
      )
    }
  }

  class SuccessHabitWriteConverter : Converter<SuccessHabit, Document> {

    override fun convert(source: SuccessHabit) = Document().apply {
      put("_id", Document().apply {
        put("userId", source.userId)
        put("typeId", source.typeId)
        put("date", source.date)
      })
      put("time", source.time)
      put("success", source.success)
      put("_class", SuccessHabit::class.qualifiedName)
    }
  }

  class WeightControlHabitReadConverter : Converter<Document, WeightControlHabit> {

    override fun convert(source: Document): WeightControlHabit {
      val id = extractId(source)
      return WeightControlHabit(
          userId = id["userId"] as String,
          typeId = id["typeId"] as String,
          date = id["date"].let { Jsr310Converters.DateToLocalDateConverter.INSTANCE.convert(it as Date) },
          time = source["time"]?.let { Jsr310Converters.DateToLocalTimeConverter.INSTANCE.convert(it as Date) },
          weight = extractCustom(source).single { it["property"] == "weight" }
              .getValue("value") as Double
      )
    }
  }

  class WeightControlHabitWriteConverter : Converter<WeightControlHabit, Document> {

    override fun convert(source: WeightControlHabit) = Document().apply {
      put("_id", Document().apply {
        put("userId", source.userId)
        put("typeId", source.typeId)
        put("date", source.date)
      })
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

  companion object {

    private fun extractId(source: Document) = source["_id"] as Map<String, Any>
    private fun extractCustom(source: Document) = source["custom"] as List<Map<String, Any>>
  }
}