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

(def ring-handler
  (fn [req]
    ;; page renders initial html
    (page
      (models req "Audi"))))
```