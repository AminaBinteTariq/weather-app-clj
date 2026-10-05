(ns weather-app.core
  (:require [org.httpkit.server :as http]
            [weather-app.api :as api]
            [weather-app.scheduler :as scheduler]
            [weather-app.weather :as weather]))

(defn -main []
  ;; fail fast if the API key is missing
  (weather/api-key)
  (let [server (http/run-server api/handler {:port 8080})
        schedule (scheduler/start-scheduler "Berlin")]
    (.addShutdownHook
     (Runtime/getRuntime)
     (Thread.
      (fn []
        (.close schedule)
        (server))))
    (println "Weather API running on port 8080")))
