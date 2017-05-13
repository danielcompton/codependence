(ns irresponsible.codependence
  #?(:clj (:refer-clojure :exclude [ref]))
  (:require [#?(:clj clojure.spec.alpha :cljs cljs.spec.alpha) :as s]
            [integrant.core :as i]
            #?(:clj [clojure.edn :as edn])))

(defmulti start-tag (fn [t _] t) :default ::default)
(defmulti stop-tag  (fn [t _] t) :default ::default)
(defmethod start-tag ::default [_ v] v)
(defmethod stop-tag  ::default [_ v] nil)

(def ref i/ref)

#?
(:clj
 (defn do-require [n data]
   (try (require n)
        (catch java.io.FileNotFoundException e
          (throw (ex-info (str "codependence: Failed loading namespace: " n) {:ns n :data data}))))))

(defn start-key
  ""
  [_ v]
  (if (map? v)
    (do #?(:clj
           (when-let [l (:co/load v)]
             (cond (symbol? l) (do-require l v)
                   (seq v)     (doseq [l2 l] (do-require l2 v)))))
      (if-let [t (:co/tag v)]
        (vary-meta (start-tag t v) assoc ::tag t)
        v))
    v))

(defn stop-key
  [_ v]
  (do (some-> v meta ::tag (stop-tag v))
      nil))

(defn start!
  "Initialises the configuration according to our rules
   args: [config] [config keys]"
  ([config]
   (start! config (keys config)))
  ([config keys]
   (i/build config keys start-key)))

(defn stop!
  ""
  ([system]
   (stop! system (keys system)))
  ([system keys]
   (i/reverse-run! system keys stop-key)))

#?
(:clj
 (defn read-string
   "Read a config from a string of edn. Refs may be denotied by tagging keywords with #co/ref."
     ([opts s]
      (edn/read-string {:readers {:co/ref ref} :eof nil} s))))
