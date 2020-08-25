package com.habits.habits

import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.support.TestPropertySourceUtils
import org.springframework.util.SocketUtils
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.wait.strategy.Wait
import reactor.test.StepVerifier

@SpringBootTest
@ContextConfiguration(initializers = [com.habits.habits.Configuration.RandomPortInitializer::class])
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

  @Bean
  fun mongoDBContainer(mongoProperties: MongoProperties) =
      object : MongoDBContainer("mongo:4.4.0") {

        fun withFixedExposedPort(hostPort: Int, containerPort: Int): MongoDBContainer {
          addFixedExposedPort(hostPort, containerPort)
          return this
        }
      }.withFixedExposedPort(mongoProperties.port, 27017)
          .waitingFor(
              Wait.forLogMessage(
                  ".*Waiting for connections.*",
                  1
              )
          )
          .also { it.start() }

  class RandomPortInitializer : ApplicationContextInitializer<ConfigurableApplicationContext?> {

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
      TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
          applicationContext, "spring.data.mongodb.port=${SocketUtils.findAvailableTcpPort()}"
      )
    }
  }
}