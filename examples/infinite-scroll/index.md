## Infinite Scroll

The infinite scroll pattern provides a way to load content dynamically on user scrolling action.

```clojure
(def src "0123456789ABCDEF")
(defn rand-str []
  (clojure.string/join (repeatedly 15 #(rand-nth src))))

(defn tr [i]
    [:tr
      (when (= 9 (mod i 10))
        {:hx-get "rows" :hx-trigger "revealed" :hx-swap "afterend" :hx-vals {:page (inc i)}})
      [:td "Agent Smith"]
      [:td (str "void" i "@null.org")]
      [:td (rand-str)]])

(defcomponent ^:endpoint rows [req ^:long page]
  (map tr (range page (+ 10 page))))

(make-routes
  "/demo"
  (fn [req]
    ;; page renders the hiccup and returns a ring response
    (page
      [:table
        [:thead
          [:tr [:th "Name"] [:th "Email"] [:th "ID"]]]
        [:tbody (rows req 0)]])))
```

{% include examples/infinite_scroll_handler.html %}
{% include footer.html %}
{% include zero_outer.html %}
