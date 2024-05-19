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
