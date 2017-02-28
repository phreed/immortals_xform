
(ns immortals.core 
    "GME produces output via a plugin but working in JavaScript is painful.
    This manipulates those files to produce what is needed."
  (:require [cheshire.core :as json]
            [clojure.pprint :as pp]))  

(defn is-meta-node 
  "in order to be a meta-node of a particular name"
  [candidate node-name]
  (if (= node-name (get-in candidate ["name" "name"]))
     (let [candidate-guid (get-in candidate ["guid"])
           type-meta-guid (get-in candidate ["type" "meta"])]
       (if (= candidate-guid type-meta-guid) true false))
     false))
     
(defn get-meta-node 
  "get the meta-node by its given name"
  [input-hash node-name]
  (->> input-hash
       (filter #(is-meta-node (second %) node-name))
       first second))

(defn has-ancestor 
  "walk up the inheritance tree looking for the ancestor"
  [input-hash candidate ancestor-guid]
  (let [candidate-guid (get-in candidate ["guid"])
        type-base-guid (get-in candidate ["type" "base"])
        type-meta-guid (get-in candidate ["type" "meta"])]
    (if (= candidate-guid type-base-guid)        
      false
      (has-ancestor input-hash (get input-hash type-base-guid) ancestor-guid))))
  
(defn get-nodes-having-ancestor
  "get the nodes by their type"
  [input-hash guid]
  (filterv #(has-ancestor input-hash (second %) guid) input-hash))


(let [input-hash (->> "./res/immortals_model.json"
                   clojure.java.io/reader
                   json/parse-stream
                   (into {}))
      feature-guid (:guid (get-meta-node input-hash "Feature"))
      features (get-nodes-having-ancestor input-hash feature-guid)
      requires-guid (:guid (get-meta-node input-hash "Requires"))
      requires (get-nodes-having-ancestor input-hash requires-guid)
      card-guid (:guid (get-meta-node input-hash "Cardinality"))
      card-src-guid (:guid (get-meta-node input-hash "CardinalitySource"))
      card-tgt-guid (:guid (get-meta-node input-hash "CardinalityTarget"))
      output (mapv #([:feature (get-in % ["name" "name"])]) features)]
   (pp/pprint [(get-meta-node input-hash "Feature") feature-guid requires-guid card-guid card-src-guid card-tgt-guid output]))
   
