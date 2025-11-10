# 🦫 Weather Groundhog

> A simple and elegant OpenWeather Java SDK for fetching weather data for any city

*Named after the classic film "Groundhog Day" — because checking the weather should be as reliable as reliving the same day!*

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/java-22%2B-orange.svg)](https://www.oracle.com/java/)

## 📋 Table of Contents

- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Usage](#usage)
- [Error Handling](#error-handling)
- [License](#license)
- [Contributing](#contributing)
- [Support](#support)

## ✨ Features

- 🌤️ Fetch real-time weather data for any city
- 🚀 Simple and intuitive API
- 🔧 Easy integration with Gradle and Maven
- 📦 Lightweight with minimal dependencies
- ⚡ Built with OpenWeather API

## 📦 Requirements

- Java 22 or higher
- Valid OpenWeather API key ([Get one here](https://openweathermap.org/api))

## 🚀 Installation

### Option 1: Maven Central (Gradle/Maven)

Add Weather-Groundhog to your project using your preferred build tool:

#### Gradle

```gradle
implementation("top.brahman.dev.weather:weather-groundhog:1.1.0")
```

#### Maven

```xml
<dependency>
    <groupId>top.brahman.dev.weather</groupId>
    <artifactId>weather-groundhog</artifactId>
    <version>1.1.0</version>
</dependency>
```

### Option 2: Download and Use Fat JAR

Download the fat JAR from the [latest release](https://github.com/kBrahman/weather-groundhog/releases):

1. Go to the [Releases page](https://github.com/kBrahman/weather-groundhog/releases)
2. Download `weather-groundhog-1.1.0-all.jar` (or the latest version)
3. Add it to your project's classpath or `libs/` directory

#### Using with Gradle

```gradle
dependencies {
    implementation(files("libs/weather-groundhog-1.1.0-all.jar"))
}
```

#### Using with Maven

```xml
<dependency>
    <groupId>top.brahman.dev.weather</groupId>
    <artifactId>weather-groundhog</artifactId>
    <version>1.1.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/libs/weather-groundhog-1.1.0-all.jar</systemPath>
</dependency>
```

The fat JAR includes all dependencies, so you don't need to manage transitive dependencies separately.

### Option 3: Import as a Git Module

Clone the repository and import it as a submodule or local module in your project:

#### Gradle (settings.gradle.kts)

```kotlin
include(":weather-groundhog")
project(":weather-groundhog").projectDir = File("path/to/weather-groundhog")
```

Then in your `build.gradle.kts`:

```gradle
dependencies {
    implementation(project(":weather-groundhog"))
}
```

#### Maven (pom.xml)

```xml
<module>../weather-groundhog</module>
```

Then reference it in your dependencies:

```xml
<dependency>
    <groupId>top.brahman.dev.weather</groupId>
    <artifactId>weather-groundhog</artifactId>
    <version>1.1.0</version>
</dependency>
```

This approach is useful for local development or if you want to contribute to the project.

## 💻 Usage

```java
try (WeatherClient client = WeatherClient.builder()
        .apiKey("your_api_key")
        .build()) {

    final CityWeather city = client.getWeatherFor("Karabutak");
    System.out.println("JSON: " + city.toJson());

} catch (WeatherClientException we) {
    // Network, I/O related errors
} catch (CityNotFoundException ce) {
    // OpenWeather doesn't have weather data for this city
}
```