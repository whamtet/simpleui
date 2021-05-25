(ns demo.routes.value-select
  (:require
    [ctmx.core :as ctmx :refer [defcomponent make-routes defn-parse]]
    [demo.middleware.formats :refer [page]]))

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

(defn routes []
  (make-routes
    "/value-select"
    (fn [req]
      (page
        :zero-outer
        (models req "Audi")))))