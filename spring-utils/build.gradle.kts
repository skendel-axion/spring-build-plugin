plugins {
    java
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.spring") version "1.7.10"
}

//springBoot = { id = "org.springframework.boot", version = "3.2.0" }
//springDependencyManagement = { id = "io.spring.dependency-management", version = "1.1.4" }
//kotlinJvm = { id = "org.jetbrains.kotlin.jvm", version = "1.9.23" }
//pluginSpring = { id = "org.jetbrains.kotlin.plugin.spring", version = "1.7.10" }
//


group = "hr.axion"
version = "0.1.9"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}


dependencies {
    api("org.springframework.boot:spring-boot-starter")
    compileOnly("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.springframework.boot:spring-boot-starter-validation")
    compileOnly("org.springframework.boot:spring-boot-starter-data-jpa")
    compileOnly("org.springframework.boot:spring-boot-starter-security")
    // used for FieldUtils (reflection)
    api("org.apache.commons:commons-lang3")

    compileOnly("org.springframework.boot:spring-boot-devtools")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    compileOnly(platform("org.springframework.cloud:spring-cloud-dependencies:2023.0.0"))
    compileOnly("org.springframework.cloud:spring-cloud-starter-openfeign")
    compileOnly("io.github.openfeign:feign-okhttp")


    compileOnly(group = "org.zalando", name = "logbook-servlet", version = "3.7.2", classifier = "javax")
    compileOnly("org.zalando:logbook-spring-boot-starter:3.7.2")
    compileOnly("org.zalando:logbook-okhttp:3.7.2")

    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation("org.springframework.boot:spring-boot-starter-validation")
    testImplementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testRuntimeOnly("com.h2database:h2")

}

tasks.withType<Test> {
    useJUnitPlatform()
}