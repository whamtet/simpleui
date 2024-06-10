(ns simpleui.render.si-set-test
  (:require
    [clojure.test :refer :all]
    [simpleui.render.si-set :as si-set]))

(deftest si-set-test
  (testing "si-set"
    (assert (=
             {:hx-vals {:si-set "hi,there", :si-clear "old,man", :si-set-class "my-class"}}
             (si-set/promote-set {:si-set [:hi :there]
                                  :si-clear [:old :man]
                                  :si-set-class "my-class"})))))

(deftest si-set-test-form
  (testing "si-set-form"
    (assert (=
             [:form {} [:input {:type "hidden", :name "si-set", :value "hi,there"}] [:input {:type "hidden", :name "si-clear", :value "old,man"}] [:input {:type "hidden", :name "si-set-class", :value "my-class"}] '("ok")]
             (si-set/promote-set-form 
              [:form {:si-set [:hi :there]
                      :si-clear [:old :man]
                      :si-set-class "my-class"} "ok"])))))