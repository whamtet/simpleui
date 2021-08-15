## Delete Row

This example shows how to implement a delete button that removes a table row upon completion.

```clojure
(def data
  [{:name "Joe Smith" :email "joe@smith.org"}
   {:name "Angie MacDowell" :email "angie@macdowell.org"}
   {:name "Fuqua Tarkenton" :email "fuqua@tarkenton.org"}
   {:name "Kim Yee"	:email "kim@yee.org"}])

(defcomponent ^:endpoint tr [{:keys [request-method]} i {:keys [name email]}]
  (if (= :delete request-method)
    ""
    [:tr
      [:td name]
      [:td email]
      [:td "Active"]
      [:td [:button.btn.btn-danger {:hx-delete "tr"} "Delete"]]]))

(make-routes
  "/demo"
  (fn [req]
    ;; page renders the hiccup and returns a ring response
    (page
      [:table.table.delete-row-example
        [:thead
          [:tr [:th "Name"] [:th "Email"] [:th "Status"] [:th]]]
        [:tbody {:hx-confirm "Are you sure?" :hx-target "closest tr" :hx-swap "outerHTML swap:0.5s"}
          (ctmx.rt/map-indexed tr req data)]])))
```

{% include serverless/examples/delete_row/demo.html %}

The table body has a **hx-confirm** attribute to confirm the delete action. 
It also set the target to be the **closest tr** that is, the closest table row, for all the buttons. 
The swap specification in **hx-swap** says to swap the entire target out and to wait 0.5 seconds after receiving a response. 
This is so that we can use the following CSS:

```
  tr.htmx-swapping {
      opacity: 0;
      transition: opacity 0.5s;
  }
```

{% include footer.html %}
{% include zero_outer.html %}
