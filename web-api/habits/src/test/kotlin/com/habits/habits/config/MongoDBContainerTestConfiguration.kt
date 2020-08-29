package com.habits.habits.config

import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.wait.strategy.Wait

@TestConfiguration
class MongoDBContainerTestConfiguration {

  @Bean
  fun mongoDBContainer(mongoProperties: MongoProperties): MongoDBContainer =
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
}