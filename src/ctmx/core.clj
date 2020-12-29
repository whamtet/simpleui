(ns ctmx.core
  (:refer-clojure :exclude [map-indexed])
  (:require
    [ctmx.form :as form]
    [ctmx.render :as render]
    [clojure.string :as string]
    [clojure.walk :as walk]))

(def parse-int #(if (string? %) (Integer/parseInt %) %))
(def read-strings
  #(if (string? %)
     (list (read-string %))
     (map read-string %)))
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

(def parsers
  {:int `parse-int
   :read `read-strings
   :boolean `parse-boolean
   :boolean-true `parse-boolean-true})

(defn sym->f [sym]
  (when-let [meta (meta sym)]
    (some (fn [[k f]]
            (when (meta k)
              f))
          parsers)))

(defn dissoc-parsers [m]
  (apply vary-meta m dissoc (keys parsers)))

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

(defn- make-f [n args expanded]
  (case (count args)
    0 (throw (Exception. "zero args not supported"))
    1 `(fn ~args ~expanded)
    `(fn this#
       (~(subvec args 0 1)
         (let [{:keys [~'params]} ~(args 0)
               ~'stack (conj-stack ~(name n) ~(args 0))]
           (this#
             ~(args 0)
             ~@(for [arg (rest args)]
                 `(get-value ~'params ~'stack ~(str arg))))))
       (~args
         (let [~@(for [sym (rest args)
                       :let [f (sym->f sym)]
                       :when f
                       x [sym `(~f ~sym)]]
                   x)]
           ~expanded)))))

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

(defn expand-parser-hint [x]
  (if-let [parser (sym->f x)]
    `(~parser ~(dissoc-parsers x))
    x))

(defn with-stack [n [req] body]
  `(let [~'top-level? (empty? *stack*)]
     (binding [*stack* (conj-stack ~(name n) ~req)
               *params* (get-params ~req)]
       (let [~'id (path ".")
             ~'path path
             ~'hash path-hash
             ~'value (fn [p#] (-> p# path keyword *params*))]
         ~@(walk/prewalk expand-parser-hint body)))))

(defmacro update-params [f & body]
  `(binding [*params* (~f *params*)] ~@body))

(defn map-indexed [f req s]
  (doall
    (clojure.core/map-indexed #(binding [*stack* (conj *stack* %1)] (f req %1 %2)) s)))

(defn map-range [f req i]
  (->> i
       parse-int
       range
       (map #(binding [*stack* (conj *stack* %)] (f req %)))
       doall))

(defmacro forall [& args]
  `(doall (for ~@args)))

(defn get-syms [body]
  (->> body
       flatten
       (filter symbol?)
       distinct
       (mapv #(list 'quote %))))

(defmacro defcomponent [name args & body]
  `(def ~(vary-meta name assoc :syms (get-syms body))
     ~(make-f name args (with-stack name args body))))

(defn- mapmerge [f s]
  (apply merge (map f s)))

(defn extract-endpoints
  ([sym] (extract-endpoints *ns* sym #{}))
  ([ns sym exclusions]
   (when-let [v (ns-resolve ns sym)]
     (let [{:keys [name endpoint syms ns]} (meta v)
           exclusions (conj exclusions name)
           mappings (->> syms
                         (remove exclusions)
                         (mapmerge #(extract-endpoints ns % exclusions)))]
       (if endpoint
         (assoc mappings name v)
         mappings)))))

(defn extract-endpoints-root [f]
  (->> f
       flatten
       (filter symbol?)
       distinct
       (mapmerge extract-endpoints)))

(defn extract-endpoints-all [f]
  (for [[sym v] (extract-endpoints-root f)
        :let [full-symbol (symbol (-> v .ns ns-name str) (str sym))]]
    [(str "/" sym) `(fn [x#] (-> x# ~full-symbol render/snippet-response))]))

(defmacro make-routes [root f]
  `[[~root {:get ~f}]
    ~@(extract-endpoints-all f)])

(defmacro with-req [req & body]
  `(let [{:keys [~'request-method]} ~req
         ~'get? (= :get ~'request-method)
         ~'post? (= :post ~'request-method)
         ~'put? (= :put ~'request-method)
         ~'patch? (= :patch ~'request-method)
         ~'delete? (= :delete ~'request-method)]
     ~@body))
