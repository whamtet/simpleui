(ns demo.routes.click-to-load
  (:require
    [simpleui.core :as simpleui :refer [defcomponent make-routes]]
    [demo.middleware.formats :refer [page]]))

(def src "0123456789ABCDEF")
(defn rand-str []
  (clojure.string/join (repeatedly 15 #(rand-nth src))))

(defn tr [i]
  [:tr [:td "Agent Smith"] [:td (str "void" i "@null.org")] [:td (rand-str)]])

(defcomponent ^:endpoint rows-click [req ^:long page]
  (list
   (map tr (range (* 10 page) (* 10 (inc page))))
   [:tr {:id id :hx-target "this"}
    [:td {:colspan "3"}
     [:button.btn
      {:hx-get "rows-click"
       :hx-vals {:page (inc page)}}
      "Load More Agents..."
      [:img.htmx-indicator {:src "/img/bars.svg"}]]]]))

(defn routes []
  (make-routes
    "/click-to-load"
     (fn [req]
       (page
        :outer
        [:table
          [:thead
            [:tr [:th "Name"] [:th "Email"] [:th "ID"]]]
          [:tbody (rows-click req 0)]]))))
