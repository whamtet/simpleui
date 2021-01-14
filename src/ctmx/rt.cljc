(ns ctmx.rt
  (:refer-clojure :exclude [map-indexed])
  (:require
    [clojure.string :as string]
    [ctmx.response :as response]))

(def parse-int #(if (string? %)
                  (#?(:clj Integer/parseInt :cljs js/Number) %)
                  %))
(def parse-boolean
  #(case %
     true true
     false false
     (contains? #{"true" "on"} %)))
(def parse-boolean-true
  #(case %
     true true
     false false
     (not= "false" %)))

(def ^:dynamic *stack* [])
(def ^:dynamic *params* nil)

(defn get-params [req]
  (or *params* (:params req)))

(defn conj-stack [n req]
  (let [target (get-in req [:headers "hx-target"])]
    (if (and (empty? *stack*) target)
      (-> target (.split "_") vec)
      (conj *stack* n))))

(defn get-value [params stack value]
  (->> value
       (conj stack)
       (string/join "_")
       keyword
       params))

(defn concat-stack [concat]
  (reduce
    (fn [stack x]
      (case x
        ".." (pop stack)
        "." stack
        (conj stack x)))
    *stack*
    concat))

(defn path [p]
  (string/join
    "_"
    (if (.startsWith p "/")
      (-> p (.split "/") rest)
      (-> p (.split "/") concat-stack))))
(defn path-hash [p]
  (str "#" (path p)))

(defn map-indexed [f req s]
  (doall
    (clojure.core/map-indexed #(binding [*stack* (conj *stack* %1)] (f req %1 %2)) s)))

(defn map-range [f req i]
  (->> i
       parse-int
       range
       (map #(binding [*stack* (conj *stack* %)] (f req %)))
       doall))

(defn redirect [path]
  (fn [req]
    (->> req
         :query-string
         (str path "?")
         response/redirect)))
