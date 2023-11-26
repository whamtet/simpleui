```clojure
(defn- content [items]
  (list*
    [:div.htmx-indicator "Updating..."]
    (for [item items]
      [:div
        [:input {:type "hidden" :name "order" :value item}]
        "Item " item])))

(defcomponent ^:endpoint sortable [req ^:ints order]
  [:form#to-sort {:hx-post "sortable" :hx-trigger "end"}
   (content (or order (range 1 6)))])

(def ring-handler
  (fn [req]
    ;; page renders initial html
    (page
      (sortable req nil))))
```