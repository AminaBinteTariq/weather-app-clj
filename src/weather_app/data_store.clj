(ns weather-app.data-store
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]))


(defn save-to-csv [filepath hourly-update]
  (let [file (io/file filepath)
        datarow [(:timestamp hourly-update)
                 (:temperature hourly-update)]]
    (if (or (not (.exists file))
            (zero? (.length file)))
      (with-open [writer (io/writer file)]
        (csv/write-csv writer [["timestamp" "temperature"] datarow]))
      (with-open [writer (io/writer file :append true)]
        (csv/write-csv writer [datarow])))))


(defn read-from-csv [filepath]
  (let [file (io/file filepath)]
    (if (or (not (.exists file))
            (zero? (.length file)))
      []
      (with-open [r (io/reader file)]
        (let [[_header & rows] (csv/read-csv r)]
          ;; skip rows with a missing timestamp or temperature
          (into []
                (keep (fn [[ts temp]]
                        (when-let [t (and (seq ts) (string? temp) (parse-double temp))]
                          {:timestamp ts
                           :temperature t})))
                rows))))))
