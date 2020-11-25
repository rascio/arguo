(ns arguo.utils
  (:require [clojure.spec.alpha :as s]))

(defn assert-spec [spec v]
  (let [res (s/conform spec v)]
    (if (= res :clojure.spec.alpha/invalid)
      (throw (ex-info (s/explain-str spec v) {:value v}))
      res)))

(defn deep-merge [a b]
  (merge-with (fn [x y]
                (cond (map? y) (deep-merge x y)
                      (vector? y) (concat x y)
                      :else y))
              a b))

(defmacro debug
  "Utility to debug an expression, usage:
      (debug + 5 6) ;print '(+ 5 6) 11'
                    ;evaluate to 11
  "
  [& expr]
  `(do
     (println "Debug:" (first (quote ~expr)))
     (println (quote ~expr))
     (println "aruments:")
     (println ~@(rest expr))
     (let [res# ~expr]
       (println "result:")
       (println res#)
       (println "------------")
       res#)))
