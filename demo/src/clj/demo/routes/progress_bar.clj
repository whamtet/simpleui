(ns demo.routes.progress-bar
  (:require
    [simpleui.core :as simpleui :refer [defcomponent make-routes defn-parse]]
    [demo.middleware.formats :refer [page]]))

(defn- progress [width]
  [:div.progress
   [:div#pb.progress-bar {:style (str "width:" width "%")}]])

(defcomponent ^:endpoint start [req ^:float width]
  (let [width (if width (-> width (+ (rand 30)) (min 100)) 0)]
    (if (= width 100)
      [:div {:hx-target "this"}
       [:h3 "Complete"]
       (progress 100)
       [:button.btn {:hx-post "start"} "Restart"]]
      [:div {:hx-target "this"
             :hx-get "start"
             :hx-trigger "load delay:600ms"
             :hx-vals {:width width}}
       [:h3 "Running"]
       (progress width)])))

(defn routes []
  (make-routes
    "/progress-bar"
    (fn [req]
      start ; include in route expansion
      (page
        :outer
        [:div {:style "height: 200px"}
          [:div {:hx-target "this"}
            [:h3 "Start Progress"]
            [:button.btn {:hx-post "start"} "Start Job"]]]))))