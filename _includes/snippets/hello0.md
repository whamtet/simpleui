```clojure
(defcomponent ^:endpoint hello [req my-name]
  [:div#hello "Hello " my-name])

(def routes
 (fn [req]
   ;; page renders html
   (page
     [:form.hello {:hx-patch "hello" :hx-target "#hello"}
      [:label {:style "margin-right: 10px"}
       "What is your name?"]
      [:input.mr {:type "text" :name "my-name"}]
      [:input {:type "submit"}]
       (hello req "")])))
```