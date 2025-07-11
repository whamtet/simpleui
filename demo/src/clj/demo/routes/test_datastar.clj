(ns demo.routes.test-datastar
  (:require
    [simpleui.core :as simpleui :refer [defcomponent make-routes-datastar]]
    [simpleui.middleware :as middleware]
    [simpleui.rt :as rt]
    [demo.middleware.formats :refer [page-datastar]]))

(defcomponent ^:endpoint subcomponent [req]
  (if top-level?
    (prn (:params req))
    [:div {:id id
           :data-signals-foo 1
           :data-on-click "@get('subcomponent')"} "hi"]))

(defcomponent my-component [req]
  (subcomponent req))

(defn routes []
  ["/test-datastar"
   {:middleware [middleware/wrap-datastar]}
   (make-routes-datastar
    (fn [req]
      (page-datastar
       [:div
        (my-component req)])))])
