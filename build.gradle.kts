import org.gradle.api.JavaVersion.VERSION_1_8

plugins {
    `java-library`
    `maven-publish`
    id("com.jfrog.bintray") version "1.7.3"
}

repositories {
    mavenCentral()
    jcenter()
}

group = "com.jpragma"
version = "0.0.1-SNAPSHOT"

val junitVer = "5.5.2"

java {
    sourceCompatibility = VERSION_1_8
    targetCompatibility = VERSION_1_8
    withSourcesJar()
    withJavadocJar()
}


dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVer")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVer")
    testImplementation("org.mockito:mockito-core:3.0.0")
    testImplementation("org.assertj:assertj-core:3.13.2")
    testImplementation("com.h2database:h2:1.4.200")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("default") {
            from(components["java"])
        }
    }
}