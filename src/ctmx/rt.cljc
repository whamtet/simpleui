(ns ctmx.rt
  (:refer-clojure :exclude [map-indexed])
  (:require
    #?(:cljs ctmx.form) ;; to ensure deps are pulled
    #?(:cljs cljs.reader)
    #?(:cljs ctmx.render)
    [clojure.string :as string]
    [ctmx.response :as response]))

(def parse-int #(if (string? %)
                  (#?(:clj Integer/parseInt :cljs js/Number) %)
                  %))
(def parse-float #(if (string? %)
                    (#?(:clj Float/parseFloat :cljs js/Number) %)
                    %))
(def parse-ints #(if (string? %)
                   [(parse-int %)]
                   (map parse-int %)))
(def parse-floats #(if (string? %)
                     [(parse-float %)]
                     (map parse-float %)))
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

(defn conj-stack [n {:keys [headers stack] :as req}]
  (assoc req
    :stack
    (if-let [target (and (empty? stack) (get headers "hx-target"))]
      (-> target (.split "_") vec)
      (if (-> stack peek number?)
        (-> stack pop (conj n (peek stack)))
        (conj (or stack []) n)))))

(def default-method :simple)
(defn set-param-method! [method]
  {:pre [(contains? #{:simple :path} method)]}
  #?(:clj (alter-var-root #'default-method (constantly method))
     :cljs (set! default-method method)))

(defn get-value [params json stack value method]
  (case (or method default-method)
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

(defn map-indexed [f req s]
  (clojure.core/map-indexed
    (fn [i x]
      (f (conj-stack i req) i x)) s))

(defn map-range
  ([f req i] (map-range f req 0 i))
  ([f req i j]
   (map #(f (conj-stack % req))
        (range
          (parse-int i) (parse-int j)))))

(defn redirect [path]
  (fn [req]
    (->> req
         :query-string
         (str path "?")
         response/redirect)))
