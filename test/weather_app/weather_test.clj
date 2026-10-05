(ns weather-app.weather-test
  (:require [clojure.test :refer [deftest is]]
            [clj-http.client :as client]
            [weather-app.weather :as weather]))

(deftest fetch-temperature-returns-temperature-and-timestamp
  (with-redefs [weather/api-key
                (fn [] "fake-api-key")
                client/get
                (fn [_endpoint]
                  {:body "{\"main\":{\"temp\":15.5}}"})]
    (let [result (weather/fetch-temperature "Berlin")]
      (is (double? (:temperature result)))
      (is (string? (:timestamp result)))
      (is (= 15.5 (:temperature result))))))

(deftest fetch-temperature-fails-without-api-key
  (with-redefs [weather/api-key
                (fn []
                  (throw (ex-info "Missing WEATHER_API_KEY" {})))]

    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"Missing WEATHER_API_KEY"
                          (weather/fetch-temperature "Berlin")))))
