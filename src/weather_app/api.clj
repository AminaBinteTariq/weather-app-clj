(ns weather-app.api
  (:require [weather-app.data-store :as data-store]
            [charred.api :as charred]))

(def city "Berlin")

(defn handler [request]
  (let [method (:request-method request)
        uri    (:uri request)]
    (cond
      (and (= method :get) (= uri "/"))
      {:status 200
       :headers {"Content-Type" "text/plain"}
       :body "Welcome to the Weather App!"}

      (and (= method :get) (= uri "/temperatures"))
      {:status 200
       :headers {"Content-Type" "application/json"}
       :body (charred/write-json-str (data-store/read-from-csv city))}

      :else
      {:status 404
       :headers {"Content-Type" "text/plain"}
       :body "Not found"})))
