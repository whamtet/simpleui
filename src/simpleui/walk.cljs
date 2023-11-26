(ns simpleui.walk)

(defn- walk
  "Meta preserving walk"
  [inner form]
  (cond
    (list? form) (with-meta (apply list (map inner form)) (meta form))
    (map-entry? form)
    (MapEntry. (inner (key form)) (inner (val form)) nil)
    (seq? form) (with-meta (doall (map inner form)) (meta form))
    (record? form)
    (with-meta (reduce (fn [r x] (conj r (inner x))) form form) (meta form))
    (coll? form) (with-meta (into (empty form) (map inner form)) (meta form))
    :else form))

(defn prewalk
  [f form]
  (walk (partial prewalk f) (f form)))

