(ns simpleui.rt
  (:refer-clojure :exclude [map-indexed parse-long parse-double parse-boolean])
  (:require
    [simpleui.config :as config]
    #?(:cljs simpleui.form) ;; to ensure deps are pulled
    #?(:cljs cljs.reader)
    #?(:cljs simpleui.render)
    #?(:clj [clojure.data.json :as json])
    [clojure.string :as string]
    [simpleui.response :as response]))

(def parse-trim #(if (string? %)
                   (-> % .trim not-empty)
                   %))
(def parse-long #(if (string? %)
                  (#?(:clj Long/parseLong :cljs js/Number) (.trim %))
                  %))
(def parse-double #(if (string? %)
                    (#?(:clj Double/parseDouble :cljs js/Number) (.trim %))
                    %))
(def parse-longs #(if (string? %)
                   [(parse-long %)]
                   (map parse-long %)))
(def parse-doubles #(if (string? %)
                     [(parse-double %)]
                     (map parse-double %)))

(def parse-long-option #(if (string? %)
                          (when-not (= "null" %)
                           (some-> % .trim not-empty #?(:clj Long/parseLong :cljs js/Number)))
                         %))
(def parse-double-option #(if (string? %)
                            (when-not (= "null" %)
                             (some-> % .trim not-empty #?(:clj Double/parseDouble :cljs js/Number)))
                           %))
(def parse-nullable #(when-not (#{"nil" "null" ""} %) %))

(defn parse-prompt [req x]
  (if (false? x)
    false
    (or x (get-in req [:headers "hx-prompt"]))))

#?(:clj (defn- key-fn [^String s]
          (if (re-find #"^\d+$" s)
            (Long/parseLong s)
            (keyword s))))
#?(:clj
   (defn- read-str [s] (json/read-str s :key-fn key-fn)))
(def parse-json #(if (string? %)
                   (#?(:clj read-str :cljs js/JSON.parse) %)
                   %))

(def parse-array #(if (string? %) [%] %))
(def parse-set #(set (parse-array %)))

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

(defn- merge-params [req i x extra]
  (update req :params merge x extra {:index i :i i}))

(defn map-indexed
  ([f req s] (map-indexed f req s {}))
  ([f req s extra]
   (clojure.core/map-indexed
    (fn [i x]
      (-> req (conj-stack i) (merge-params i x extra) f))
    s)))

(defmacro map-indexedm [f req s & syms]
  `(map-indexed
    ~f
    ~req
    ~s
    ~(zipmap (map keyword syms) syms)))

(defn redirect [req]
  (->> req
       :query-string
       (str (:uri req) "/?")
       response/redirect))
