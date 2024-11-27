plugins {
  kotlin("jvm") version "2.0.0"
  id("com.diffplug.spotless") version "6.25.0"
}

group = "com.hopskipnfall"

version = "1.0-SNAPSHOT"

repositories { mavenCentral() }

dependencies {
  val floggerVersion = "0.8"
  api("com.google.flogger:flogger:$floggerVersion")
  api("com.google.flogger:flogger-system-backend:$floggerVersion")
  api("com.google.flogger:flogger-log4j2-backend:$floggerVersion")

  val log4j = "2.23.1"
  api("org.apache.logging.log4j:log4j:$log4j")
  api("org.apache.logging.log4j:log4j-core:$log4j")
  api("org.apache.logging.log4j:log4j-api:$log4j")

  testImplementation("junit:junit:4.13.2")
  testImplementation("com.google.truth:truth:1.4.4")
  testImplementation(kotlin("test"))
  testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
}

tasks.test { useJUnitPlatform() }

// Formatting/linting.
spotless {
  kotlin {
    target("**/*.kt", "**/*.kts")
    targetExclude("build/", ".git/", ".idea/", ".mvn", "src/main/java-templates/")
    ktfmt().googleStyle()
  }

  yaml {
    target("**/*.yml", "**/*.yaml")
    targetExclude("build/", ".git/", ".idea/", ".mvn")
    jackson()
  }
}