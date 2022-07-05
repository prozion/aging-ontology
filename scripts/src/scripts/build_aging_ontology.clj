(ns scripts.build-aging-ontology
  (:require [clojure.test :refer :all]
            [clojure.string :as s]
            [jena.triplestore :as ts]
            [org.clojars.prozion.tabtree.rdf :as rdf]
            [org.clojars.prozion.tabtree.output :as output]
            [org.clojars.prozion.tabtree.atext :as atext]
            [org.clojars.prozion.tabtree.utils :as utils]))

(def DATABASE_PATH "/var/db/jena/tabtree")
(def TTL-FILEPATH "../core/aon.ttl")
(def TABTREE-FILEPATH "../core/aon.tree")

(def ^:dynamic *source-output?*)

(defn generate-aon []
  (binding [*source-output?* true]
    (spit
      TTL-FILEPATH
      (rdf/tabtree->rdf
        (atext/extract-annotations
          "../sources/common.atxt"
          "../sources/Khaltourina_2020_Mechanisms_of_ageing_and_development.atxt"
        )))
    (let [db (ts/init-db DATABASE_PATH TTL-FILEPATH)]
      (spit
        TABTREE-FILEPATH
        (output/tabtree->string
          (rdf/rdf->tabtree db TTL-FILEPATH))))))
