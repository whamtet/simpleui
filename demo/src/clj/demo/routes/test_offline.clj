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
      :offline
      [:div {:hx-ext "hx-offline"}
       [:div {:hx-get "https://www.google.com"}
        "hi"]]))))
