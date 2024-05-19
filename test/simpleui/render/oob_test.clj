(ns simpleui.render.oob-test
  (:require
    [clojure.test :refer :all]
    [simpleui.render.oob :as oob]))

(defn- get-attr [[_ m] attr]
  (get m attr))
(defn- has-attr? [[_ m] attr]
  (and
   (map? m)
   (contains? m attr)))

(deftest oob-test
  (testing "oob assoc"
           (let [[a b c d e f]
                 (oob/assoc-oob
                  (list
                   [:div "should oob"]
                   [:div {:hx-post "my-endpoint"} "should also oob"]
                   [:div {:hx-swap-oob "other"} "should leave oob untouched"]
                   [:link "should not oob"]
                   [:script "should not oob"]
                   [:div "should not oob"]))]
             (is (= "true" (get-attr a :hx-swap-oob)))
             (is (= "true" (get-attr b :hx-swap-oob)))
             (is (= "other" (get-attr c :hx-swap-oob)))
             (is (not (has-attr? d :hx-swap-oob)))
             (is (not (has-attr? e :hx-swap-oob)))
             (is (not (has-attr? f :hx-swap-oob))))))
