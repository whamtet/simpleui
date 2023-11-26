(ns simpleui.render.oob)

(defn assoc-attr [v k value]
  (if (vector? v)
    (case (count v)
      0 v
      1 (conj v {k value})
      (let [[tag attrs & rest] v]
        (if (map? attrs)
          (assoc-in v [1 k] value)
          (vec
            (list* tag {k value} attrs rest)))))
    v))

(defn assoc-oob [items]
  (concat
    (->> items butlast (map #(assoc-attr % :hx-swap-oob true)))
    (take-last 1 items)))