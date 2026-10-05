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

The city is set in `src/weather_app/config.clj` and defaults to Berlin. The CSV file name is derived from the city (`Berlin.csv`). To collect readings for another city, change `city` in that file.

## Run

From the project root:

```sh
clj -M -m weather-app.core
```

The HTTP server listens on port `8080`. The scheduler performs a weather update immediately at startup and then once every hour. Requests to OpenWeatherMap use a 5 second connection timeout and a 10 second socket timeout. When a fetch or save fails, the scheduler prints the error and stays active to try again on its next scheduled run.

## Tests

From the project root:

```sh
clj -M:test
```

The tests use [cognitect test-runner](https://github.com/cognitect-labs/test-runner). They do not call the real OpenWeatherMap API and do not need `WEATHER_API_KEY` to be set. CSV fixtures for the data store tests are in `test/weather_app/data/`.

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

- `config.clj` holds the city and the CSV file path
- `weather.clj` requests and validates the temperature response from the OpenWeatherMap API
- `data_store.clj` reads from and writes temperature updates to a CSV file
- `scheduler.clj` runs the update immediately and schedules hourly updates; it catches and reports fetch or storage errors
- `api.clj` handles HTTP requests
- `core.clj` starts the server and scheduler and registers shutdown cleanup

## Assumptions and current limitations

- The city is set in code (`config.clj`), not at runtime, and defaults to Berlin
- The API key is required before startup
- Rows with a missing timestamp or a temperature that isn't a number are skipped when reading the CSV
- The timestamp reflects the machine's local timezone but does not record the timezone in the CSV or response

## Possible production improvements

- **Configuration:** Load and validate the API key, city, port, and schedule interval from environment variables at startup
- **Retries:** Retry temporary API failures with backoff instead of waiting for the next hourly run
- **Monitoring:** Add structured logs for easier debugging, and connect them to an alerting system
- **Timestamps:** Store timestamps in UTC to avoid ambiguity across time zones
