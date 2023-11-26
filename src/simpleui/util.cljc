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
