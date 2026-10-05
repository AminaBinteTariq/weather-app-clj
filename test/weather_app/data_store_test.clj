(ns weather-app.data-store-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is]]
            [weather-app.data-store :as data-store]))

(deftest read-from-csv-returns-records
  (let [records (data-store/read-from-csv
                 "test/weather_app/data/sample_weather.csv")]

    (is (= 5 (count records)))

    (is (every? #(contains? % :temperature) records))
    (is (every? #(contains? % :timestamp) records))

    (is (= "2026-10-04T20:01:08"
           (:timestamp (first records))))
    (is (= 13.86
           (:temperature (first records))))

    (is (every? #(double? (:temperature %)) records))
    (is (every? #(string? (:timestamp %)) records))))


(deftest read-from-csv-returns-empty-for-missing-file
  (let [records (data-store/read-from-csv
                 "test/weather_app/data/invalid_file.csv")]
    (is (= [] records))))


(deftest read-from-csv-returns-empty-for-empty-file
  (let [records (data-store/read-from-csv
                 "test/weather_app/data/empty.csv")]
    (is (= [] records))))


(deftest read-from-csv-returns-empty-for-header-only-file
  (let [records (data-store/read-from-csv
                 "test/weather_app/data/header_only.csv")]
    (is (= [] records))))


(def sample-hourly-update {:timestamp "2026-10-05T10:00:00"
                           :temperature 15.5})

(deftest save-to-csv-creates-file-if-not-found
  (let [filepath "test/weather_app/data/test_berlin.csv"]
    (try
      ;; check that the file was missing before testing
      (is (not (.exists (io/file filepath))))

      (data-store/save-to-csv filepath sample-hourly-update)

      ;; this should create a new file
      (is (.exists (io/file filepath)))
      
      ;; ensure that the records are saved in new file
      (let [records (data-store/read-from-csv filepath)]
        (is (= [sample-hourly-update] records)))

      (finally
        ;; remove the file after testing
        (when (.exists (io/file filepath))
          (.delete (io/file filepath)))))))


(deftest save-to-csv-does-not-duplicate-header
  (let [filepath "test/weather_app/data/test_berlin.csv"]
    (try
      ;; First write to a missing file add headers
      (data-store/save-to-csv filepath sample-hourly-update)

      ;; Second write shouldn't add headers 
      (data-store/save-to-csv filepath sample-hourly-update)

      ;; Read the raw file as a string
      (let [content (slurp filepath)]

        ;; (println (re-seq #"(?i)^timestamp,temperature" content)))
        ;; count of headers (case insensitive) from the string should be 1
        (is (= 1
               (count (re-seq #"(?i)^timestamp,temperature" content)))))

      (finally
        (when (.exists (io/file filepath))
          (.delete (io/file filepath)))))))