(ns demo.routes.test-offline
  (:require
    [simpleui.core :as simpleui :refer [defcomponent make-routes]]
    [simpleui.rt :as rt]
    [demo.middleware.formats :refer [page]]))

(defn routes []
  (make-routes
   "/test-offline"
   (fn [req]
     (page
      :zero-outer
      :non-minified
      [:div
       "ok"]))))
