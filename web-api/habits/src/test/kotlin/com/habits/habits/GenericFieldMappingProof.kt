package com.habits.habits

import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import reactor.test.StepVerifier

@SpringBootTest
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
}

data class Person(
    val name: String
)

@Configuration
internal class Configuration {

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