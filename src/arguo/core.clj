(ns arguo.core
  (:require [arguo.utils :refer :all]
            [clojure [test :refer [testing deftest]]
                     [walk :as walk]]
            [clojure.core.strint :refer [<<]]
            [clojure.tools.logging :as log]
            [clojure.spec.alpha :as s]))

(s/def ::observe any?)
(s/def ::description any?)
(s/def ::export vector?)
(s/def ::assert any?)
(s/def ::spec any?)
(s/def ::having vector?)
(s/def ::step (s/keys :req-un [::observe] :opt-un [::description ::export ::assert ::spec ::having]))

(s/fdef step
  :args (s/cat :id symbol?
               :opts ::step
               :then (s/* any?)))

(s/fdef repeat
  :args (s/cat :description ::description
               :vars vector?
               :body (s/+ any?)))

(s/fdef flow
  :args (s/cat :description any? :steps (s/+ any?)))

(s/fdef def-use-case
  :args (s/cat :name symbol? :description (s/? string?) :steps (s/+ any?)))



(defn- eval-test-object
  [id description v spec]
  (if (some? spec)
    `(do (log/debug "🚀" ~(str id) "|" ~description)
         (assert-spec ~spec ~v))
    v))
(defn- interpolate [x]
  (if (string? x)
    `(<< ~x)
    x))

(defmacro step
  "Define an Arguo step.  
   id is a symbol that will be binded to the result of ':observe' key of the step options
   The second parameter is the map of options for the test:
   - :description => the description of the test
   - :observe => the expression to test
   - :export => a list of variables to export (in the form of a let binding)
   - :assert => the expression to assert (use 'do' to have multiple assertions)
   - :spec => a spec that the result of :observe should conform (if used the value will be conformed)
   - :having => a list of variables to use in the test (before evaluate :observe)
   Any other expression after the step options will be executed after the test,
   and can use the test id to access to its result (or any of the :export or :having symbols)
   Inside the :assert and :export the symbol *this* can be used to refer to the :observe result.
   The description can be an interpolation string (eg. \"x value is ~{x}\")"
  [id
   {:keys [description observe export assert spec having]
    :or   {description (str id)
           export []
           assert '()}}
   & then]
  `(let [~@having
         ~id ~(eval-test-object id description observe spec)
         ~@(walk/postwalk-replace {'*this* id} export)]
     (testing ~(interpolate description)
       ~(walk/postwalk-replace {'*this* id} assert))
     ~@then))

(defmacro flow
  "Define a flow of steps executed in order.
   desc is a string.  
   steps can be any valid s-expression.  
   each subsequent step is rewritten as last argument of the previous step
   Eg.
   (arguo/flow a-flow \"description\"
       (arguo/step some {})
       (do (println \"something\")))
   is rewritten to:
   (testing \"description\"
       (arguo step some {} 
              (do (println \"something\"))))
   The description can be an interpolation string (eg. \"x value is ~{x}\")"
  [desc & steps]
  (letfn [(write [[step & tail]]
                 (if (nil? tail)
                   step
                   (concat step [(write tail)])))]
    `(testing ~(interpolate desc)
         ~(write steps))))

(defmacro def-use-case
  "Define a use case as a sequence of steps which depend one another.
   The description argument is optional.
     Eg.
     (arguo/def-use-case example-use-case 
         \"Example use case\"
         (arguo/step first 
                {:description \"First step\"
                 :having [n 1]
                 :observe (+ n 1)
                 :spec (s/spec int?)
                 :assert (do (is (< n *this*))
                            (is (= *this* 2)))})
         (arguo/step second 
                ... 
                :assert (is (= second first)]))))
   It is a shortcut for:
       (deftest some-test
           (arguo/flow a-flow))"
  
  [use-case-name step-or-desc & steps]
  (if (string? step-or-desc)
    `(deftest ~use-case-name 
       (flow ~step-or-desc
             ~@steps))
    `(deftest ~use-case-name
       (flow ~(str use-case-name)
             ~@(cons step-or-desc steps)))))

(defmacro repeat 
  "Repeat the nested operations for each entry of the data list as a flow.
   Eg.
       (arguo/repeat \"Testing a list, element: ~{element}\"
                      [element [:a :b :d]]
                      (is (keyword? element)))
   "
  [description data & body]
  `(doseq ~data
     (flow ~description
           ~@body)))