(ns demo.routes.test
  (:require
    [simpleui.core :as simpleui :refer [defcomponent make-routes defn-parse]]
    [demo.middleware.formats :refer [page]]))

(defcomponent ^:endpoint incrementer [req ^:long num]
  (if num
    (inc num)
    [:div#incrementer
     {:hx-post "incrementer"
      :hx-vals {:num 0}} 0]))

(defn routes []
  (make-routes
    "/test"
    (fn [req]
      start ; include in route expansion
      (page
        :zero-inner
        :notify
        [:div {:hx-ext "htmx-notify"}
         (incrementer req)]))))
