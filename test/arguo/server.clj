(ns arguo.server
  (:require [ring.adapter.jetty :as jetty]
            [ring.util.response :as response]
            [ring.middleware.params :as params]
            [ring.middleware.json :as json]))

(defn handler [request]
  (case (:uri request)
    "/" {:status 200
         :headers {"Content-Type" "text/plain"}
         :body "Hello World"}
    "/first" (let [name (or (get-in request [:query-params "name"]) "world")]
                (response/response {:hello name}))

    "/post" (if (= :post (:request-method request))
                (response/response (:body request))
                (response/bad-request "Not a POST"))))

(defonce ^:private server (atom nil))

(defn start []
  (swap! server (fn [s]
                    (when (some? s)
                        (println "Server already started, restarting it...")
                        (.stop s))
                    (jetty/run-jetty
                        (-> handler
                            (params/wrap-params)
                            (json/wrap-json-response))
                        {:port 3000
                         :join? false}))))

(defn stop []
    (swap! server (fn [s]
                    (if (some? s)
                      (do
                        (println "Stopping server...")
                        (.stop s))
                      (println "No server started.")))))
  
