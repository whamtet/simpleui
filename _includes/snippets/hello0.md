```clojure
(defcomponent ^:endpoint hello [req my-name]
  [:div#hello "Hello " my-name])

(def routes
 (fn [req]
   ;; page renders html
   (page
     [:div {:style "padding: 10px"}
      [:label {:style "margin-right: 10px"}
       "What is your name?"]
      [:input {:type "text"
               :name "my-name"
               :hx-patch "hello"
               :hx-target "#hello"}]
       (hello req "")])))
```