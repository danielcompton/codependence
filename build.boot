; vim: syntax=clojure
(set-env!
  :resource-paths #{"src" "resources"}
  :source-paths #{"src"}
  :dependencies '[[org.clojure/clojure "1.9.0-alpha16" :scope "provided"]
                  [integrant "0.4.0"]
                  [org.clojure/spec.alpha "0.1.108"]
                  [org.clojure/clojurescript "1.9.542" :scope "test"]
                  [adzerk/boot-test        "1.2.0"     :scope "test"]
                  [adzerk/boot-cljs        "2.0.0"     :scope "test"]
                  [crisptrutski/boot-cljs-test "0.3.0" :scope "test"]]
  :repositories #(conj % ["clojars" {:url "https://clojars.org/repo/"}])
  )

(require '[adzerk.boot-test :as t]
         '[crisptrutski.boot-cljs-test :refer [test-cljs]])

(task-options!
 pom {:project 'irresponsible/codependence
      :version "0.1.1"
      :description "Lightweight, flexible, configuration-driven dependencies-resolved app structure"}
 push {:tag true
       :ensure-branch "master"
       :ensure-release true
       :ensure-clean true
       :gpg-sign true
       :repo "clojars"
 }
 target {:dir #{"target"}})

(deftask testing []
  (set-env! :source-paths  #(conj % "test")
            :resource-paths #(conj % "test"))
  identity)

(deftask test []
  (testing)
  (t/test)
  (test-cljs))

(deftask autotest []
  (comp (testing) (watch) (test)))

;; RlsMgr Only stuff
(deftask release []
  (comp (pom) (jar) (push)))
