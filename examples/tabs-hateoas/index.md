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
    ;; page renders the hiccup and returns a ring response
    (page
      (content req 1))))
```
{% include serverless/examples/tabs_hateoas/demo.html %}

## Tabs (Using Hyperscript)
Tabs are a good example of a static component.  
We can use [hyperscript](https://hyperscript.org/) instead of server requests for increased performance.

```clojure
(defn- static-tab [i]
  [:a {:class (if (= 1 i) "tab selected" "tab")
       :_ (str "on click take .selected from .tab then add .d-none to .tab-content then remove .d-none from #content" i)}
   "Tab " i])

(defn- static-content [i]
  [:div {:class (if (= 1 i) "tab-content" "tab-content d-none")
         :id (str "content" i)}
   "This is the content for tab " i])

(make-routes
  "/demo2"
  (fn [req]
    ;; page renders the hiccup and returns a ring response
    (page
      [:div
       [:div.tab-list
        (map static-tab (range 1 4))]
       (map static-content (range 1 4))])))
```
Try clicking the second set of tabs and notice the performance difference.
{% include serverless/examples/tabs_hateoas/demo2.html %}

{% include footer.html %}
{% include zero_outer.html %}
{% include hyperscript.html %}