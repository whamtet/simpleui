```clojure
(defcomponent ^:endpoint hello [req my-name]
  [:div#hello "Hello " my-name])

(def ring-handler
 (fn [req]
   ;; page renders initial html
   (page
     [:form.hello {:hx-patch "hello" :hx-target "#hello"}
      [:label "What is your name?"]
      [:input.mr {:type "text" :name "my-name"}]
      [:input {:type "submit"}]
      (hello req "")])))
```