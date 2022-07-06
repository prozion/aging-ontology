(defproject aging-ontology "0.0.1"
  :description "Scripts that help to develop Aging Ontology"
  :url "https://github.com/prozion/aging-ontology"
  :license {:name "MIT License"
            :url  "https://github.com/aws/mit-0"}
  :dependencies [
                [org.clojars.prozion/odysseus "0.1.5"]
                [org.clojars.prozion/tabtree "0.6.2"]
                [org.clojure/data.csv "1.0.1"]
                ]
  :plugins [
            ; [lein-ancient "0.6.15"]
            ]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.9.0"]]}}
  :release-tasks [["shell" "git" "diff" "--exit-code"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["changelog" "release"]
                  ["update-readme-version"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["deploy"]
                  ["vcs" "push"]]
  :repl-options {
    ; :init-ns scripts.process-hasd
    ; :init-ns scripts.build-aging-ontology
    :init-ns scripts.process-aada
  }
)
