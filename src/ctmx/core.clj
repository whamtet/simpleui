(ns ctmx.core
  (:refer-clojure :exclude [map-indexed ns-resolve])
  (:require
    [clojure.string :as string]
    [clojure.walk :as walk]
    [cljs.env :as env]
    cljs.analyzer.api
    [ctmx.render :as render]))

(def parse-int #(if (string? %) (Integer/parseInt %) %))
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

(defn with-stack [n [req] body]
  `(let [~'top-level? (empty? *stack*)]
     (binding [*stack* (conj-stack ~(name n) ~req)
               *params* (get-params ~req)]
       (let [~'id (path ".")
             ~'path path
             ~'hash path-hash
             ~'value (fn [p#] (-> p# path keyword *params*))]
         ~@body))))

(defn expand-parser-hint [x]
  (if-let [parser (sym->f x)]
    `(~parser ~(dissoc-parsers x))
    x))
(defn expand-parser-hints [x]
  (walk/prewalk expand-parser-hint x))

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
     ~(->> body
           expand-parser-hints
           (with-stack name args)
           (make-f name args))))

(defn- mapmerge [f s]
  (apply merge (map f s)))

(defn ns-resolve-clj [ns sym]
  (when-let [v (clojure.core/ns-resolve ns sym)]
    (as-> (meta v) m
          (assoc m :ns-name (-> m :ns ns-name)))))

(defn ns-resolve-cljs [ns sym]
  (when-let [{:keys [name syms] :as m} (cljs.analyzer.api/ns-resolve ns sym)]
    (let [[ns name] (-> name str (.split "/"))]
      (assoc m
        :name (symbol name)
        :syms (map second syms) ;;confusing
        :ns (symbol ns)
        :ns-name (symbol ns)))))

(defn ns-resolve [ns sym]
  ((if env/*compiler* ns-resolve-cljs ns-resolve-clj) ns sym))

(defn extract-endpoints
  ([sym]
   (extract-endpoints
     (if env/*compiler* (ns-name *ns*) *ns*)
     sym
     #{}))
  ([ns sym exclusions]
   (when-let [{:keys [ns ns-name name syms endpoint]} (ns-resolve ns sym)]
     (let [exclusions (conj exclusions name)
           mappings (->> syms
                         (remove exclusions)
                         (mapmerge #(extract-endpoints ns % exclusions)))]
       (if endpoint
         (assoc mappings name ns-name)
         mappings)))))

(defn extract-endpoints-root [f]
  (->> f
       flatten
       (filter symbol?)
       distinct
       (mapmerge extract-endpoints)))

(defn extract-endpoints-all [f]
  (for [[name ns-name] (extract-endpoints-root f)
        :let [full-symbol (symbol (str ns-name) (str name))]]
    [(str "/" name) `(fn [x#] (-> x# ~full-symbol render/snippet-response))]))

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
