## Active Search
This example actively searches a contacts database as the user enters text.

```clojure
(def data
  [{:name "Joe Smith" :email "joe@smith.org"}
   {:name "Angie MacDowell" :email "angie@macdowell.org"}
   {:name "Fuqua Tarkenton" :email "fuqua@tarkenton.org"}
   {:name "Kim Yee"	:email "kim@yee.org"}])

(defn tr [{:keys [name email]}]
  [:tr [:td name] [:td email]])

(defcomponent ^:endpoint active-search [req search]
  (let [search (.toLowerCase search)]
    (->> data
      (filter #(-> % :name .toLowerCase (.includes search)))
      (map tr))))

(make-routes
  "/demo"
  (fn [req]
    ;; page renders the hiccup and returns a ring response
    (page
      [:div
        [:h3 "Search Contacts"]
        [:input.mr
          {:type "text" :name "search" :placeholder "Search e.g. Joe"
           :hx-post "active-search" :hx-trigger "keyup changed delay:500ms"
           :hx-target "#search-results"}]
        [:span.htmx-indicator
            [:img {:src "../../bars.svg"}] " Searching..."]
        [:table.table
          [:thead
            [:tr [:th "Name"] [:th "Email"]]]
          [:tbody#search-results
            (active-search req "")]]])))
```

The input issues a **POST** to **rows** on the keyup event and sets the body of the table to be the resulting content.

We add the **delay:500ms** modifier to the trigger to delay sending the query until the user stops typing. 
Additionally, we add the **changed** modifier to the trigger to ensure we don't send new queries when the user doesn't change the value of the input (e.g. they hit an arrow key).

Finally, we show an indicator when the search is in flight with the **hx-indicator** attribute.

{% include serverless/examples/active_search/demo.html %}
{% include footer.html %}
{% include zero_inner.html %}