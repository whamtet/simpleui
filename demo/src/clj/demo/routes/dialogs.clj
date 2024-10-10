(ns demo.routes.dialogs
  (:require
    [simpleui.core :as simpleui :refer [defcomponent make-routes]]
    [demo.middleware.formats :refer [page]]))

(defcomponent ^:endpoint reply [{:keys [headers]}]
  [:div#response.mmargin "You entered " (headers "hx-prompt")])

(defn routes []
  (make-routes
    "/dialogs"
    (fn [req]
      reply ; to include in route expansion
      (page
        :zero-outer
       [:div
        [:button.btn.mb
         {:hx-post "reply"
          :hx-prompt "Enter a string"
          :hx-confirm "Are you sure?"
          :hx-target "#response"}
         "Prompt Submission"]
        [:div#response]]))))
