ctmx is an app development tool for fast product development and even faster page load times.

```clojure
(defcomponent ^:endpoint hello [req my-name]
  [:div#hello "Hello " my-name])

(make-routes
  "/demo"
  (fn [req]
    [:div {:style "padding: 10px"}
     [:label {:style "margin-right: 10px"}
      "What is your name?"]
     [:input {:type "text"
              :name "my-name"
              :hx-patch "hello"
              :hx-target "#hello"}]
     (hello req "")]))

```
{% include serverless/functions/core/demo.html %}
{% include footer.html %}
---
Try inspecting the above text field.  You should see something like this.

![](inspect.png)

Now try editing the text.  When the input looses focus it submits a request to `/hello` and updates its contents.

The core of ctmx is the `defcomponent` macro which expands to both an ordinary function and a REST endpoint.  `defcomponent` enables developers to quickly build rich user interfaces with *no* javascript.  All code is on the server backend and yet it feels the same as frontend code.

