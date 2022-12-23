```clojure
(defn- tab [i val]
  [:a {:hx-get "content"
       :hx-vals {:tab-index i}
       :class (when (= i val) "selected")}
    "Tab " i])

(defcomponent ^:endpoint content [req ^:long tab-index]
  [:div {:hx-target "this"}
    [:div.tab-list
      (map #(tab % tab-index) (range 1 4))]
    [:div.tab-content
      "This is the content for tab " tab-index]])

(def ring-handler
  (fn [req]
    ;; page renders initial html
    (page
      (content req 1))))
```