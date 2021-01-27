(ns ctmx.rt
  (:refer-clojure :exclude [map-indexed])
  (:require
    [clojure.string :as string]
    [ctmx.response :as response]))

(def parse-int #(if (string? %)
                  (#?(:clj Integer/parseInt :cljs js/Number) %)
                  %))
(def parse-float #(if (string? %)
                    (#?(:clj Float/parseFloat :cljs js/Number) %)
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

(defn conj-stack [n {:keys [headers stack] :as req}]
  (assoc req
    :stack
    (if-let [target (and (empty? stack) (get headers "hx-target"))]
      (-> target (.split "_") vec)
      (if (-> stack peek number?)
        (-> stack pop (conj n (peek stack)))
        (conj (or stack []) n)))))

(defn get-value [params stack value]
  (->> value
       (conj stack)
       (string/join "_")
       keyword
       params))

(defn concat-stack [concat stack]
  (reduce
    (fn [stack x]
      (case x
        ".." (pop stack)
        "." stack
        (conj stack x)))
    stack
    concat))

(defn path [stack p]
  (string/join
    "_"
    (if (.startsWith p "/")
      (-> p (.split "/") rest)
      (-> p (.split "/") (concat-stack stack)))))
(defn path-hash [stack p]
  (str "#" (path stack p)))

(defn map-indexed [f req s]
  (clojure.core/map-indexed
    (fn [i x]
      (f (conj-stack i req) i x)) s))

(defn map-range [f req i]
  (->> i
       parse-int
       range
       (map #(f (conj-stack % req) %))))

(defn redirect [path]
  (fn [req]
    (->> req
         :query-string
         (str path "?")
         response/redirect)))
