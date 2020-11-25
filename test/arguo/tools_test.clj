(ns arguo.tools-test
  (:require [clojure.test :refer [is]]
            [arguo.core :as arguo]
            [arguo.tools :as tools]))

(arguo/def-use-case testing-csv
  (tools/with-csv "test.csv"
    [first second]
    (arguo/step assertion-on-csv-entry
                {:observe (- (Integer/parseInt second) (Integer/parseInt first))
                 :assert (is (= 1 *this*))})))

(arguo/def-use-case test-edn
  (arguo/step reading-file
              {:observe (tools/read-values "test-data.edn")
               :assert (is (= *this*
                              [{:test true}
                               {:test false}
                               1
                               "pippo"
                               :test/something]))}))