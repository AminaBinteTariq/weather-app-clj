(ns weather-app.weather-test
  (:require [clojure.test :refer [deftest is]]
            [clj-http.client :as client]
            [weather-app.weather :as weather]))

(deftest fetch-temperature-returns-temperature-and-timestamp
  ;; improved using AI
  (let [request-opts (atom nil)]
    (with-redefs [weather/api-key
                  (fn [] "fake-api-key")
                  client/get
                  (fn [_url opts]
                    (reset! request-opts opts)
                    {:body "{\"main\":{\"temp\":15.5}}"})]
      (let [result (weather/fetch-temperature "Berlin")]
        (is (= {"q" "Berlin" "units" "metric" "appid" "fake-api-key"}
               (:query-params @request-opts)))
        (is (pos? (:connection-timeout @request-opts)))
        (is (pos? (:socket-timeout @request-opts)))
        (is (double? (:temperature result)))
        (is (string? (:timestamp result)))
        (is (= 15.5 (:temperature result)))))))

(deftest fetch-temperature-fails-without-api-key
  (with-redefs [weather/api-key
                (fn []
                  (throw (ex-info "Missing WEATHER_API_KEY" {})))]
    ;; improved using AI
    (is (thrown-with-msg? clojure.lang.ExceptionInfo
                          #"Missing WEATHER_API_KEY"
                          (weather/fetch-temperature "Berlin")))))
