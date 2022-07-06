(ns scripts.process-hasd
  (:require
            [org.clojars.prozion.odysseus.io :refer :all]
            [org.clojars.prozion.odysseus.utils :refer :all]
            [org.clojars.prozion.odysseus.text :refer :all]
            [org.clojars.prozion.odysseus.debug :refer :all]
            [clojure.string :as s]
            [clojure.java.io :as io]
            [jena.triplestore :as jts]
            [org.clojars.prozion.tabtree.tabtree :as tabtree]
            [org.clojars.prozion.tabtree.rdf :as rdf]
            [org.clojars.prozion.tabtree.output :as output]
            [org.clojars.prozion.tabtree.utils :as utils :refer :all]
            [clojure.data.csv :as csv]))

(def DATABASE_PATH "/var/db/jena/aging-ontology")

(defn read-csv [filepath]
  (with-open [reader (io/reader filepath)]
    (rest
      (doall
        (csv/read-csv reader :separator \tab)))))

(defn ao-id [id]
  (keyword (format "AO%s" (-> id (subs 1)))))

(defn read-blocks []
  (let [content (read-csv "../_import/hasd_67.3_blocks.csv")]
    (reduce
      (fn [acc line]
        (let [[id name age-from age-to system] line
              id (ao-id id)]
          (merge acc {id {:__id id :name name :ageFrom age-from :ageTo age-to :system system}})))
      {}
      content)))

(defn read-links [tabtree]
  (let [content (read-csv "../_import/hasd_67.3_links.csv")]
    (reduce
      (fn [tabtree' line]
        (let [[from-id _ _ to-id _] line
              from-id (ao-id from-id)
              to-id (ao-id to-id)
              old-item (tabtree' from-id)
              ; old-children (:__children old-item)
              ; new-children (conj old-children to-id)
              new-to-id (conj (:followsTo old-item) to-id)
              new-item (merge
                          old-item
                          {
                            :followsTo new-to-id
                            ; :__children new-children
                            })
              ]
          (merge tabtree' {from-id new-item})))
      tabtree
      content)))

(defn analyze []
  (defn count-systems []
    (let [tabtree (tabtree/parse-tab-tree "../hasd/hasd_individuals.tree")]
      (->> tabtree vals (map :system) flatten (remove nil?) distinct)))
  true)

(defn generate-hasd-individuals []
  (write-to-file
    "../hasd/hasd_individuals.tree"
    (output/tabtree->string
      (-> (read-blocks) read-links)
      :sorter (fn [a b] (compare (-> a name ->integer) (-> b name ->integer))))))

(defn glue-hasd-scheme-and-hasd-nodes []
  (let [scheme (read-file "../hasd/hasd_scheme.tree")
        individuals-lines (read-file-by-lines "../hasd/hasd_individuals.tree")
        formatted-individuals (format "%s\n" (s/join "\n\t\t\t" individuals-lines))
        filled-scheme (s/replace scheme "[HASDNode instances]" formatted-individuals)]
    (write-to-file
      "../hasd/hasd.tree"
      filled-scheme)))

(defn generate-hasd-rdf []
  (glue-hasd-scheme-and-hasd-nodes)
  (let [tabtree (tabtree/parse-tab-tree "../hasd/hasd.tree")]
    (write-to-file
      "../hasd/hasd.ttl"
      (rdf/tabtree->rdf tabtree))))
