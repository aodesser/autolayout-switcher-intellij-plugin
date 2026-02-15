plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm") version "2.1.10"
  id("org.jetbrains.intellij.platform") version "2.2.1"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

repositories {
  mavenCentral()
  intellijPlatform {
    defaultRepositories()
  }
}

dependencies {
  intellijPlatform {
    create(providers.gradleProperty("platformType"), providers.gradleProperty("platformVersion"))
    testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
  }

  testImplementation("junit:junit:4.13.2")
  testImplementation("io.mockk:mockk:1.13.13")
}
kotlin {
  // Use locally installed JDK 25 for compilation, but emit JVM 21 bytecode for IntelliJ 2025.1.
  jvmToolchain(25)
}

tasks.withType<JavaCompile>().configureEach {
  options.release.set(21)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
  compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
}

intellijPlatform {
  pluginConfiguration {
    name = providers.gradleProperty("pluginName")
    ideaVersion {
      sinceBuild = "251"
      untilBuild = provider { null }
    }
  }
}
