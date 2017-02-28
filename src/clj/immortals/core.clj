
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
      (if (= type-base-guid ancestor-guid) 
         true
         (has-ancestor input-hash (get input-hash type-base-guid) ancestor-guid)))))
  
(defn get-nodes-having-ancestor
  "get the nodes by their type"
  [input-hash guid]
  (filterv #(has-ancestor input-hash (second %) guid) input-hash))

(defn get-guid [obj] (get obj "guid"))

(defn serialize-feature 
  [feature]
  (list 'feature (keyword (get-in (second feature) ["name" "name"]))))

(defn serialize-requires 
  [requires input-hash]
  (let [values (second requires)
        src-guid (get-in values ["pointers" "src" "guid"])
        tgt-guid (get-in values ["pointers" "dst" "guid"])
        src (get input-hash src-guid)
        tgt (get input-hash tgt-guid)
        src-name (get-in src ["name" "name"])
        tgt-name (get-in tgt ["name" "name"])]
    (list 'requires (keyword src-name) 1 1 (keyword tgt-name))))
  
(defn get-cards
  "get the nodes by their type"
  [input-hash card-guid card-src-guid card-tgt-guid]
  (second input-hash))

(defn serialize-cardinality 
  [cards]
  #(list 'requires (get-in (second requires) ["name" "name"])))

(let [input-hash 
         (->> "./res/immortals_model.json"
              clojure.java.io/reader
              json/parse-stream
              (into {}))
      feature-guid (get-guid (get-meta-node input-hash "Feature"))
      features (get-nodes-having-ancestor input-hash feature-guid)
      feature-output (mapv #(serialize-feature %) features)

      requires-guid (get-guid (get-meta-node input-hash "Requires"))
      requires (get-nodes-having-ancestor input-hash requires-guid)
      req-output (mapv #(serialize-requires % input-hash) requires)

      card-guid (get-guid (get-meta-node input-hash "Cardinality"))
      cards (get-nodes-having-ancestor input-hash card-guid)
      card-src-guid (get-guid (get-meta-node input-hash "CardinalitySource"))
      card-tgt-guid (get-guid (get-meta-node input-hash "CardinalityTarget"))
      cards (get-cards input-hash card-guid card-src-guid card-tgt-guid)
      card-output (mapv #(serialize-cardinality %) cards)]
  (pp/pprint [feature-output req-output card-output]))
   
