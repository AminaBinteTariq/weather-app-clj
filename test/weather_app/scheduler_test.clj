(ns weather-app.scheduler-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer [deftest is]]
            [weather-app.data-store :as data-store]
            [weather-app.scheduler :as scheduler]
            [weather-app.weather :as weather]))


(deftest hourly-weather-update-saves-fetched-reading
  (let [filepath "test/weather_app/data/test_scheduler.csv"
        reading {:timestamp "2026-10-05T10:00:00"
                 :temperature 15.5}]
    ;; improved using AI
    (try
      (with-redefs [weather/fetch-temperature (fn [_city] reading)]
        (with-out-str
          (scheduler/hourly-weather-update "Berlin" filepath)))
      (is (= [reading] (data-store/read-from-csv filepath)))
      (finally
        (when (.exists (io/file filepath))
          (.delete (io/file filepath)))))))


(deftest hourly-weather-update-logs-and-continues-when-fetch-fails
  (let [save-called (atom false)]
    (with-redefs [weather/fetch-temperature (fn [_city]
                                              (throw (ex-info "Request timeout" {})))
                  data-store/save-to-csv (fn [_filepath _reading]
                                           (reset! save-called true))]
      ;; improved using AI
      (let [output (with-out-str
                     (scheduler/hourly-weather-update "Berlin" "test.csv"))]
        (is (str/includes? output "Request timeout"))
        (is (false? @save-called))))))
