
plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "1.9.23" // March 7, 2024
}

group = "hr.axion"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

// based on https://github.com/rogervinas/gradle-plugins-first-steps
gradlePlugin {
    plugins {
        create("spring-build-plugin") {
            id = "de.logpay.spring-build-plugin"
            implementationClass = "de.logpay.SpringBuildPlugin"
        }
    }
}


