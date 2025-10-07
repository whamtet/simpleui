(ns demo.routes.test-datastar
  (:require
    [simpleui.core :as simpleui :refer [defcomponent make-routes-datastar]]
    [simpleui.middleware :as middleware]
    [simpleui.rt :as rt]
    [demo.middleware.formats :refer [page-datastar]]))

(defcomponent ^:endpoint subcomponent [req num]
  (let [num (if num (inc num) 0)]
    [:div {:id "counter"
           :data-signals-num num
           :data-on-click "@get('subcomponent')"}
     (format "You have clicked me %s times." num)]))

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
