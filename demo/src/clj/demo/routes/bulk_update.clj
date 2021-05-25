(ns demo.routes.bulk-update
  (:require
    [ctmx.core :as ctmx :refer [defcomponent make-routes defn-parse]]
    [demo.middleware.formats :refer [page]]))

(def init-data
  [{:name "Joe Smith" :email "joe@smith.org" :status "Inactive"}
   {:name "Angie MacDowell" :email "angie@macdowell.org" :status "Inactive"}
   {:name "Fuqua Tarkenton" :email "fuqua@tarkenton.org" :status "Inactive"}
   {:name "Kim Yee"	:email "kim@yee.org"	:status "Inactive"}])

(defn- set-status [status data i]
  (update data i assoc :status status))

(defn-parse update-data [{:keys [^:edn data ^:ints ids status]} _]
  {:ids (set ids)
   :data (reduce (partial set-status status) data ids)
   :status status})

(defn tr [ids action i {:keys [name email status]}]
  [:tr {:class (when (contains? ids i) action)}
   [:td [:input {:type "checkbox" :name "ids" :value i :checked (contains? ids i)}]]
   [:td name]
   [:td email]
   [:td status]])

(defcomponent ^:endpoint ^{:params update-data} update-form [req ids ^:json data status]
  [:form {:id id :hx-target "this"}
   [:input {:type "hidden" :name "data" :value (pr-str data)}]
   [:table
    [:thead
     [:tr [:th] [:th "Name"] [:th "Email"] [:th "Status"]]]
    [:tbody (map-indexed (partial tr ids status) data)]]
   [:button.mmargin
    {:hx-put "update-form"
     :hx-vals {:status "Active"}}
    "Activate"]
   [:button.mmargin
    {:hx-put "update-form"
     :hx-vals {:status "Inactive"}}
    "Deactivate"]])

(defn routes []
  (make-routes
    "/bulk-update"
    (fn [req]
      (page
       :outer
       (update-form req #{} init-data nil)))))