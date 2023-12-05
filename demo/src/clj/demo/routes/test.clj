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

(defcomponent ^:endpoint subcomponent [req]
  [:div {:id id :hx-post "subcomponent" :hx-include "#extra"}
   [:input {:type "hidden" :name (path "extra") :value (value "extra")
            :id (if top-level? "result2" "extra")}]
   [:div#path-check (path ".")]
   [:div#hash-check (hash ".")]])

(defcomponent component [req]
  (subcomponent req))

(defn routes []
  (make-routes
    "/test"
    (fn [req]
      incrementer
      (page
        :zero-outer
        [:div
         (incrementer req)
         (component req)]))))
