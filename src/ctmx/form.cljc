(ns ctmx.form
  (:require
    [clojure.string :as string]
    [clojure.walk :as walk]))

(defn nest-params [params]
  (reduce
    (fn [m [k v]]
      (assoc-in m (-> k name (.split "_")) v))
    {}
    params))

(defn prune-params [m]
  (if (= 1 (count m))
    (cond
      (and (vector? m) (-> m peek coll?))
      (-> m peek recur)
      (and (map? m) (-> m first second coll?))
      (-> m first second recur)
      :else m)
    m))

(defn first-val [[s]]
  (#?(:clj Integer/parseInt :cljs js/Number) s))

(defn vectorize-map [m]
  (if (and
        (map? m)
        (not-empty m)
        (every? #(re-find #"^\d+$" %) (keys m)))
    (->> m (sort-by first-val) (mapv second))
    m))

(defn json-params [params]
  (->> params
       nest-params
       (walk/postwalk vectorize-map)
       walk/keywordize-keys))

(defn json-params-pruned [params]
  (-> params json-params prune-params))

(defn flatten-json
  ([m] (flatten-json [] {} m))
  ([stack done m]
   (reduce
     (fn [done [k v]]
       (let [stack (conj stack (name k))]
         (if (or (map? v) (vector? v))
           (flatten-json stack done v)
           (assoc done
             (->> stack (string/join "_") keyword) v))))
     done
     (if (map? m)
       m
       (map-indexed #(list (str %1) %2) m)))))

(defn apply-params [params f & args]
  (as-> params $
        (json-params $)
        (apply f $ args)
        (flatten-json $)))
