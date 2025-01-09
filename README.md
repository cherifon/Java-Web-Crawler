# Web Crawler

This project implements a simple web crawler that automatically traverses the web by downloading pages and following links. The crawler extracts relevant data from web pages and stores it in a structured format.

## Project Structure

- **src/main/java/com/example/WebCrawler.java**: Main class that initializes the crawling process, manages the URL queue, and handles data extraction.
- **src/main/java/com/example/Config.java**: Responsible for loading configuration settings from the `application.properties` file.
- **src/main/resources/application.properties**: Configuration settings for the application, including crawler settings and logging details.
- **pom.xml**: Maven configuration file specifying project dependencies and build settings.

## Features

- Starts from a given URL and extracts page data.
- Follows clickable links to traverse the web.
- Respects `robots.txt` rules to avoid restricted pages.
- Prevents revisiting the same URL.
- Saves the state of the URL queue and visited URLs for resumption.
- Uses multithreading to speed up crawling.

## Prerequisites

- Java 8 or higher
- Maven

## Getting Started

### Clone the Repository

```sh
git clone https://github.com/yourusername/web-crawler.git
cd web-crawler
```

### Build the Project

```sh
mvn clean package
```

### Run the Application

```sh
mvn exec:java
```

## Configuration

You can customize the behavior of the web crawler by modifying the `application.properties` file. The available settings include:

- `crawler.maxDepth`: Maximum depth of links to follow.
- `crawler.userAgent`: User agent string to use for HTTP requests.
- `crawler.delayBetweenRequests`: Delay between requests in milliseconds.
- `logging.level` : Log level for the application (e.g., INFO, DEBUG).
- `logging.file` : Log file path.
- `crawled.urls.output.file` : Output file path for storing crawled URLs.

## Logging

The crawler logs its activity to a file specified in the `application.properties` file. By default, it logs to `logs/crawler.log`.

## Resuming from Saved State

The crawler saves the state of the URL queue and visited URLs to disk, allowing it to resume from where it left off after being shut down. The state is saved to the following files:

- `output/queue_state.txt`
- `output/visited_urls_state.txt`


