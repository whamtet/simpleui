ctmx is an app development tool for fast product development and even faster page load times.

```clojure
(defcomponent ^:endpoint hello [req my-name]
  [:div#hello "Hello " my-name])

(make-routes
  "/demo"
  (fn [req]
    [:div
     [:label "What is your name?"]
     [:input {:name "my-name"
              :hx-patch "hello"
              :hx-target "#hello"}]
     (hello req "")]))

```
{% include serverless/functions/core/demo.html %}
---
Try inspecting the above text field.  You should see something like this.

![](inspect.png)

Now try editing the text.  When the input looses focus it submits a request to `/hello` and updates its contents.

The core of ctmx is the `defcomponent` macro which expands to both:

- An ordinary function
- A rest endpoint.  Arguments are bound based on the html `name` attribute.

`defcomponent` enables developers to quickly build rich user interfaces with *no* javascript.  All code is on the server backend and yet it feels the same as frontend code.

## Handling data flow

{% include serverless/functions/core/data_flow.html %}



```clojure
(defcomponent ^:endpoint form [req ^:path first-name ^:path last-name]
  [:form {:id id :hx-post "form"}
    [:input {:type "text" :name (path "first-name") :value first-name}] [:br]
    [:input {:type "text" :name (path "last-name") :value last-name}] [:br]
    (when (= ["Barry" "Crump"] [first-name last-name])
      [:div "A good keen man!"])
    [:input {:type "submit"}]])

(make-routes
  "/data-flow"
  (fn [req]
    (form req "Barry" "")))
```

ctmx maintains a call stack of nested components.  This makes it easy to label data without name clashes.  Try submitting the above form and then inspecting the browser network tab.

![](network.png)

`(path "first-name")` and `(path "last-name")` macroexpand to unique values which are automatically mapped to the function arguments.  We can use the `form` component multiple times on the page without worrying about a name clash.

## Transforming parameters to JSON

The component call stack helps maintain a natural relationship between stored data and the UI.


{% include footer.html %}
