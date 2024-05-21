(ns simpleui.render.oob)

(def insert-head? #{:script :link})
(defn- unimportant? [v]
  (and (vector? v) (-> v first insert-head?)))

(defn- map-with-last [f s]
  (let [i (->> s reverse (take-while unimportant?) count (- (count s) 1))]
    (map-indexed #(f (= i %1) %2) s)))

(defn- assoc-oob* [last-important? v]
  (if (and (vector? v) (not-empty v) (not last-important?))
    (let [[tag attrs & rest] v]
      (cond
        (insert-head? tag) [:div {:hx-swap-oob "beforeend:head"} v]
        (map? attrs) (update-in v [1 :hx-swap-oob] #(or % "true"))
        :else (vec (list* tag {:hx-swap-oob "true"} attrs rest))))
    v))

(defn assoc-oob [items]
  (map-with-last assoc-oob* items))