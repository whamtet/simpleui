(ns demo.routes.infinite-scroll
  (:require
    [simpleui.core :as simpleui :refer [defcomponent make-routes]]
    [demo.middleware.formats :refer [page]]))

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
   [:table
    [:thead
     [:tr [:th "Name"] [:th "Email"] [:th "ID"]]]
    [:tbody (rows req 0)]]))

(defn routes []
  (make-routes
    "/infinite-scroll"
    (fn [req]
      (page
        :zero-outer
        [:table
         [:thead
          [:tr [:th "Name"] [:th "Email"] [:th "ID"]]]
         [:tbody (rows req 0)]]))))
