```clojure
(defcomponent table-row [req i first-name last-name]
  [:tr
   [:td first-name] [:td last-name]])

(defcomponent table [req]
  [:table
   (ctmx.rt/map-indexed
    table-row
    req
    [{:first-name "Matthew" :last-name "Molloy"}
     {:first-name "Chad" :last-name "Thomson"}])])

(def ring-handler
 (fn [req]
   ;; page renders initial html
   (page
     (table req))))
```