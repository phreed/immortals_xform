
(ns immortals.function-model-test
    (:require 
        [immortals.function-model :as fm]
        [cheshire.core :as json]
        [clojure.pprint :as pp]
        [cure.core :as cure]))
  
(->> "./res/immortals_model.json"
      clojure.java.io/reader
      json/parse-stream
      (into {})
      fm/transform
      pp/pprint)

(dir cure.core)

(defn cpr "print and return value"
    [x] (cprint x) x)

(def fm0 
    "a sample feature model: raw clauses"
    (->> "./res/immortals_model.json"
       clojure.java.io/reader
       json/parse-stream
       (into {})
       fm/transform
       cpr))         

(def fm1 "a sample feature model: compiled" (cpr (cure/feature-model fm0)))

(def fm2 "a sample feature model: compiled and ATAK selected" 
    (cpr (cure/feature-model (conj fm0 '(selected [:ATAK])))))

(cprint (cure/configuration fm2))

