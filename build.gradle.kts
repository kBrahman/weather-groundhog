plugins {
    `java-library`
    `maven-publish`
}

group = "top.brahman.grndhog.weather"
version = "1.0.0"

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = project.name  // "weather-groundhog"
            from(components["java"])
        }
    }
}
java {
    withSourcesJar()
    withJavadocJar()
}
repositories { mavenCentral() }

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
    //Debug
    implementation("com.squareup.okhttp3:logging-interceptor:5.3.0")
}

tasks.test {
    useJUnitPlatform()  // Enables JUnit 5 (Jupiter)
    testLogging {
        showExceptions = true
        showStandardStreams = true
        events("passed", "skipped", "failed")
    }
}