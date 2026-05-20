plugins {
    id("io.micronaut.application") version "4.5.4"
    id("com.gradleup.shadow") version "8.3.7"
}

version = "0.1.0-SNAPSHOT"
group = "com.couragegang.bff"

repositories {
    maven { url = uri("https://maven.aliyun.com/repository/public") }
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

micronaut {
    version("4.7.6")
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
        module("com.couragegang.bff")
    }
}

dependencies {
    annotationProcessor("io.micronaut.serde:micronaut-serde-processor")

    implementation("io.micronaut:micronaut-http-server-netty")
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut:micronaut-management")
    implementation("io.micronaut.micrometer:micronaut-micrometer-registry-prometheus")
    implementation("io.micronaut.serde:micronaut-serde-jackson")
    implementation("com.fasterxml.jackson.core:jackson-databind")

    runtimeOnly("org.yaml:snakeyaml")
    runtimeOnly("ch.qos.logback:logback-classic")

    testImplementation("io.micronaut.test:micronaut-test-junit5")
    testImplementation("org.mockito:mockito-core:5.14.2")
    testImplementation("org.mockito:mockito-junit-jupiter:5.14.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

application {
    mainClass.set("com.couragegang.bff.Application")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
