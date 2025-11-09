plugins {
    `java-library`
    `maven-publish`
    signing
    id("com.gradleup.shadow") version "9.2.2"
}

group = "top.brahman.dev.weather"
version = "1.0.0"
tasks {
    named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        archiveClassifier.set("all")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        // optional: relocate packages or merge service files if needed
//        mergeServiceFiles()
    }

    // Make the build task depend on shadowJar if you want the fat JAR produced automatically
//    build {
//        dependsOn(named("shadowJar"))
//    }
}
publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = project.name
            from(components["java"])
            pom {
                name.set("Weather Groundhog")
                description.set("OpenWeather SDK")
                url.set("https://github.com/yourusername/yourrepo")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("yourid")
                        name.set("Kairat Kaibrakhman")
                        email.set("brahman.dev.kz@gmail.com")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/yourusername/yourrepo.git")
                    developerConnection.set("scm:git:ssh://github.com/yourusername/yourrepo.git")
                    url.set("https://github.com/yourusername/yourrepo")
                }
            }
        }
    }
    repositories {
        maven {
            name = "OSSRH"
            val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl

            credentials {
                username = project.findProperty("ossrhUsername") as String? ?: System.getenv("OSSRH_USERNAME")
                password = project.findProperty("ossrhPassword") as String? ?: System.getenv("OSSRH_PASSWORD")
            }
        }
    }

}
signing {
    sign(publishing.publications["maven"])
}
java {
    withSourcesJar()
    withJavadocJar()
}
repositories {
    mavenCentral()
}

dependencies {
    //test
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockito:mockito-junit-jupiter:5.20.0")
    testImplementation("org.awaitility:awaitility:4.3.0")
    //api
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    //JSON
    implementation("com.fasterxml.jackson.core:jackson-databind:2.20.1")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")
}

tasks.test {
    useJUnitPlatform()  // Enables JUnit 5 (Jupiter)
    testLogging {
        showExceptions = true
        showStandardStreams = true
        events("passed", "skipped", "failed")
    }
}