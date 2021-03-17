(ns ctmx.core-test
  (:require [clojure.test :refer :all]
            [ctmx.core :as ctmx :refer [defcomponent]]))

(def css "css")

(defcomponent ^:endpoint ^{:css css} a [req]
  [:div])
(defcomponent b [req]
  (a req))

(prn
  (ctmx/extract-css 'b))

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 1 1))))
