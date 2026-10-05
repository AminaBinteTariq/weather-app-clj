# Scheduled Weather Collector

A small Clojure service that fetches Berlin's current temperature from OpenWeatherMap, appends readings to a CSV file, and exposes the stored readings through an HTTP API.

## Requirements

- Java 11 or newer
- [Clojure CLI tools](https://clojure.org/guides/install_clojure)
- An OpenWeatherMap API key

## Configuration

Set the API key in your shell before starting the application:

```sh
export WEATHER_API_KEY="your-openweathermap-api-key"
```

The key is checked when the application starts. If it is missing, the application fails during startup before the server begins listening.

## Run

From the project root:

```sh
clj -M -m weather-app.core
```

The HTTP server listens on port `8080`. The scheduler performs a weather update immediately at startup and then once every hour. When a fetch or save fails, the scheduler prints the error and stays active to try again on its next scheduled run.

## HTTP API

Get all stored readings from the CSV file:

```sh
curl http://localhost:8080/temperatures
```

Example response:

```json
[
  {
    "timestamp": "2026-10-04T18:01:08",
    "temperature": 19.32
  }
]
```

The endpoint returns `[]` when no readings are stored. The root endpoint returns a welcome message:

```sh
curl http://localhost:8080/
```

Unknown routes return `404 Not found`.

## CSV storage

Readings are stored in `Berlin.csv` in the process's current working directory. The CSV file is the source of truth for the API. Its header is `timestamp,temperature`; each successful update appends a row. The header is written when the file is first created or is empty, and is not written again when appending.

Timestamps use the machine's local time in `yyyy-MM-dd'T'HH:mm:ss` format and do not include a timezone offset. Temperatures are in Celsius because the OpenWeatherMap request uses metric units.

## Project structure

- `weather.clj` requests and validates the temperature response from the OpenWeatherMap API
- `data_store.clj` reads from and writes temperature updates to a CSV file
- `scheduler.clj` runs the update immediately and schedules hourly updates; it catches and reports fetch or storage errors
- `api.clj` handles HTTP requests
- `core.clj` starts the server and scheduler and registers shutdown cleanup

## Assumptions and current limitations

- The city is fixed to Berlin in the application
- The API key is required before startup
- The timestamp reflects the machine's local timezone but does not record the timezone in the CSV or response

## Possible production improvements

- **Configuration:** Load and validate the API key, city, port, and schedule interval from environment variables at startup
- **Retries and timeouts:** Set HTTP request timeouts and use backoff for temporary API failures
- **Monitoring:** Add structured logs for easier debugging, and connect them to an alerting system
- **Timestamps:** Store timestamps in UTC to avoid ambiguity across time zones
