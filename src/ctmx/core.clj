(ns ctmx.core
  (:refer-clojure :exclude [ns-resolve])
  (:require
    [clojure.string :as string]
    [clojure.walk :as walk]
    [cljs.env :as env]
    [ctmx.form :as form]
    [ctmx.render :as render]
    [ctmx.rt :as rt]))

(def parsers
  {:int `rt/parse-int
   :float `rt/parse-float
   :ints `rt/parse-ints
   :floats `rt/parse-floats
   :array `rt/parse-array
   :boolean `rt/parse-boolean
   :boolean-true `rt/parse-boolean-true
   :edn `rt/parse-edn})

(defn sym->f [sym]
  (when-let [meta (meta sym)]
    (some (fn [[k f]]
            (when (meta k)
              f))
          parsers)))

(defn dissoc-parsers [m]
  (apply vary-meta m dissoc (keys parsers)))

(defn- get-symbol [s]
  (if (symbol? s)
    s
    (do
      (-> s :as symbol? assert)
      (:as s))))
(defn- get-symbol-safe [s]
  (if (symbol? s)
    s
    (:as s)))
(defn- assoc-as [m]
  (if (and (map? m) (-> m :as not))
    (assoc m :as (gensym))
    m))

(def ^:private json? #(-> % meta :json))
(def ^:private annotations #{:simple :json :path})
(defn- some-annotation [arg]
  (->> arg meta keys (some annotations)))

(defn- expand-params [arg]
  (when-let [symbol (get-symbol-safe arg)]
    `(rt/get-value
       ~'params
       ~'json
       ~'stack
       ~(str symbol)
       ~(some-annotation arg))))

(defn- make-f [n args expanded]
  (let [pre-f (-> n meta :params)
        r (-> args (get 0) get-symbol-safe)]
    (case (count args)
      0 (throw (Exception. "zero args not supported"))
      1
      (if pre-f
        `(fn ~args
           (let [~r (update ~r :params form/apply-params ~pre-f ~r)] ~expanded))
        `(fn ~args ~expanded))
      `(fn this#
         ([~'req]
          (let [req# ~(if pre-f `(update ~'req :params form/apply-params ~pre-f ~'req) 'req)
                {:keys [~'params ~'stack]} (rt/conj-stack ~(name n) req#)
                ~'json ~(when (some json? args) `(form/json-params ~'params ~'stack))]
            (this#
              req#
              ~@(map expand-params (rest args)))))
         (~args
           (let [~@(for [sym (rest args)
                         :let [f (sym->f sym)]
                         :when f
                         x [sym `(~f ~sym)]]
                     x)]
             ~expanded))))))

(defmacro update-params [req f & body]
  `(let [~req (update ~req :params ~f ~req)
         {:keys [~'params]} ~req
         ~'value (fn [p#] (-> p# ~'path keyword ~'params))] ~@body))

(defn- with-stack [n [req] body]
  (let [req (get-symbol req)]
    `(let [~'top-level? (-> ~req :stack empty?)
           {:keys [~'params ~'stack] :as ~req} (rt/conj-stack ~(name n) ~req)
           ~'id (rt/path "" ~'stack ".")
           ~'path (partial rt/path "" ~'stack)
           ~'hash (partial rt/path "#" ~'stack)
           ~'value (fn [p#] (-> p# ~'path keyword ~'params))]
       ~@body)))

(defn expand-parser-hint [x]
  (if-let [parser (sym->f x)]
    `(~parser ~(dissoc-parsers x))
    x))
(defn expand-parser-hints [x]
  (walk/prewalk expand-parser-hint x))

(defn- cljs-quote [sym]
  (if env/*compiler* sym `(quote ~sym)))
(defn get-syms [body]
  (->> body
       flatten
       (filter symbol?)
       distinct
       (mapv cljs-quote)))

(defmacro defcomponent [name args & body]
  (let [args (if (not-empty args)
               (update args 0 assoc-as)
               args)]
    `(def ~(vary-meta name assoc :syms (get-syms body))
       ~(->> body
             expand-parser-hints
             (with-stack name args)
             (make-f name args)))))

(defn- mapmerge [f s]
  (apply merge (map f s)))

(defn ns-resolve-clj [ns sym]
  (when-let [v (clojure.core/ns-resolve ns sym)]
    (as-> (meta v) m
          (assoc m :ns-name (-> m :ns ns-name)))))

(defn ns-resolve-cljs [ns sym]
  ;; very, very hacky
  (let [all-info (:cljs.analyzer/namespaces @env/*compiler*)
        [prefix suffix] (.split (str sym) "/")
        sym-short (symbol (or suffix prefix))
        ns (if suffix
             (get-in all-info [ns :requires (symbol prefix)])
             (or
               (get-in all-info [ns :uses sym-short])
               ns))]
    (when-let [m (and ns (get-in all-info [ns :defs sym-short]))]
      (assoc m
        :name sym-short
        :ns ns
        :ns-name ns))))

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

(defn- compiled-str [s]
  (-> s name
      (.replace "-" "_")
      (.replace "?" "_QMARK_")
      (.replace "!" "_BANG_")))

(defn- full-symbol [ns-name name]
  (symbol (str ns-name) (str name)))

(defn extract-endpoints-all [f]
  (for [[name ns-name] (extract-endpoints-root f)]
    [(str "/" name) `(fn [x#] (-> x# ~(full-symbol ns-name name) render/snippet-response))]))

(defn strip-slash [root]
  (if (.endsWith root "/")
    [(.substring root 0 (dec (count root))) root]
    [root (str root "/")]))

(defmacro make-routes [root f]
  (let [[short full] (strip-slash root)]
    `[~short
      ["" {:get (rt/redirect ~full)}]
      ["/" {:get ~f}]
      ~@(extract-endpoints-all f)]))

(defmacro defstatic [sym args & body]
  `(defn
     ~(vary-meta sym assoc
                 :export true
                 :deps (into {}
                             (for [[name ns-name] (extract-endpoints-root body)]
                               [(str name)
                                (str (compiled-str ns-name) "." (compiled-str name))])))
     ~args ~@body))

(defmacro with-req [req & body]
  `(let [{:keys [~'request-method ~'session]} ~req
         ~'get? (= :get ~'request-method)
         ~'post? (= :post ~'request-method)
         ~'put? (= :put ~'request-method)
         ~'patch? (= :patch ~'request-method)
         ~'delete? (= :delete ~'request-method)]
     ~@body))

(defmacro metas [& syms]
  (mapv (fn [sym] `(meta (var ~sym))) syms))
