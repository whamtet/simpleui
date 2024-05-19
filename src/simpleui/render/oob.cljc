(ns simpleui.render.oob)

;; test this directly if publicised
(defn- assoc-oob* [v]
  (if (and (vector? v) (not-empty v))
    (let [[tag attrs & rest] v]
      (cond
        (#{:script :link} tag) [:div {:hx-swap-oob "beforeend:head"} v]
        (map? attrs) (update-in v [1 :hx-swap-oob] #(or % "true"))
        :else (vec (list* tag {:hx-swap-oob "true"} attrs rest))))
    v))

(defn assoc-oob [items]
  (concat
    (->> items butlast (map assoc-oob*))
    (take-last 1 items)))