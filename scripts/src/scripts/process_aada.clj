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
         (s/replace #"[:\"&.,]" "")
         (s/replace #"[\s/]" "_")
         (s/replace #"\+" "")
         (s/replace #"_{2,}" "_")
         (s/replace #"_$" "")
         keyword))

(defn make-aada-individuals-tabtree []
  (let [content (read-tsv "../biomarkers/all_aging_diagnostic_approaches.tsv")
        content (drop 8 content)
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
                    panels (include-to-coll
                              not-empty?
                              [horvath-clock :Horvath_Clock]
                              [hannum-clock :Hannum_Clock]
                              [dnam-phenoage :DNAm_PhenoAge]
                              [frailty-index :Frailty_Index]
                              [singapore-panel :Singapore_Panel]
                              [chinese-panel :Chinese_Panel]
                              [british-panel :British_Panel]
                              [sens :SENS_Panel]
                              [sasp :SASP_Panel]
                              [tame :TAME_Panel]
                              [gdf11 :GDF11_Panel]
                              [ol-basic :Open_Longevity_Panel_Basic]
                              [ol-pro :Open_Longevity_Panel_Pro]
                              [ol-new :Open_Longevity_Panel_New]
                              [theranostic-full :Theranostic_Panel_Full]
                              [theranostic-oncology :Theranostic_Panel_Oncology]
                              [theranostic-brain-and-depression :Theranostic_Panel_Brain_and_Depression]
                              [theranostic-heart-and-vessels :Theranostic_Panel_Heart_and_Vessels]
                              )
                    class (case type
                            "Molecular" :aon/MolecularBiomarker
                            "Hardware" :aon/HardwareBiomarker
                            "Hardware diagnostics and biochemistry" :aon/HardwareBiomarker
                            "Phenotypic biomarkers" :aon/PhenotypicBiomarker
                            "Questionnaires and tests" :aon/QuestionnairyBiomarker
                            :aon/Biomarker)
                    system (case system-organ-process
                              "Cardiovascular system" :org/Cardiovascular_system
                              "Hormonal system" :org/Hormonal_system
                              "DNA" :DNA_system
                              "RNAi transcription" :cell/RNAi_transcription
                              "Metabolism" :org/Metabolism_system
                              "Glucose metabolism" :org/Glucose_metabolism_system
                              "Oxidative stress, mitochondria and apoptosis" [:cell/Oxidative_stress :cell/Mitochondria :cell/Apoptosis]
                              "Protein glycation" :cell/Protein_glycation
                              "Cell senescence" :cell/Cell_senescence
                              "Inflammation and intercellular communication" [:do/Inflammation :cell/Intercellular_communication]
                              "Fibrosis" :cell/Fibrosis
                              "Neurons and neuromuscular junction" :org/Nervous_system
                              "All-cause mortality" :do/All-cause_mortality
                              "Mortality related CVDs" :do/Mortality_related_CVDs
                              "Mortality related neoplasms" :do/Mortality_related_neoplasms
                              "Mortality related respiratory diseases" :do/Mortality_related_respiratory_diseases
                              "Lung function" :org/Respiratory_system
                              "Bone health" :org/Skeletal_system
                              "Skeletal muscle and body composition" :org/Muscular_system
                              "Immune function" :org/Immune_system
                              "Physical abilities" :Physical_ability
                              "Facial features" :Facial_feature
                              "Cognitive function" :Cognitive_function
                              "Immune system" :org/Immune_system
                              "Participate in DNA damage response" :cell/DNA_damage_response
                              nil)
                    ]
                [(conj
                  acc
                  {
                    id
                    (merge
                      { :__id id :name name :a class}
                      (when (not-empty? panels) {:panel panels})
                      (when system {:system system})
                      ; (when (not-empty? hallmark-of-aging) {:agingMechanism hallmark-of-aging})
                    )
                    })
                  (inc i)]))
            [{} 1]
            content)
        ]
    (write-to-file
      "../biomarkers/aada_individuals.tree"
      (output/tabtree->string content))
    true))

(defn build-aada []
  (let [aada-tabtree "../biomarkers/aada.tree"]
    (make-aada-individuals-tabtree)
    (write-to-file
      aada-tabtree
      (output/glue-tabtree-file "../biomarkers/aada.glue" :glue-files-root "../biomarkers/"))
    (write-to-file
      "../biomarkers/aada.ttl"
      (rdf/tabtree->rdf (tabtree/parse-tab-tree aada-tabtree)))))
