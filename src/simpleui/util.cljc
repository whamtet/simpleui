(ns simpleui.util)

(defn flatten-all [m] (->> m (tree-seq coll? seq) (remove coll?)))

(defn filter-vals
  ([m]
   (into {}
         (for [[k v] m :when v]
           [k v])))
  ([f m]
   (into {}
         (for [[k v] m :when (f v)]
           [k v]))))

(defn max-by [f [x & xs]]
  (first
   (reduce
    (fn [[x1 y1] x2]
      (let [y2 (f x2)]
        (if (pos? (compare y1 y2))
          [x1 y1]
          [x2 y2])))
    [x (f x)]
    xs)))

(defn restcat [a b]
  (->> b (drop (count a)) (concat a)))