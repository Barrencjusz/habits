package com.habits.habits.repository.impl

import com.habits.habits.Habit
import com.habits.habits.repository.HabitsRepository
import com.mongodb.BasicDBObject
import org.bson.Document
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.aggregation.Aggregation.facet
import org.springframework.data.mongodb.core.aggregation.Aggregation.group
import org.springframework.data.mongodb.core.aggregation.Aggregation.limit
import org.springframework.data.mongodb.core.aggregation.Aggregation.match
import org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation
import org.springframework.data.mongodb.core.aggregation.Aggregation.project
import org.springframework.data.mongodb.core.aggregation.Aggregation.sort
import org.springframework.data.mongodb.core.aggregation.Aggregation.unwind
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators
import org.springframework.data.mongodb.core.aggregation.ConvertOperators
import org.springframework.data.mongodb.core.aggregation.ExposedFields
import org.springframework.data.mongodb.core.aggregation.Fields
import org.springframework.data.mongodb.core.aggregation.FieldsExposingAggregationOperation
import org.springframework.data.mongodb.core.findAll
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
class MongoDBHabitsRepository(
    private val mongoOperations: ReactiveMongoOperations
) : HabitsRepository {

  override fun save(habit: Habit) = with(habit.toId()) {
    mongoOperations.upsert(
        query(
            Criteria("_id").isEqualTo(this)
        ),
        Update.fromDocument(habit.toDocument())
            .setOnInsert("_id", this)
            .setOnInsert("_class", habit::class.qualifiedName),
        "habit"
    )
  }

  override fun insertOrUpdateHabits(habits: List<Habit>) = mongoOperations.insertAll(habits)

  override fun getAllHabits() = mongoOperations.findAll<Habit>()

  override fun getHabitsHistoryForUser(userId: String): Flux<Map<*, *>> {
    val aggregation = newAggregation(
        match(Criteria("_id.userId").`is`(userId)),
        sort(Sort.Direction.DESC, "_id.date"),
        group("_id.typeId").push(
            BasicDBObject()
                .append("date", "\$_id.date")
                .append("success", "\$success")
        )
            .`as`("dates"),
        project()
            .andExclude("_id")
            .and("_id")
            .`as`("type")
            .andInclude("dates")
    )

    return mongoOperations.aggregate(aggregation, "habit", Map::class.java)
  }

  override fun getHabitsSuccessPercentage(userId: String): Flux<Map<*, *>> {
    val aggregation = newAggregation(
        match(
            Criteria("userId").isEqualTo(userId)
                .and("success")
                .exists(true)
        ),
        group("type", "success").count()
            .`as`("count")
        //        project()
        //            .andExclude("_id")
        //            .and("_id")
        //            .`as`("type")
        //            .andInclude("days")
    )

    return mongoOperations.aggregate(aggregation, "habit", Map::class.java)
  }

  override fun getStreaksForUserHabit(userId: String, habitId: String) = mongoOperations.aggregate(
      newAggregation(
          match(
              Criteria().and("userId")
                  .isEqualTo(userId)
                  .and("type")
                  .isEqualTo(habitId)
                  .and("success")
                  .isEqualTo(true)
          ),
          group("day"),
          sort(Sort.Direction.DESC, "_id"),
          SingleGroupOfDays(),
          unwind("day", "rownum"),
          project().andExclude("_id")
              .andInclude("\$day")
              .and(
                  ArithmeticOperators.Add.valueOf(ConvertOperators.ToDate.toDate("\$day"))
                      .add(
                          ArithmeticOperators.Multiply.valueOf(24 * 60 * 60 * 1000)
                              .multiplyBy("rownum")
                      )
              )
              .`as`("dayMinusRowNumber"),
          group("dayMinusRowNumber").count()
              .`as`("days")
              .min("day")
              .`as`("start")
              .max("day")
              .`as`("end"),
          project().andExclude("_id"),
          facet().and(
              sort(Sort.Direction.DESC, "end"),
              limit(1)
          )
              .`as`("lastStreak")
              .and(
                  sort(Sort.Direction.DESC, "days"),
                  limit(1)
              )
              .`as`("longestStreak")
      ),
      "habit", Map::class.java
  )

  class SingleGroupOfDays : FieldsExposingAggregationOperation {

    private val document = Document.parse("{ \$group: { _id: 1, day: { \$push: '\$_id' } } }")

    override fun toDocument(aggregationOperationContext: AggregationOperationContext): Document {
      return aggregationOperationContext.getMappedObject(document)
    }

    override fun getFields() = ExposedFields.synthetic(Fields.fields("day"))
  }
}