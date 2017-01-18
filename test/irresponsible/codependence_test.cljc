(ns irresponsible.codependence-test
  (:require [irresponsible.codependence :as c]
            [integrant.core :as i]
            [#?(:clj clojure.test :cljs cljs.test) :as t]))

(t/deftest start-tag-test
  (defmethod c/start-tag ::start-tag-test [_ v] [::bar v])
  (t/is (= [::bar 123] (c/start-tag ::start-tag-test 123)))
  (t/is (nil? (c/start-tag ::invalid nil)))
  (remove-method c/start-tag ::start-tag-test))
  
(t/deftest stop-tag-test
  ;; these would be promises except clojurescript
  (let [p1 (atom nil)
        p2 (atom nil)]
  (defmethod c/stop-tag ::stop-tag-test [_ v] (reset! p1 v))
  (c/stop-tag ::stop-tag-test 123)
  (c/stop-tag ::invalid nil)
  (t/is (= 123 @p1))
  (t/is (nil? @p2))
  (remove-method c/start-tag ::stop-tag-test)))

(t/deftest start-key-test
  (let [p1 (atom nil)]
    (t/testing "untagged inputs are returned unchanged"
      (doseq [in [123 1.23 "abc" :abc [] {} {:a :b}]]
        (t/is (= in (c/start-key :foo in)))))
    (defmethod c/start-tag ::start-key-test [_ v] (reset! p1 (:a v)) {:sent :inel})
    (let [i1 {:co/tag ::start-key-test :a 123}
          r1 (c/start-key :b i1)]
      (t/testing "places meta"
        (t/is (= {::c/tag ::start-key-test} (meta r1))))
      (t/testing "result"
        (t/is (= {:sent :inel} r1))
        (t/is (= 123 @p1))))
    (remove-method c/start-tag ::start-key-test)))

(t/deftest stop-key-test
  (let [v1 ^{::c/tag ::stop-key-test} {:sent :inel}
        p1 (atom nil)]
    (defmethod c/stop-tag ::stop-key-test [_ v] (reset! p1 v))
    (t/testing "returns nil always"
      (doseq [in [123 1.23 "abc" :abc [] {} v1]]
        (t/is (nil? (c/stop-key :foo in)))))
    (t/testing "meta is followed"
      (t/is (= v1 @p1)))
    (remove-method c/stop-tag ::stop-key-test)))

(t/deftest start-test
  (let [c {:a (i/ref :b)
           :b {:sent :inel}
           :c {:a (i/ref :a) :co/tag ::start-test}}
        e1 {:a {:sent :inel} :b {:sent :inel} :c {:senti :nel}}
        e2 {:a {:sent :inel} :co/tag ::start-test}
        p1 (atom nil)]
    (defmethod c/start-tag ::start-test [_ v] (reset! p1 v) {:senti :nel})
    (t/is (= e1 (c/start! c)))
    (t/is (= e2 @p1))
    (remove-method c/start-tag ::start-test)))

(t/deftest stop-test
  (let [c ^{::i/origin {:a 123 :b {:sent :inel}}} {:a 123 :b ^{::c/tag ::foo} {:sent :inel}}
        p1 (atom nil)]
    (defmethod c/stop-tag ::foo [_ v] (reset! p1 v))
    (t/is (nil? (c/stop! c)))
    (t/is (= {:sent :inel} @p1))
    (remove-method c/stop-tag ::foo)))
