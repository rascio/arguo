(ns arguo.tools
  (:require [clojure.test :refer :all]
            [arguo.core :as arguo]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [clojure.edn :as edn])
  (:import [java.io PushbackReader]))

(defn read-values
  "Read a file containing edn values, returning a list representing it"
  [file]
  (with-open [reader (PushbackReader. (io/reader (io/resource file)))]
    (loop [v (edn/read {:eof ::eof} reader)
           res []]
      (if (= ::eof v)
        res
        (recur (edn/read {:eof ::eof} reader)
               (conj res v))))))

(defmacro with-csv
    "Cycle its body for each row in the csv, adding testing context.
     'tokens' is a list with the name of the csv columns
     (all of them will be assigned with a String value)
          (arguo/with-csv \"csv/simple.csv\"
            [Name Surname]
            (is (not= Name Surname)))
    "
    [csv tokens & body]
    (with-open [reader (io/reader (io/resource csv))]
      `(do
         ~@(->> reader
              (csv/read-csv)
              (drop 1)
              (map-indexed
                (fn [n row]
                  `(testing ~(str csv ":" (+ n 2))
                     (log/debug "📝" ~(str csv ":" (+ n 2)) ~row)
                     (let [~@(mapcat list tokens row) ~'$row-number ~(+ n 2)]
                       ~@body))))
              (doall)))))