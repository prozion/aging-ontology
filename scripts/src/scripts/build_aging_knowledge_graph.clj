(ns scripts.build-aging-knowledge-graph
  (:require [clojure.test :refer :all]
            [clojure.string :as s]
            [jena.triplestore :as ts]
            [org.clojars.prozion.odysseus.io :refer :all]
            [org.clojars.prozion.odysseus.utils :refer :all]
            [org.clojars.prozion.odysseus.text :refer :all]
            [org.clojars.prozion.odysseus.debug :refer :all]
            [org.clojars.prozion.tabtree.tabtree :as tabtree]
            [org.clojars.prozion.tabtree.rdf :as rdf]
            [org.clojars.prozion.tabtree.output :as output]
            [org.clojars.prozion.tabtree.atext :as atext]
            [org.clojars.prozion.tabtree.utils :as utils]
            ))

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

(defn make-ttl [tabtree-file ttl-file]
  (write-to-file
    ttl-file
    (rdf/tabtree->rdf (tabtree/parse-tab-tree tabtree-file))))

(defn make-ttls []
  (make-ttl "../ontologies/aon.tree" "../knowledge_graph/aon.ttl")
  (make-ttl "../ontologies/cell.tree" "../knowledge_graph/cell.ttl")
  (make-ttl "../ontologies/disease.tree" "../knowledge_graph/disease.ttl")
  (make-ttl "../ontologies/drugs.tree" "../knowledge_graph/drugs.ttl")
  (make-ttl "../ontologies/organism.tree" "../knowledge_graph/organism.ttl")
  (make-ttl "../ontologies/therapies.tree" "../knowledge_graph/therapies.ttl"))
