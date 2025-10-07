(ns simpleui.render-test
  (:require [clojure.test :refer :all]
            [simpleui.render :as render]
            [simpleui.response :as response]))

(deftest snippet-render
  (testing "snippet render"
           (is (-> nil render/snippet-response :status (= 204)))
           (is (-> :refresh render/snippet-response (= response/hx-refresh)))
           (is (-> {:hx-redirect "/ok"} render/snippet-response (= (response/hx-redirect "/ok"))))
           (is (-> {:body "hi"} render/snippet-response :body (= "hi")))
           (is (-> {:body [:div]} render/snippet-response (= {:status 200
                                                              :headers {"Content-Type" "text/html"}
                                                              :body "<div></div>"})))
           (is (-> {:hi "there"} render/snippet-response (= {:status 204
                                                             :headers {}
                                                             :body ""
                                                             :session {:hi "there"}})))
           (is (->> [:div {:hx-get "hi"}]
                    (render/snippet-response "http://prefix/")
                    :body
                    (= "<div hx-get=\"http://prefix/hi\"></div>")))
           (is (-> [:div {:_ ["hi" "there"]
                          :class ["class1" "class2"]
                          :style {:border "1px solid black"}
                          ;; todo - need to check these are actually rendered properly
                          ; :data-class {:hidden "$input == ''" :font-bold "$input == 1"}
                          ; :data-attr {:disabled "$input == ''" :title "$input"}
                          ; :data-signals {:input 1 :form {:input 2}}
                          :hx-vals {:hi "there"}}]
                   render/snippet-response
                   :body
                   (= "<div _=\"hi there\" class=\"class1 class2\" hx-vals=\"{&quot;hi&quot;:&quot;there&quot;}\" style=\"border: 1px solid black\"></div>")))))
