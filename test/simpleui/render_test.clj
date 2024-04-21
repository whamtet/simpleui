(ns simpleui.render-test
  (:require [clojure.test :refer :all]
            [simpleui.render :as render]))

(deftest snippet-render
  (testing "snippet render"
           (is (-> nil render/snippet-response :status (= 204)))
           (is (-> {:body "hi"} render/snippet-response :body (= "hi")))
           (is (-> {:body [:div]} render/snippet-response (= {:status 200
                                                              :headers {"Content-Type" "text/html"}
                                                              :body "<div></div>"})))
           (is (-> [:div {:_ ["hi" "there"]
                          :class ["class1" "class2"]
                          :style {:border "1px solid black"}
                          :hx-vals {:hi "there"}}]
                   render/snippet-response
                   :body
                   (= "<div _=\"hi there\" class=\"class1 class2\" hx-vals=\"{&quot;hi&quot;:&quot;there&quot;}\" style=\"border: 1px solid black\"></div>")))))
