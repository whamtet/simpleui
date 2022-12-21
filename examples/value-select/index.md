## Value Select

In this example we show how to make the values in one select depend on the value selected in another select.
---
{% include examples/value_select_handler.html %}

```clojure
(def data
  {"Audi" ["A1" "A4" "A6"]
   "Toyota" ["Landcruiser" "Hiace" "Corolla"]
   "BMW" ["325i" "325ix" "X5"]})

(defn- select [m value options]
  [:select m
    (for [option options]
      [:option {:value option :selected (= value option)} option])])

(defcomponent ^:endpoint models [req make]
  (let [models (data make)]
    [:div {:id id :hx-target "this"}
      [:h3 "Pick a Make / Model"]
      [:div
        [:label.mr "Make"]
        (select {:name "make"
                 :hx-get "models"} make (keys data))]
      [:div
        [:label.mr "Model"]
        (select {} (first models) models)]]))

(make-routes
  "/demo"
  (fn [req]
    ;; page renders the hiccup and returns a ring response
    (page
      (models req "Audi"))))
```

{% include footer.html %}
{% include zero_outer.html %}