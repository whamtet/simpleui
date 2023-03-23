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

(defn- digital? [[k]]
  (boolean (re-find #"^\d+$" k)))
(defn- key-value [[k]]
  (Long/parseLong k))
(defn- conjv [s x]
  (conj (or s []) x))

(defn vectorize-map [m]
  (if (map? m)
    (let [{digital-elements true normal-elements false} (group-by digital? m)
          normal-map (into {} normal-elements)
          digital-sorted (->> digital-elements (sort-by key-value) (map second))]
      (reduce
       (fn [m sorted-item]
         (reduce
          (fn [m [k v]]
            (update m k conjv v))
          m
          sorted-item))
       normal-map
       digital-sorted))
    m))

(defn json-params [params]
  (->> params
       nest-params
       (walk/postwalk vectorize-map)
       walk/keywordize-keys))

(defn json-params-pruned [params]
  (-> params json-params prune-params))

(defn- reduce-indexed [f state s]
  (reduce
    (fn [state [i x]]
      (f state i x))
    state
    (map-indexed list s)))

(declare flatten-json)
(defn flatten-json-vector [k stack done v]
  (reduce-indexed
    (fn [done i x]
      (let [stack (conj stack i (name k))]
        (cond
          (vector? x) (throw (IllegalStateException. "nested vectors unsupported"))
          (map? x) (flatten-json stack done x)
          :else
          (assoc done
            (->> stack (string/join "_") keyword) x))))
    done
    v))

(defn flatten-json
  ([m] (flatten-json [] {} m))
  ([stack done m]
   (reduce
     (fn [done [k v]]
       (if (vector? v)
         (flatten-json-vector k stack done v)
         (let [stack (conj stack (name k))]
           (if (map? v)
             (flatten-json stack done v)
             (assoc done
               (->> stack (string/join "_") keyword) v)))))
     done
     m)))

(defn- apply-params [params f req]
  (as-> params $
        (json-params $)
        (f $ req)
        (flatten-json $)))

(defn- apply-params-stack [params stack f req]
  (as-> params $
        (json-params $)
        (update-in $ (mapv keyword stack) f req)
        (flatten-json $)))

(defn apply-prebind [req [k f] stack]
  (case k
    :req (f req)
    :params (update req :params apply-params f req)
    :params-stack (update req :params apply-params-stack stack f req)))
