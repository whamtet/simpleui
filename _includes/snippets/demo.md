```clojure
(defcomponent ^:endpoint hello [req my-name]
  [:div#hello "Hello " my-name])

(make-routes
  "/demo"
  (fn [req]
    ;; page renders the hiccup and returns a ring response
    (page
      [:div
       [:label "What is your name?"]
       [:input {:name "my-name" :hx-patch "hello" :hx-target "#hello"}]
       (hello req "")])))

```