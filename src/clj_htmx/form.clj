(ns clj-htmx.form
  (:require
    [clojure.walk :as walk]))

(defn nest-params [params]
  (reduce
    (fn [m [k v]]
      (assoc-in m (-> k name (.split "_")) v))
    {}
    params))

(defn prune-params [m]
  (if (= 1 (count m))
    (if (-> m first second map?)
      (-> m first second prune-params)
      m)
    m))

(defn vectorize-map [m]
  (if (and (map? m) (every? #(re-find #"^\d+$" %) (keys m)))
    (->> m (sort-by #(-> % first Integer/parseInt)) (mapv second))
    m))

(defn json-params [params]
  (->> params nest-params prune-params (walk/postwalk vectorize-map)))
