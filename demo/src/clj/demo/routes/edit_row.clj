(ns demo.routes.edit-row
  (:require
    [ctmx.core :as ctmx :refer [defcomponent make-routes defn-parse]]
    ctmx.rt
    [demo.middleware.formats :refer [page]]))

(def data
  [{:name "Joe Smith" :email "joe@smith.org"}
   {:name "Angie MacDowell" :email "angie@macdowell.org"}
   {:name "Fuqua Tarkenton" :email "fuqua@tarkenton.org"}
   {:name "Kim Yee"	:email "kim@yee.org"}])

(defcomponent ^:endpoint demo-table-row [req index {:keys [name email]}]
  (let [edit-index ^:long (value "edit-index")]
    (if (= index edit-index)
      [:tr {:id id :hx-target "this"}
       [:td "editing"]]
      [:tr {:id id :hx-target "this"}
       [:td name]
       [:td email]
       [:td [:button.btn.btn-primary
             {:hx-get self} "Edit"]]])))

(defcomponent demo-table [req]
  [:table.table.delete-row-example
   [:thead
    [:tr [:th "Name"] [:th "Email"] [:th]]]
   [:tbody ;{:hx-confirm "Are you sure?" :hx-target "closest tr" :hx-swap "outerHTML swap:0.5s"}
    (ctmx.rt/map-indexed demo-table-row req data)]])

(defn routes []
  (make-routes
    "/edit-row"
    (fn [req]
      (page :zero-outer (demo-table req)))))
