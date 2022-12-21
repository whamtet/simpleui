```clojure
(defcomponent ^:endpoint reply [{:keys [headers]}]
  [:div#response.mmargin "You entered " (headers "hx-prompt")])

(def routes
  (fn [req]
    ;; page renders html
    (page
      reply
      [:div
        [:button.btn.mb
          {:hx-post "reply"
           :hx-prompt "Enter a string"
           :hx-confirm "Are you sure?"
           :hx-target "#response"}
          "Prompt Submission"]
          [:div#response]])))
```