package com.habits.habits.config;

import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.support.TestPropertySourceUtils
import org.springframework.util.SocketUtils

class RandomPortInitializer : ApplicationContextInitializer<ConfigurableApplicationContext?> {

  override fun initialize(applicationContext: ConfigurableApplicationContext) {
    TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
        applicationContext, "spring.data.mongodb.port=${SocketUtils.findAvailableTcpPort()}"
    )
  }
}