```clojure
(defcomponent ^:endpoint click-div [req ^:long num-clicks]
  [:form {:id id :hx-get "click-div" :hx-trigger "click"}
   [:input {:type "hidden" :name "num-clicks" :value (inc num-clicks)}]
   "You have clicked me " num-clicks " times!"])

(def routes
 (fn [req]
   ;; page renders html
   (page
      (click-div req 0))))
```