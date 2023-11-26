(ns simpleui.walk)

(defmacro preserve-meta [f]
  `(with-meta ~f (meta ~'form)))
(defmacro preserve-meta-safe [f]
  `(if (instance? clojure.lang.IObj ~'form)
    (with-meta ~f (meta ~'form))
    ~f))

(defn- walk
  "Meta preserving walk"
  [inner form]
  (cond
    (list? form) (preserve-meta (apply list (map inner form)))
    (instance? clojure.lang.IMapEntry form)
    (clojure.lang.MapEntry/create (inner (key form)) (inner (val form)))
    (seq? form) (preserve-meta (doall (map inner form)))
    (instance? clojure.lang.IRecord form)
    (preserve-meta (reduce (fn [r x] (conj r (inner x))) form form))
    (coll? form) (preserve-meta-safe (into (empty form) (map inner form)))
    :else form))

(defn prewalk
  [f form]
  (walk (partial prewalk f) (f form)))

