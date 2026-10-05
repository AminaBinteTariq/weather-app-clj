(ns weather-app.scheduler
  (:require [weather-app.weather :as weather]
            [weather-app.data-store :as data-store]
            [chime.core :as chime])
  (:import [java.time Duration]))


(defn hourly-weather-update [city]
  (try
    (let [weather (weather/fetch-temperature city)]
      (data-store/save-to-csv (str city ".csv") weather)
      (println "Temperature saved successfully for" city))
    (catch Exception e
      (println "Fetch or save failed for" city ":" (.getMessage e)))))


(defn start-scheduler [city]
  (chime/chime-at
   (chime/periodic-seq (chime/now) (Duration/ofHours 1))
   (fn [_time]
     (hourly-weather-update city))))
