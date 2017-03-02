
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

(def fm 
    (->> "./res/immortals_model.json"
      clojure.java.io/reader
      json/parse-stream
      (into {})
      fm/transform))


(def tm (cure/feature-model fm))
