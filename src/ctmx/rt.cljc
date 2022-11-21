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

(def parse-long-option #(if (string? %)
                  (when (-> % .trim not-empty)
                    (#?(:clj Long/parseLong :cljs js/Number) %))
                  %))
(def parse-double-option #(if (string? %)
                  (when (-> % .trim not-empty)
                    (#?(:clj Double/parseDouble :cljs js/Number) %))
                    %))

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

(defn conj-stack [{:keys [headers stack] :as req} n]
  (let [stack (if (empty? stack)
                (if-let [target (get headers "hx-target")]
                  (-> target (.split "_") vec pop)
                  [])
                stack)]
    (assoc req :stack (conj stack n))))

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
      (if (.startsWith p "\\")
        (-> p (.split "\\\\") rest)
        (-> p (.split "\\\\") (concat-stack stack))))))

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

(defn- merge-params [req i x]
  (update req :params merge x {:index i}))

(defn map-indexed [f req s]
  (clojure.core/map-indexed
    (fn [i x]
      (-> req (conj-stack i) (merge-params i x) f))
    s))

(defn redirect [path]
  (fn [req]
    (->> req
         :query-string
         (str path "?")
         response/redirect)))
