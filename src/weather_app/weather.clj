(ns weather-app.weather
  (:require [clj-http.client :as client]
            [charred.api :as charred])
  (:import [java.time LocalDateTime]
           [java.time.format DateTimeFormatter]))

(def formatter
  (DateTimeFormatter/ofPattern "yyyy-MM-dd'T'HH:mm:ss"))


(def weather-api-url
  "https://api.openweathermap.org/data/2.5/weather")

(defn api-key []
  (or (System/getenv "WEATHER_API_KEY")
      (throw (ex-info "Missing WEATHER_API_KEY" {}))))


(defn fetch-temperature [city]
  (let [response (client/get weather-api-url
                             {:query-params {"q" city
                                             "units" "metric"
                                             "appid" (api-key)}
                              ;; timeouts in milliseconds
                              :connection-timeout 5000
                              :socket-timeout 10000})
        data (charred/read-json (:body response) :key-fn keyword)
        temperature (get-in data [:main :temp])
        timestamp (.format (LocalDateTime/now) formatter)]
    (when-not (number? temperature)
      (throw (ex-info "Weather API response has missing or invalid temperature" {:city city :response data})))
    {:timestamp timestamp
     :temperature temperature}))
