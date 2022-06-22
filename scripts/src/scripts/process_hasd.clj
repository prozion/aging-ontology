(ns scripts.process-hasd
  (:require
            [org.clojars.prozion.odysseus.io :refer :all]
            [org.clojars.prozion.odysseus.utils :refer :all]
            [org.clojars.prozion.odysseus.text :refer :all]
            [org.clojars.prozion.odysseus.debug :refer :all]
            [clojure.string :as s]
            [jena.triplestore :as jts]
            [org.clojars.prozion.tabtree.tabtree :as tabtree]
            [org.clojars.prozion.tabtree.rdf :as rdf]
            [org.clojars.prozion.tabtree.output :as output]
            [org.clojars.prozion.tabtree.utils :as utils :refer :all]))

(def DATABASE_PATH "/var/db/jena/aging-ontology")

(defn get-ages [age]
  (if (not age)
    [nil nil]
    (let [age-splitted (s/split age #"-")
          several-ages? (> (count age-splitted) 1)
          bottom-age (and several-ages? (first age-splitted))
          top-age (and several-ages? (second age-splitted))]
      [bottom-age top-age])))

(defn hasd->tabtree []
  (let [tabtree (tabtree/parse-tab-tree "../hasd/hasd_nodes.tree")
        tabtree (map-hash
                  (fn [[id item]]
                    (let [aname (:name item)
                          age (some-> item :age name)
                          [bottom-age top-age] (get-ages age)
                          new-id (if aname
                                     (-> aname tabtree/idify titlefy keyword)
                                     (->> (:__id item) name (format "node_%s") keyword))
                          _ (--- id new-id)
                          node-number (->integer (name id))
                          new-item (merge item {:__id new-id :hasd-node-number node-number :a :HASD_Node})
                          new-item (if bottom-age (conj new-item {:bottom-age bottom-age}) new-item)
                          new-item (if top-age (conj new-item {:top-age top-age}) new-item)
                          new-item (if (or bottom-age top-age) (dissoc new-item :age) new-item)
                          ]
                    {new-id new-item}))
                  tabtree)]
    tabtree))

(defn generate-hasd-tabtree []
  (write-to-file
    "../hasd/ontology.tree"
    (output/tabtree->string (hasd->tabtree))))
