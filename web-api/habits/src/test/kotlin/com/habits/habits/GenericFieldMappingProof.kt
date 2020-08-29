package com.habits.habits

import com.habits.habits.config.MongoDBContainerTestConfiguration
import com.habits.habits.config.RandomPortInitializer
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.convert.converter.Converter
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.test.context.ContextConfiguration
import reactor.test.StepVerifier

@SpringBootTest
@ContextConfiguration(initializers = [RandomPortInitializer::class])
class GenericFieldMappingProof(
    @Autowired private val mongoOperations: ReactiveMongoOperations
) {

  @Test
  fun `should insert and find entity with custom converters`() {
    mongoOperations.insert(Person(name = "Piotr"))
        .block()

    val person = mongoOperations.findOne(
        Query.query(
            Criteria.where("generic")
                .elemMatch(
                    Criteria("property").isEqualTo("name")
                        .and("value")
                        .isEqualTo("Piotr")
                )
        ), Person::class.java
    )

    StepVerifier.create(person)
        .assertNext {
          assertThat(it.name).isEqualTo("Piotr")
        }
        .expectComplete()
        .verify()
  }

  @org.springframework.context.annotation.Configuration
  @Import(MongoDBContainerTestConfiguration::class)
  class Configuration {

    @Bean
    fun customConversions() = MongoCustomConversions(listOf(PersonReadConverter(), PersonWriteConverter()))

    class PersonReadConverter : Converter<Document, Person> {

      override fun convert(source: Document) = Person(
          name = source["generic"].let { it as List<Map<String, String>> }
              .single { it["property"] == "name" }
              .getValue("value")
      )
    }

    class PersonWriteConverter : Converter<Person, Document> {

      override fun convert(source: Person) = Document().apply {
        put(
            "generic", listOf(
            Document().apply {
              put("property", "name")
              put("value", source.name)
            }
        )
        )
      }
    }
  }

  data class Person(
      val name: String
  )
}
