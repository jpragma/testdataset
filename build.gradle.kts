import org.gradle.api.JavaVersion.VERSION_1_8

plugins {
    `java-library`
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.1"
}

repositories {
    mavenCentral()
    jcenter()
}

group = "com.jpragma"
val artifactId = "testdataset"
version = "0.0.1"

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
            groupId = project.group as String
            artifactId = "testdataset"
            version = project.version as String
            from(components["java"])
            pom.withXml {
                asNode().apply {
                    appendNode("description", "Library to load test data to DB")
                    appendNode("licenses").appendNode("license").apply {
                        appendNode("name", "The Apache Software License, Version 2.0")
                        appendNode("url", "http://www.apache.org/licenses/LICENSE-2.0.txt")
                        appendNode("distribution", "repo")
                    }
                }
            }
        }
    }
}

fun findProperty(s: String) = project.findProperty(s) as String?

bintray {
    user = findProperty("bintrayUser")
    key = findProperty("bintrayApiKey")
    publish = true
    setPublications("default")
    pkg(delegateClosureOf<com.jfrog.bintray.gradle.BintrayExtension.PackageConfig>{
        repo = "jprmvn"
        name = "testdataset"
        userOrg = "jpragma"
        vcsUrl = "https://github.com/jpragma/testdataset"
        setLabels("java")
        setLicenses("Apache-2.0")
    })
}