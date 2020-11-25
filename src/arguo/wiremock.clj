(ns arguo.wiremock
    (:require [arguo.http :as http]))

(defn create-mapping
  [host params]
  (let [response (http/request :POST host ["__admin" "mappings"]
                               :content-type :json
                               :as :json
                               :form-params params)]
    (assert (= 201 (:status response)) (str "Not status 201 " response))
    (:body response)))

(defn delete-mapping
  [host mapping]
  (let [response (http/request :DELETE host ["__admin" "mappings" (:id mapping)])]
    (assert (= 200 (:status response)) (str "Not status 200 " response))
    (:body response)))

(defn test-setup
  "Usage:
        (use-fixtures :once (arguo.wiremock/test-setup host [{:request {...} :response {...}}]))
                                                            {:request {...} :response {...}}
   "
  [host & mocks]
  (fn [test]
    (let [mappings (->> mocks
                    (map (partial create-mapping host))
                    (doall))]
        (test)
        (doseq [mapping mappings]
          (delete-mapping host mapping)))))
     
