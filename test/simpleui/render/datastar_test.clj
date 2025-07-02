(ns simpleui.render.datastar-test
  (:require
    [clojure.test :refer :all]
    [simpleui.render.datastar :as datastar]
    [simpleui.response :as response]))

(defn snippet-response [x]
  (->> x
       (datastar/snippet-response-datastar "prefix")
       :body))

(deftest datastar-test
  (testing "empty body"
           (is (= response/no-content
                  (datastar/snippet-response-datastar "prefix" nil))))
  (testing "plain render"
           (is
            (=
             "event: datastar-merge-fragments\ndata: fragments <div>hi</div>"
             (snippet-response [:div "hi"]))))
  (testing "seq render"
           (is
            (prn
             (snippet-response
              (list
                [:div "hi"]))))))
