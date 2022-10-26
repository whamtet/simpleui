(ns ctmx.core
  (:refer-clojure :exclude [ns-resolve])
  (:require
    [clojure.string :as string]
    [clojure.walk :as walk]
    [cljs.env :as env]
    [ctmx.form :as form]
    [ctmx.render :as render]
    [ctmx.rt :as rt]
    [ctmx.util :as util]))

(def parsers
  {:long `rt/parse-long
   :long-option `rt/parse-long-option
   :double `rt/parse-double
   :double-option `rt/parse-double-option
   :longs `rt/parse-longs
   :doubles `rt/parse-doubles
   :array `rt/parse-array
   :boolean `rt/parse-boolean
   :boolean-true `rt/parse-boolean-true
   :edn `rt/parse-edn
   :keyword `rt/parse-kw})

(defn sym->f [sym]
  (when-let [meta (meta sym)]
    (some (fn [[k f]]
            (when (meta k)
              f))
          parsers)))

(defn dissoc-parsers [m]
  (apply vary-meta m dissoc (keys parsers)))

(defn- symbol-or-as [s]
  (if (symbol? s)
    s
    (:as s)))
(defn- assoc-as [m]
  (if (and (map? m) (-> m :as not))
    (assoc m :as (gensym))
    m))

(def ^:private json?
  #(let [{:keys [json json-stack]} (meta %)]
     (or json json-stack)))
(def ^:private annotations #{:simple :json :path :json-stack})
(defn- some-annotation [arg]
  (->> arg meta keys (some annotations)))

(def ^:private middleware [:req :params :params-stack])
(defn- some-middleware [sym]
  (when-let [m (meta sym)]
    (some
      #(when-let [f (m %)] [% f])
      middleware)))

(defn- expand-params [arg]
  (when-let [symbol (symbol-or-as arg)]
    `(rt/get-value
       ~'params
       ~'json
       ~'stack
       ~(str symbol)
       ~(some-annotation arg))))

(defn- parse-args [args expanded]
  `(let [~@(for [sym (util/flatten-all args)
                 :let [f (sym->f sym)]
                 :when f
                 x [sym `(~f ~sym)]]
             x)]
     ~expanded))

(defn- make-f [n args expanded]
  (let [middleware (some-middleware n)
        r (-> args (get 0) symbol-or-as)]
    (case (count args)
      0 (throw (Exception. "zero args not supported"))
      1
      (if middleware
        `(fn ~args
           (let [stack# (:stack (rt/conj-stack ~(name n) ~r))
                 ~r (form/apply-middleware ~r ~middleware stack#)] ~expanded))
        `(fn ~args ~expanded))
      `(fn this#
         ([~'req]
          (let [{:keys [~'stack]} (rt/conj-stack ~(name n) ~'req)
                req# ~(if middleware `(form/apply-middleware ~'req ~middleware ~'stack) 'req)
                {:keys [~'params]} req#
                ~'json ~(when (some json? args) `(form/json-params ~'params))]
            (this#
              req#
              ~@(map expand-params (rest args)))))
         (~args ~(parse-args args expanded))))))

(defmacro update-params [req f & body]
  `(let [~req (update ~req :params ~f ~req)
         {:keys [~'params]} ~req
         ~'value (fn [p#] (-> p# ~'path keyword ~'params))] ~@body))

(defn- with-stack [n [req] body]
  (let [req (symbol-or-as req)]
    `(let [~'top-level? (-> ~req :stack empty?)
           {:keys [~'params ~'stack] :as ~req} (rt/conj-stack ~(name n) ~req)
           ~'id (rt/path "" ~'stack ".")
           ~'path (partial rt/path "" ~'stack)
           ~'hash (partial rt/path "#" ~'stack)
           ~'hash-find (partial rt/path-find "#" ~'stack)
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
       util/flatten-all
       (filter symbol?)
       distinct
       (mapv cljs-quote)))

(defmacro defn-parse [name args & body]
  `(defn ~name ~args
     ~(parse-args args `(do ~@body))))

(defmacro defcomponent [name args & body]
  (let [args (if (not-empty args)
               (update args 0 assoc-as)
               args)]
    `(def ~(vary-meta name assoc :syms (get-syms body))
       ~(->> body
             expand-parser-hints
             (with-stack name args)
             (make-f name args)))))
(defmacro defn-assets [name args & body]
  `(defn ~(vary-meta name assoc :syms (get-syms body))
     ~args
     ~@body))

(defn- mapmerge [f s]
  (apply merge (map f s)))

(defn ns-resolve-clj [ns sym]
  (when-let [v (clojure.core/ns-resolve ns sym)]
    (as-> (meta v) m
          (assoc m :ns-name (-> m :ns ns-name)))))

(defn namespaces []
  (:cljs.analyzer/namespaces @env/*compiler*))

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
  ([search sym]
   (extract-endpoints
     (if env/*compiler* (ns-name *ns*) *ns*)
     search
     sym
     #{}))
  ([ns search sym exclusions]
   (when-let [{:keys [ns ns-name name syms endpoint] :as m} (ns-resolve ns sym)]
     (let [exclusions (conj exclusions name)
           mappings (->> syms
                         (remove exclusions)
                         (mapmerge #(extract-endpoints ns search % exclusions)))
           v (if (= :ns-name search)
               (when endpoint ns-name)
               (search m))]
       (if v
         (assoc mappings name v)
         mappings)))))

(defn extract-endpoints-root [search f]
  (->> f
       util/flatten-all
       (filter symbol?)
       distinct
       (mapmerge (partial extract-endpoints search))))

(defn- compiled-str [s]
  (-> s name
      (.replace "-" "_")
      (.replace "?" "_QMARK_")
      (.replace "!" "_BANG_")))

(defn- full-symbol [ns-name name]
  (symbol (str ns-name) (str name)))

(defn extract-endpoints-all [f]
  (for [[name ns-name] (extract-endpoints-root :ns-name f)]
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
                             (for [[name ns-name] (extract-endpoints-root :ns-name body)]
                               [(str name)
                                (str (compiled-str ns-name) "." (compiled-str name))])))
     ~args ~@body))

(defmacro with-req [req & body]
  `(let [{:keys [~'request-method ~'session ~'params]} ~req
         ~'get? (= :get ~'request-method)
         ~'post? (= :post ~'request-method)
         ~'put? (= :put ~'request-method)
         ~'patch? (= :patch ~'request-method)
         ~'delete? (= :delete ~'request-method)]
     ~@body))

(defmacro metas [& syms]
  (mapv (fn [sym] `(meta (var ~sym))) syms))
