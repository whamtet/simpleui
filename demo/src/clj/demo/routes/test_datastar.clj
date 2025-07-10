(ns demo.routes.test-datastar
  (:require
    [simpleui.core :as simpleui :refer [defcomponent make-routes-datastar]]
    [simpleui.rt :as rt]
    [demo.middleware.formats :refer [page-datastar]]))

(defcomponent ^:endpoint subcomponent [req]
  (if top-level?
    (prn (:params req))
    [:div {:id id
           :data-on-click "@get('relative-url')"} "hi"]))

(defcomponent my-component [req]
  (subcomponent req))

(defn routes []
  ["/test-datastar"
   (make-routes-datastar
    (fn [req]
      (page-datastar
       [:div
        (my-component req)])))])
