# 🦫 Weather Groundhog

> A simple and elegant OpenWeather Java SDK for fetching weather data for any city

*Named after the classic film "Groundhog Day" — because checking the weather should be as reliable as reliving the same day!*

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/java-16%2B-orange.svg)](https://www.oracle.com/java/)

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

- Java 16 or higher
- Valid OpenWeather API key ([Get one here](https://openweathermap.org/api))

## 🚀 Installation

### Option 1: Maven Central (Gradle/Maven)

Add Weather-Groundhog to your project using your preferred build tool:

#### Gradle

```gradle
implementation("top.brahman.dev.weather:weather-groundhog:1.0.0")
```

#### Maven

```xml
<dependency>
    <groupId>top.brahman.dev.weather</groupId>
    <artifactId>weather-groundhog</artifactId>
    <version>1.0.0</version>
</dependency>
```

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

