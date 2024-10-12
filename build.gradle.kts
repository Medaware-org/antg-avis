import org.jetbrains.kotlin.cli.jvm.main

plugins {
    kotlin("jvm") version "2.0.0"
    `maven-publish`
}

group = "org.medaware"
version = "2.0.0"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("org.medaware:anterogradia:+")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

sourceSets {
    main {
        resources {
            kotlin.srcDirs("$rootDir/src/main/resources")
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven-publication") {
            from(components["kotlin"])
            artifactId = "anterogradia-avis"
        }
    }
    repositories {
        mavenLocal()
    }
}

kotlin {
    jvmToolchain(17)
}