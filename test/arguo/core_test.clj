(ns arguo.core-test
  (:require [clojure.test :refer [is deftest]]
            [arguo.core :as arguo]
            [clojure.spec.alpha :as s]))

;Define a test case, automatically wraps everything in a flow
(arguo/def-use-case basic-features "Testing Arguo basic features"
  (arguo/step simple-assertion
              {:description "Testing *this*, export and assertions"
               :observe "Hello World"
               ;export two variable (a-value, another-value)
               :export [a-value *this*
                        another-value "Hello World"]
               :assert (is (= "Hello World"
                              *this* ;*this* is an alias for simple-assertion (in this step)
                              simple-assertion
                              a-value
                              another-value))})
  (arguo/step spec-assertion
              {:description "Testing specs are 'conformed'"
               :observe [1 "aaa"]
               :spec (s/spec (s/cat :first int? :second string?)) ;Clojure specs are conformed ('s/cat' transforms a list into a map)
               :export [f (*this* :first)
                        s (*this* :second)]
               :assert (do (is (= f 1))
                           (is (= s "aaa")))}))

(arguo/def-use-case depending-cases "Testing dependency between steps"
  (arguo/step step-1
              {:description "Step 1"
               :observe "Hello World"
               :assert (is (= "Hello World" *this*))})
  
  (do (println "We can also write blocks of code")
      (println "step-1 ==" step-1)) ;step-1 maintain the result of the previous step

  (arguo/step step-2
              {:description "Step 2"
               :observe "Hello World"
               :assert (is (= step-1 *this*))}))

(defn ƒ
  "ƒ(0)   = 0
   ƒ(n) = ƒ(n - 1) + 2(n - 1) + 1"
  [n]
  (if (= 0 n)
    0
    (let [n' (dec n)]
      (+ (ƒ n') (* 2 n') 1))))

(deftest test-repetition 
  ;~{n} is string interpolation, it will be replaced by the value of 'n'
  ; interpolation is automatically resolved only for descriptions
  (arguo/repeat "Tests using repetition in tests: ~{n}"
                [n (range 0 10)]
                (arguo/step verify-formula
                            {:description "is true that: ƒ(~{n}) = ~{n}^2"
                             :observe (ƒ n)
                             :assert (is (= *this* (int (Math/pow n 2))))})))