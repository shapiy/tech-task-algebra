(defproject tech-task-algebra "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [medley "1.3.0"]
                 [prismatic/schema "1.1.12"]]
  :main ^:skip-aot tech-task-algebra.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
