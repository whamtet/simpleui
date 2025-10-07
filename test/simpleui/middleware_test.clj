(ns simpleui.middleware-test
  (:require [clojure.test :refer :all]
            [simpleui.middleware :as middleware]))

(def req {:headers {"hx-current-url" "https://www.example.com?hi=there"}})

(deftest src-params-test
  (testing "src-params"
    (is
     (-> req
         ((middleware/wrap-src-params :src-params))
         (= {:hi "there"})))))

(def datastar-get {:params {:datastar "{\"input\":\"dd\"}"}})
(def datastar-post {:body-params {:input "dd"}})

(def expected-params {:input "dd"})

(deftest datastar-test
  (testing "get"
    (is
     (-> datastar-get
         ((middleware/wrap-datastar :params))
         (= expected-params))))
  (testing "post"
    (is
     (-> datastar-post
         ((middleware/wrap-datastar :params))
         (= expected-params)))))