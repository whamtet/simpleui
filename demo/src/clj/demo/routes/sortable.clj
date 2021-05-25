(ns demo.routes.sortable
  (:require
    [ctmx.core :as ctmx :refer [defcomponent make-routes defn-parse]]
    [demo.middleware.formats :refer [page]]))

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

(make-routes
 "/demo"
 (fn [req]
   (sortable req nil)))

(defn routes []
  (make-routes
    "/sortable"
    (fn [req]
      (page
        :sortable
       (sortable req nil)))))