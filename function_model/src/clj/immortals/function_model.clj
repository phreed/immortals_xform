
(ns immortals.function-model 
    "GME produces output via a plugin but working in JavaScript is painful.
    This manipulates those files to produce what is needed.")

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
  (list 'feature (keyword (get-in feature ["name" "name"]))))

(defn serialize-requires 
  "Write one record for each Requires connection.
  The Requires meta-node is passed as the first arg."
  [requires input-hash]
  (let [src-guid (get-in requires ["pointers" "src" "guid"])
        tgt-guid (get-in requires ["pointers" "dst" "guid"])
        src (get input-hash src-guid)
        tgt (get input-hash tgt-guid)
        src-name (get-in src ["name" "name"])
        tgt-name (get-in tgt ["name" "name"])]
    (list 'requires (keyword src-name) 1 1 [(keyword tgt-name)])))

(defn cardinality-tuple 
  "convert a string representing cardinality to a pair.
  1..n 0..1 0..N 1..*"
  [str]
  (let [tuple (re-find #"(?x)  # allow embedded whitespace and comments
                   (\d)    # the first indicator must be a single digit
                   \.\.    # there must be two dots
                   ([\dnN*]) # a digit or an unlimited indicator
                   " str)]
      (if tuple 
        (let 
            [left (Integer/parseInt (nth tuple 1))
             right (as-> (nth tuple 2) & 
                     (case &
                       ("n" "N" "*") 5
                       (Integer/parseInt &)))]
          [left right]))))

(defn serialize-cardinality 
  "Write one record for each CardinalitySource,
  The CardinalitySource meta-node is passed as the first arg."
  [card-src input-hash]
  (let [feat-name
          (as-> (get-in card-src ["pointers" "src" "guid"]) &
                (get input-hash &)
                (get-in & ["name" "name"])
                (keyword &))
        card-guid (get-in card-src ["pointers" "dst" "guid"])
        card (get input-hash card-guid)
        [min max] (cardinality-tuple (get-in card ["attributes" "Type"]))
        card-set-names 
          (->> (get-in card ["inv_pointers" "src"])
               (map get-guid)
               (map #(get input-hash %))
               (map #(get-in % ["pointers" "dst" "guid"]))
               (map #(get input-hash %))
               (mapv #(get-in % ["name" "name"]))
               (mapv keyword))]
    (list 'requires feat-name min max card-set-names)))

(defn extract-features
  [input-hash]
  (let [feature-guid (get-guid (get-meta-node input-hash "Feature"))
        features (get-nodes-having-ancestor input-hash feature-guid)]
      (mapv #(serialize-feature (second %)) features)))

(defn extract-requires 
  [input-hash]
  (let [requires-guid (get-guid (get-meta-node input-hash "Requires"))
        requires (get-nodes-having-ancestor input-hash requires-guid)]
      (mapv #(serialize-requires (second %) input-hash) requires)))

(defn extract-cards
  [input-hash]
  (let [card-src-guid (get-guid (get-meta-node input-hash "CardinalitySource"))
        card-srcs (get-nodes-having-ancestor input-hash card-src-guid)]
      (mapv #(serialize-cardinality (second %) input-hash) card-srcs)))
             
(defn transform
  "The primary function for performing the transformation"
  [input-hash]
  (vec 
    (concat 
      (extract-features input-hash) 
      (extract-requires input-hash)
      (extract-cards input-hash))))
 
