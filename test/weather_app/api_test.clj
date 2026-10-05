(ns weather-app.api-test
  (:require [clojure.test :refer [deftest is]]
            [charred.api :as charred]
            [weather-app.api :as api]
            [weather-app.data-store :as data-store]))

(deftest handler-returns-welcome-response
  (let [response (api/handler {:request-method :get
                               :uri "/"})]
    (is (= 200 (:status response)))
    (is (= "text/plain" (get-in response [:headers "Content-Type"])))
    (is (= "Welcome to the Weather App!" (:body response)))))


(deftest handler-returns-404-for-unknown-route
  (let [unknown-path-response (api/handler {:request-method :get
                                            :uri "/random"})
        unsupported-method-response (api/handler {:request-method :post
                                                  :uri "/"})]
    (is (= 404 (:status unknown-path-response)))
    (is (= "Not found" (:body unknown-path-response)))
    (is (= 404 (:status unsupported-method-response)))
    (is (= "Not found" (:body unsupported-method-response)))))


(deftest handler-returns-temperature-data-as-json
  (let [expected-readings [{:timestamp "2026-10-05T10:00:00"
                            :temperature 15.5}]]
    (with-redefs [data-store/read-from-csv (fn [_] expected-readings)]
      (let [response (api/handler {:request-method :get
                                   :uri "/temperatures"})]
        (is (= 200 (:status response)))
        (is (= "application/json"
               (get-in response [:headers "Content-Type"])))
        (is (= expected-readings
               (charred/read-json (:body response) :key-fn keyword)))))))
