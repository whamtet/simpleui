## Bulk Update

This demo shows how to implement a common pattern where rows are selected and then bulk updated. This is accomplished by putting a form around a table, with checkboxes in the table.

```clojure

(def data
  (atom
    [{:name "Joe Smith" :email "joe@smith.org" :status "Inactive"}
     {:name "Angie MacDowell" :email "angie@macdowell.org" :status "Inactive"}
     {:name "Fuqua Tarkenton" :email "fuqua@tarkenton.org" :status "Inactive"}
     {:name "Kim Yee"	:email "kim@yee.org"	:status "Inactive"}]))

(defn- set-status [status data i]
  (update data i assoc :status status))

(defn tr [ids action i {:keys [name email status]}]
  [:tr {:class (when (contains? ids i) action)}
    [:td [:input {:type "checkbox" :name "ids" :value i :checked (contains? ids i)}]]
    [:td name]
    [:td email]
    [:td status]])

(defcomponent ^:endpoint bulk-update [{:keys [request-method]} ^:ints ids status]
  (when (= :put request-method)
    (swap! data #(reduce (partial set-status status) % ids)))
  [:form {:id id}
    [:table
      [:thead
        [:tr
          [:th]
          [:th "Name"]
          [:th "Email"]
          [:th "Status"]]]
      [:tbody (map-indexed (partial tr (set ids) status) @data)]]
    [:a.btn.mmargin
      {:hx-put "bulk-update"
       :hx-vals (json {:status "Active"})
       :hx-target (hash ".")}
      "Activate"]
    [:a.btn.mmargin
      {:hx-put "bulk-update"
       :hx-vals (json {:status "Inactive"})
       :hx-target (hash ".")}
      "Deactivate"]])

(make-routes
  "/demo"
  (fn [req] (bulk-update req [] "")))
```

{% include serverless/examples/bulk_update/demo.html %}

We use the **.htmx-settling** class to flash the rows when they change status

```
  .htmx-settling tr.Inactive td {
    background: lightcoral;
  }
  .htmx-settling tr.Active td {
    background: darkseagreen;
  }
  tr td {
    transition: all 1.2s;
  }
```

{% include footer.html %}
{% include zero_outer.html %}
