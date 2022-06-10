## Click To Load

This example shows how to implement click-to-load the next page in a table of data. The crux of the demo is the final row:

```clojure
(def src "0123456789ABCDEF")
(defn rand-str []
  (clojure.string/join (repeatedly 15 #(rand-nth src))))

(defn tr [i]
    [:tr [:td "Agent Smith"] [:td (str "void" i "@null.org")] [:td (rand-str)]])

(defcomponent ^:endpoint rows-click [req ^:int page]
  (list
    (map tr (range (* 10 page) (* 10 (inc page))))
    [:tr {:id id :hx-target "this"}
      [:td {:colspan "3"}
        [:button.btn
          {:hx-get "rows-click"
           :hx-vals {:page (inc page)}}
           "Load More Agents..."
           [:img.htmx-indicator {:src "../../bars.svg"}]]]]))

(make-routes
  "/demo"
  (fn [req]
    ;; page renders the hiccup and returns a ring response
    (page
      [:table
        [:thead
          [:tr [:th "Name"] [:th "Email"] [:th "ID"]]]
        [:tbody (rows-click req 0)]])))

```

{% include serverless/examples/click_to_load/demo.html %}

{% include footer.html %}
{% include outer.html %}
