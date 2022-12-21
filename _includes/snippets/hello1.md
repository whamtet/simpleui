```clojure
(defcomponent ^:endpoint form [req ^:path first-name ^:path last-name]
  [:form {:id id :hx-post "form"}
   [:input {:type "text" :name (path "first-name") :value first-name}] [:br]
   [:input {:type "text" :name (path "last-name") :value last-name}] [:br]
   (when (= ["Barry" "Crump"] [first-name last-name])
         [:div "A good keen man!"])
   [:input {:type "submit"}]])

(def routes
 (fn [req]
   ;; page renders html
   (page
      (form req "Barry" ""))))
```