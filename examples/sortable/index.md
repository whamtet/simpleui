## Sortable
In this example we show how to integrate the [Sortable](https://sortablejs.github.io/Sortable/) javascript library with htmx.

To begin we intialize the **Sortable** javascript library:

```javascript
  const sortable = htmx.find('#to-sort');
  new Sortable(sortable, {animation: 150, ghostClass: 'blue-background-class'});
```

We then trigger **POST** to **sortable** on the **end** event to persist changes (if necessary).

{% include serverless/examples/sortable/demo.html %}

```clojure

(defn- content [items]
  (list*
    [:div.htmx-indicator "Updating..."]
    (for [item items]
      [:div
        [:input {:type "hidden" :name "order" :value item}]
        "Item " item])))

(defcomponent ^:endpoint sortable [req ^:ints order]
  (if (not-empty order)
    (content order)
    [:form#to-sort {:hx-post "sortable" :hx-trigger "end"}
      (content (range 1 6))]))

(make-routes
  "/demo"
  (fn [req]
    ;; page renders the hiccup and returns a ring response
    (page
      (sortable req nil))))
```

{% include footer.html %}
{% include sortable.html %}
