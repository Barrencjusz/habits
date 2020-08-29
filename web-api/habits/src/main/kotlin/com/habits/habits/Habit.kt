package com.habits.habits

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import org.springframework.data.mongodb.core.mapping.Document
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime
import kotlin.reflect.KClass

@JsonSubTypes(
    JsonSubTypes.Type(SuccessHabit::class, name = "success"),
    JsonSubTypes.Type(WeightControlHabit::class, name = "weight")
)
@Document("habit")
interface Habit {
  val userId: String
  val typeId: String
  val date: LocalDate
  val time: LocalTime?

  fun toId() = org.bson.Document()
      .apply {
        put("userId", userId)
        put("typeId", typeId)
        put("date", date)
      }

  fun toDocument(): org.bson.Document
}

data class SuccessHabit(
    override val userId: String,
    override val typeId: String,
    override val date: LocalDate,
    override val time: LocalTime? = null,
    val success: Boolean
) : Habit {

  override fun toDocument() = org.bson.Document()
      .apply {
        put("time", time)
        put("success", success)
      }
}

data class WeightControlHabit(
    override val userId: String,
    override val typeId: String,
    override val date: LocalDate,
    override val time: LocalTime?,
    val weight: Double
) : Habit {

  override fun toDocument() = org.bson.Document()
      .apply {
        put("time", time)
        put("custom", org.bson.Document("weight", weight))
      }
}

open class PresentPropertyPolymorphicDeserializer<T>(vc: Class<T>) : StdDeserializer<T>(vc) {
  private val propertyNameToType: Map<String, KClass<*>> = vc.getAnnotation(JsonSubTypes::class.java)
      .value
      .associateBy({ it.name }, { it.value })

  @Throws(IOException::class)
  override fun deserialize(p: JsonParser, ctxt: DeserializationContext): T {
    val objectMapper: ObjectMapper = p.codec as ObjectMapper
    val `object`: ObjectNode = objectMapper.readTree(p)
    for (propertyName in propertyNameToType.keys) {
      if (`object`.has(propertyName)) {
        return deserialize(objectMapper, propertyName, `object`)
      }
    }
    throw IllegalArgumentException("could not infer to which class to deserialize $`object`")
  }

  @Throws(IOException::class)
  private fun deserialize(
      objectMapper: ObjectMapper,
      propertyName: String,
      `object`: ObjectNode
  ): T {
    return objectMapper.treeToValue(`object`, propertyNameToType.getValue(propertyName).java) as T
  }
}