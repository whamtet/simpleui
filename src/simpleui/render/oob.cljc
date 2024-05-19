(ns simpleui.render.oob)

;; test this directly if publicised
(defn- assoc-attr [v k value]
  (if (and (vector? v) (not-empty v))
    (let [[tag attrs & rest] v]
      (cond
        (#{:script :link} tag) v
        (map? attrs) (update-in v [1 k] #(or % value))
        :else (vec (list* tag {k value} attrs rest))))
    v))

(defn assoc-oob [items]
  (concat
    (->> items butlast (map #(assoc-attr % :hx-swap-oob "true")))
    (take-last 1 items)))