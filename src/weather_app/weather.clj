(ns weather-app.weather
  (:require [clj-http.client :as client]
            [charred.api :as charred]))

(import '[java.time LocalDateTime]
        '[java.time.format DateTimeFormatter])

(def formatter
  (DateTimeFormatter/ofPattern "yyyy-MM-dd'T'HH:mm:ss"))


(def weather-api-t
  "https://api.openweathermap.org/data/2.5/weather?q=%s&units=metric&appid=%s")

(def api-key
  (or (System/getenv "WEATHER_API_KEY")
      (throw (ex-info "Missing WEATHER_API_KEY" {}))))


(defn fetch-temperature [city]
  (let [endpoint (format weather-api-t city api-key)
        response (client/get endpoint)
        data (charred/read-json (:body response) :key-fn keyword)
        temperature (get-in data [:main :temp])
        timestamp (.format (LocalDateTime/now) formatter)]
    (when-not (number? temperature)
      (throw (ex-info "Weather API response has missing or invalid temperature" {:city city :response data})))
    {:timestamp timestamp
     :temperature temperature}))
