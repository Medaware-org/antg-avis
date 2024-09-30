plugins {
    kotlin("jvm") version "2.0.0"
    `maven-publish`
}

group = "org.medaware"
version = "1.1.1"

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