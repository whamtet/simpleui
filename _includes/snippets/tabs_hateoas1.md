```clojure
(defn- static-tab [i]
  [:a {:class (if (= 1 i) "tab selected" "tab")
       :_ (str "on click take .selected from .tab then add .d-none to .tab-content then remove .d-none from #content" i)}
   "Tab " i])

(defn- static-content [i]
  [:div {:class (if (= 1 i) "tab-content" "tab-content d-none")
         :id (str "content" i)}
   "This is the content for tab " i])

(def routes
  (fn [req]
    ;; page renders html
    (page
      [:div
       [:div.tab-list
        (map static-tab (range 1 4))]
         (map static-content (range 1 4))])))
```