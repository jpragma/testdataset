import org.gradle.api.JavaVersion.VERSION_1_8

plugins {
    `java-library`
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.1"
    id("pl.allegro.tech.build.axion-release") version "1.10.3"
}

repositories {
    mavenCentral()
    jcenter()
}

group = "com.jpragma"
val artifactId = "testdataset"
version = scmVersion.version

val junitVer = "5.6.0"

java {
    sourceCompatibility = VERSION_1_8
    targetCompatibility = VERSION_1_8
    withSourcesJar()
    withJavadocJar()
}


dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVer")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVer")
    testImplementation("org.mockito:mockito-core:3.2.4")
    testImplementation("org.assertj:assertj-core:3.15.0")
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
                    appendNode("name", "TestDataSet")
                    appendNode("description", "Library to load test data to DB")
                    appendNode("url", "https://github.com/jpragma/testdataset")
                    appendNode("licenses").appendNode("license").apply {
                        appendNode("name", "The Apache Software License, Version 2.0")
                        appendNode("url", "http://www.apache.org/licenses/LICENSE-2.0.txt")
                        appendNode("distribution", "repo")
                    }
                    appendNode("developers").appendNode("developer").apply {
                        appendNode("name", "Isaac Levin")
                        appendNode("email", "ilevin@jpragma.com")
                        appendNode("organization", "JPragma")
                        appendNode("organizationUrl", "http://blog.jpragma.com/")
                    }
                    appendNode("scm").apply {
                        appendNode("connection", "scm:git:git://github.com/jpragma/testdataset.git")
                        appendNode("developerConnection", "scm:git:ssh://github.com/jpragma/testdataset.git")
                        appendNode("url", "https://github.com/jpragma/testdataset")
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