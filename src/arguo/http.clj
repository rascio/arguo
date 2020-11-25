(ns arguo.http
  (:require [clj-http.client :as http]
            [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [arguo.utils :as u]
            [clojure.tools.logging :as log]))

(def ^:private http->fn
  {:GET    http/get
   :POST   http/post
   :PUT    http/put
   :DELETE http/delete
   :HEAD   http/head})

(defn- do-request
  "Wrapper, with exception handling, for clj-http"
  [method uri opts]
  (try
    (log/debug "📩" method uri)
    ((http->fn method) uri (assoc opts :coerce :always))
    (catch clojure.lang.ExceptionInfo e#
      ((Throwable->map e#) :data))
    (catch Exception e
      (throw
       (ex-info "Error during http call"
                {:uri uri
                 :method method}
                e)))))

(defn host [host & {:as default-opts}]
  [host (or default-opts {})])

(defmulti create-uri #(first %2))

(defmethod create-uri :str [host [_ path]]
  (str host path))

(defmethod create-uri :coll [host [_ segments]]
  (string/join "/" (cons host segments)))
    

(s/def ::request
  (s/cat :method keyword?
         :host (s/tuple string? map?)
         :path (s/or :str string? :coll (s/coll-of some?))
         :opts (s/* (s/cat :key keyword? :val some?))))

(defn request [& args]
  (let [conf (u/assert-spec ::request args)
        method (:method conf)
        [host default-opts] (:host conf)
        path (:path conf)
        opts (->> (:opts conf)
                  (map (juxt :key :val))
                  (into {}))
        uri (create-uri host path)]
    (do-request method uri (u/deep-merge default-opts opts))))

(s/fdef request :args ::request)