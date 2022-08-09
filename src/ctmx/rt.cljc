(ns ctmx.rt
  (:refer-clojure :exclude [map-indexed parse-long parse-double parse-boolean])
  (:require
    [ctmx.config :as config]
    #?(:cljs ctmx.form) ;; to ensure deps are pulled
    #?(:cljs cljs.reader)
    #?(:cljs ctmx.render)
    [clojure.string :as string]
    [ctmx.response :as response]))

(def parse-long #(if (string? %)
                  (#?(:clj Long/parseLong :cljs js/Number) %)
                  %))
(def parse-double #(if (string? %)
                    (#?(:clj Double/parseDouble :cljs js/Number) %)
                    %))
(def parse-longs #(if (string? %)
                   [(parse-long %)]
                   (map parse-long %)))
(def parse-doubles #(if (string? %)
                     [(parse-double %)]
                     (map parse-double %)))
(def parse-array #(if (string? %) [%] %))
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

(def parse-edn #(if (string? %)
                  (#?(:clj read-string :cljs cljs.reader/read-string) %)
                  %))
(def parse-kw #(if (string? %) (keyword %) %))

(defn conj-stack [n {:keys [headers stack] :as req}]
  (let [stack (if (empty? stack)
                (if-let [target (get headers "hx-target")]
                  (-> target (.split "_") vec pop)
                  [])
                stack)]
    (assoc req
      :stack
      (if (-> stack peek number?)
        (-> stack pop (conj n (peek stack)))
        (conj stack n)))))

(defn get-value [params json stack value method]
  (case (or method config/default-param-method)
    :simple (-> value keyword params)
    :path (->> value (conj stack) (string/join "_") keyword params)
    :json (-> value keyword json)
    :json-stack (->> value
                     (conj stack)
                     (mapv keyword)
                     (get-in json))))

(defn concat-stack [concat stack]
  (reduce
    (fn [stack x]
      (case x
        ".." (pop stack)
        "." stack
        (conj stack x)))
    stack
    concat))

(defn path [prefix stack p]
  (str
    prefix
    (string/join
      "_"
      (if (.startsWith p "/")
        (-> p (.split "/") rest)
        (-> p (.split "/") (concat-stack stack))))))

(defn path-find
  ([prefix stack find]
   (path-find prefix stack find "."))
  ([prefix stack find p]
   (as-> stack $
         (reverse $)
         (drop-while #(not= find %) $)
         (reverse $)
         (vec $)
         (path prefix $ p))))

(defn map-indexed [f req s]
  (clojure.core/map-indexed
    (fn [i x]
      (f (conj-stack i req) i x)) s))

(defn map-range
  ([f req i] (map-range f req 0 i))
  ([f req i j]
   (map #(f (conj-stack % req))
        (range
          (parse-long i) (parse-long j)))))

(defn redirect [path]
  (fn [req]
    (->> req
         :query-string
         (str path "?")
         response/redirect)))
