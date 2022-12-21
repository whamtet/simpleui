## Progress Bar

This example shows how to implement a smoothly scrolling progress bar.
We start with an intial state with a button that issues a **POST** to **start** to begin the job:

```clojure
(defn- progress [width]
  [:div.progress
    [:div#pb.progress-bar {:style (str "width:" width "%")}]])

(defcomponent ^:endpoint start [req ^:double width]
  (let [width (if width (-> width (+ (rand 30)) (min 100)) 0)]
    (if (= width 100)
      [:div {:hx-target "this"}
        [:h3 "Complete"]
        (progress 100)
        [:button.btn {:hx-post "start"} "Restart"]]
      [:div {:hx-target "this"
             :hx-get "start"
             :hx-trigger "load delay:600ms"
             :hx-vals {:width width}}
        [:h3 "Running"]
        (progress width)])))

(make-routes
  "/demo"
  (fn [req]
    ;; page renders the hiccup and returns a ring response
    (page
      [:div {:style "height: 200px"}
        [:div {:hx-target "this"}
          [:h3 "Start Progress"]
          [:button.btn {:hx-post "start"} "Start Job"]]])))
```

{% include examples/progress_bar_handler.html %}
{% include footer.html %}
{% include outer.html %}
