(ns demo.routes.test
  (:require
    [simpleui.core :as simpleui :refer [defcomponent make-routes defn-parse]]
    [demo.middleware.formats :refer [page]]))

(defcomponent ^:endpoint incrementer [req ^:long num]
  (if num
    [:div#result (inc num)]
    [:div#incrementer
     {:hx-post "incrementer"
      :hx-vals {:num 0}} 0]))

(defn routes []
  (make-routes
    "/test"
    (fn [req]
      incrementer
      (page
        :zero-inner
        [:div {:hx-ext "htmx-notify"}
         (incrementer req)]))))
