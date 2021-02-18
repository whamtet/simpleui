CTMX is an app development tool for fast product development and even faster page load times.

```clojure
(defcomponent ^:endpoint hello [req my-name]
  [:div#hello "Hello " my-name])

(make-routes
  "/demo"
  (fn [req]
    (render/html5-response
      [:div {:style "padding: 10px"}
        [:label {:style "margin-right: 10px"}
          "What is your name?"]
        [:input {:type "text"
                 :name "my-name"
                 :hx-patch "hello"
                 :hx-target "#hello"
                 :hx-swap "outerHTML"}]
         (hello req "")])))
```
{% include serverless/functions/core/demo.html %}
{% include footer.html %}
