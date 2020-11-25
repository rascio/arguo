(ns arguo.http-test
  (:require [clojure.test :refer [use-fixtures is]]
            [arguo.core :as arguo]
            [arguo.http :as http]
            [clojure.spec.alpha :as s]
            [arguo.server :as server]))

(defn server-fixture [t]
  (server/start)
  (t)
  (server/stop))

(use-fixtures :once server-fixture)

(def localhost (http/host "http:localhost:3000"))
(def localhost-with-defaults (http/host "http:localhost:3000"
                                   :as :json))

(arguo/def-use-case http-features "Testing Arguo HTTP features"
  (arguo/step simple-http-get
              {:description "Simple test using HTTP GET"
               :observe (http/request :GET localhost "/")
               :assert (is (= "Hello World" (:body *this*)))})
  (arguo/step json-http-get
              {:description "HTTP GET with json response"
               :observe (http/request :GET localhost ["first"]
                                      :as :json)
               :assert (is (= "world" (-> *this* :body :hello)))})
  (arguo/step json-http-get-with-params
              {:description "HTTP GET with json response"
               :having [name "Joe"]
               :observe (http/request :GET localhost-with-defaults "/first"
                                      :query-params {:name name})
               :assert (is (= name (-> *this* :body :hello)))})
  (arguo/step json-http-post
              {:description "HTTP POST sending a json"
               :observe (http/request :POST localhost-with-defaults "/post"
                                      :content-type :json
                                      :form-params {:world "hello"})
               :assert (is (= "hello" (-> *this* :body :world)))})
  (arguo/step simple-http-error
              {:description "Expected HTTP 400"
               :observe (http/request :GET localhost "/post")
               :assert (is (= 400 (:status *this*)))}))
