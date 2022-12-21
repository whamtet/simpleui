```clojure
(defn- content [items]
  (list*
    [:div.htmx-indicator "Updating..."]
    (for [item items]
      [:div
        [:input {:type "hidden" :name "order" :value item}]
        "Item " item])))

(defcomponent ^:endpoint sortable [req ^:ints order]
  (if (not-empty order)
    (content order)
    [:form#to-sort {:hx-post "sortable" :hx-trigger "end"}
      (content (range 1 6))]))

(def routes
  (fn [req]
    ;; page renders html
    (page
        (sortable req nil))))
```