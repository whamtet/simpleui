(ns demo.routes.test
  (:require
    [simpleui.core :as simpleui :refer [defcomponent make-routes]]
    [simpleui.rt :as rt]
    [demo.middleware.formats :refer [page]]))

(defcomponent ^:endpoint incrementer [req ^:long num]
  (if num
    [:div#result (inc num)]
    [:div#incrementer
     {:hx-post "incrementer"
      :hx-vals {:num 0}} 0]))

(defcomponent ^:endpoint subcomponent [req i index first-name]
  [:div {:id id :hx-post "subcomponent" :hx-include "#extra"}
   [:input {:type "hidden" :name (path "extra") :value (or (value "extra") first-name)
            :id (if top-level? "result2" "extra")}]
   [:div#i-check i]
   [:div#index-check index]
   [:div#path-check (path ".")]
   [:div#hash-check (hash ".")]])

(defcomponent component [req]
  [:div
   (rt/map-indexed subcomponent req [{:first-name "Matt"}])])

(defcomponent ^:endpoint command-test [req command]
  (if command
    [:div#result3 command]
    [:div#command-test {:hx-post "command-test:fuck"}
     "woah"]))

(defn routes []
  (make-routes
    "/test"
    (fn [req]
      incrementer
      (page
        :zero-outer
        [:div
         (incrementer req)
         (component req)
         (command-test req)]))))
