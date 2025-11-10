plugins {
    `java-library`
    `maven-publish`
    signing
    id("com.gradleup.shadow") version "9.2.2"
    id("org.jreleaser") version "1.21.0"
}

group = "top.brahman.dev.weather"
version = "1.1.0"

java {
    sourceCompatibility = JavaVersion.VERSION_22
    targetCompatibility = JavaVersion.VERSION_22
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = project.name
            from(components["java"])

            pom {
                name.set("Weather Groundhog")
                description.set("OpenWeather SDK")
                url.set("https://github.com/kBrahman/weather-groundhog.git")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("kBrahman")
                        name.set("Kairat Kaibrakhman")
                        email.set("brahman.dev.kz@gmail.com")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/kBrahman/weather-groundhog.git")
                    developerConnection.set("scm:git:ssh://github.com/kBrahman/weather-groundhog.git")
                    url.set("https://github.com/kBrahman/weather-groundhog.git")
                }
            }
        }
    }

    repositories {
        maven {
            name = "staging"
            url = uri("build/staging-deploy")
        }
    }
}

jreleaser {
    signing {
        active.set(org.jreleaser.model.Active.ALWAYS)
        armored.set(true)
        passphrase.set(
            providers.environmentVariable("JRELEASER_GPG_PASSPHRASE")
                .orElse(providers.gradleProperty("signing.password"))
        )
        publicKey.set(
            providers.fileContents(
                rootProject.layout.projectDirectory.file("public.asc")
            ).asText
        )
        secretKey.set(
            providers.environmentVariable("JRELEASER_GPG_SECRET_KEY")
                .orElse(providers.provider { file("secret.asc").takeIf { it.exists() }?.readText() })
        )
    }

    deploy {
        maven {
            mavenCentral {
                create("sonatype") {
                    active.set(org.jreleaser.model.Active.ALWAYS)
                    url.set("https://central.sonatype.com/api/v1/publisher")
                    stagingRepository("build/staging-deploy")
                    username.set(
                        providers.environmentVariable("JRELEASER_MAVENCENTRAL_SONATYPE_USERNAME")
                            .orElse(providers.gradleProperty("ossrhUsername"))
                    )
                    password.set(
                        providers.environmentVariable("JRELEASER_MAVENCENTRAL_SONATYPE_PASSWORD")
                            .orElse(providers.gradleProperty("ossrhPassword"))
                    )
                }
            }
        }
    }
//    release {
//        github {
//            overwrite.set(true)
//            update {
//                enabled.set(true)
//            }
//        }
//    }
//
//    files {
//        artifacts {
//            artifact {
//                path.set(layout.buildDirectory.file("libs/weather-groundhog-*-all.jar"))
//            }
//        }
//    }
}

tasks {
    named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        archiveClassifier.set("all")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
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