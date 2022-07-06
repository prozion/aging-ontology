(ns scripts.process-aada
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
            ))

(defn idify [id]
  (-> id
         (s/replace #"\(.+\)" "")
         (s/replace #"\s" "_")
         keyword))

(defn choose-not-empty [& args]
  (cond
    (empty? args) nil
    (not-empty? (first args)) (first args)
    :else (apply choose-not-empty (rest args))))

(defn make-aada-tabtree []
  (let [content (read-tsv "../_import/aada.tsv")
        content (drop 9 content)
        ; content (remove (fn [[_ marker-en]] (empty? marker-en)) content)
        [content _]
          (reduce
            (fn [[acc i]
                  [marker-ru
                   marker-en
                   whats-in-general
                   type
                   system-organ-process
                   hallmark-of-aging
                   cytokine
                   panel-inclusion-ranking
                   horvath-clock
                   hannum-clock
                   dnam-phenoage
                   frailty-index
                   singapore-panel
                   chinese-panel
                   british-panel
                   sens
                   sasp
                   tame
                   gdf11
                   ol-basic
                   ol-pro
                   ol-new
                   theranostic-full
                   theranostic-oncology
                   theranostic-brain-and-depression
                   theranostic-heart-and-vessels
                   :as item
                   ]]
              (let [id (choose-not-empty marker-en marker-ru (str i))
                    id (idify id)
                    id (if (acc id) (idify marker-ru) id)
                    name (-> (choose-not-empty marker-en marker-ru) (s/replace #"\s+" " "))
                    panels [
                            (and (not-empty? horvath-clock) :Horvath_Clock)
                            (and (not-empty? hannum-clock) :Hannum_Clock)
                            (and (not-empty? dnam-phenoage) :DNAm_PhenoAge)
                           ]
                    panels (remove false? panels)
                    ; _ (--- i (inc (count (keys acc))) id item)
                    ]
                [(conj
                  acc
                  {
                    id
                    (merge
                      { :__id id :name name }
                      (when (not-empty? panels) {:panel panels}))
                    })
                  (inc i)]))
            [{} 1]
            content)
        ]
    (write-to-file
      "../ontologies/biomarkers.tree"
      (output/tabtree->string content))
    true))
