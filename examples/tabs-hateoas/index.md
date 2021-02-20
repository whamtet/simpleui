## Tabs (Using HATEOAS)

This example shows how easy it is to implement tabs using ctmx. 
Following the principle of [Hypertext As The Engine Of Application State](https://en.wikipedia.org/wiki/HATEOAS),
the selected tab is a part of the application state. 
Therefore, to display and select tabs in your application, simply include the tab markup in the returned HTML.

```clojure
(defn- tab [i val]
  [:a {:hx-get "content"
       :hx-vals (json {:tab-index i})
       :class (when (= i val) "selected")}
    "Tab " i])

(defcomponent ^:endpoint content [req ^:int tab-index]
  [:div {:hx-target "this"}
    [:div.tab-list
      (map #(tab % tab-index) (range 1 4))]
    [:div.tab-content
      "This is the content for tab " tab-index]])

(make-routes
  "/demo"
  (fn [req]
    (content req 1)))
```

{% include serverless/examples/tabs_hateoas/demo.html %}
{% include footer.html %}
{% include zero_outer.html %}