(ns simpleui.core
  (:refer-clojure :exclude [ns-resolve])
  (:require
    [simpleui.walk :as walk]
    ;[cljs.env :as env]
    [simpleui.form :as form]
    [simpleui.middleware :as middleware]
    [simpleui.render :as render]
    [simpleui.rt :as rt]
    [simpleui.util :as util]
    [clojure.core :as c]))

(def parsers
  {:long `rt/parse-long
   :long-option `rt/parse-long-option
   :double `rt/parse-double
   :double-option `rt/parse-double-option
   :longs `rt/parse-longs
   :doubles `rt/parse-doubles
   :array `rt/parse-array
   :set `rt/parse-set
   :boolean `rt/parse-boolean
   :boolean-true `rt/parse-boolean-true
   :edn `rt/parse-edn
   :keyword `rt/parse-kw
   :nullable `rt/parse-nullable
   :trim `rt/parse-trim
   :json `rt/parse-json})

(defn sym->f [sym]
  (when-let [meta (meta sym)]
    (or
     (when (:prompt meta) :prompt)
     (some (fn [[k f]]
             (when (meta k)
               f))
           parsers))))

(defn dissoc-parsers [m]
  (apply vary-meta m dissoc (keys parsers)))

(defn symbol-or-as [s]
  (if (symbol? s)
    s
    (:as s)))
(defn keyword-or-as [s]
  (keyword
   (symbol-or-as s)))
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

(def ^:private prebind [:req :params :params-stack])
(defn- some-prebind [sym]
  (when-let [m (meta sym)]
    (some
      #(when-let [f (m %)] [% f])
      prebind)))

(defn- expand-params [arg]
  (when-let [symbol (symbol-or-as arg)]
    `(rt/get-value
       ~'params
       ~'json
       ~'stack
       ~(str symbol)
       ~(some-annotation arg))))

(defn- arg-pair [r]
  (fn [sym]
    (when-let [f (sym->f sym)]
      [sym
       (if (= :prompt f)
         `(rt/parse-prompt ~r ~sym)
         `(~f ~sym))])))
(defn- parse-args [n args expanded]
  `(let [~@(->> args util/flatten-all (mapcat (arg-pair n)))]
     ~expanded))

(defn- make-f [n args expanded]
  (let [prebind (some-prebind n)
        r (-> args (get 0) symbol-or-as)]
    (case (count args)
      0 (throw (Exception. "zero args not supported"))
      1
      (if prebind
        `(fn ~args
           (let [stack# (:stack (rt/conj-stack ~r ~(name n)))
                 ~r (form/apply-prebind ~r ~prebind stack#)] ~expanded))
        `(fn ~args ~expanded))
      `(fn this#
         ([~'req]
          (let [{:keys [~'stack]} (rt/conj-stack ~'req ~(name n))
                req# ~(if prebind `(form/apply-prebind ~'req ~prebind ~'stack) 'req)
                {:keys [~'params]} req#
                ~'json ~(when (some json? args) `(form/json-params ~'params))]
            (this#
              req#
              ~@(map expand-params (rest args)))))
         (~args ~(parse-args r args expanded))))))

(defn- with-stack [n [req] body]
  (let [req (symbol-or-as req)]
    `(let [~'top-level? (-> ~req :stack empty?)
           {:keys [~'params ~'stack] :as ~req} (rt/conj-stack ~req ~(name n))
           ~'id (rt/path "" ~'stack ".")
           ~'path (partial rt/path "" ~'stack)
           ~'hash (partial rt/path "#" ~'stack)
           ~'hash-find (partial rt/path-find "#" ~'stack)
           ~'value (fn [p#] (-> p# ~'path keyword ~'params))
           ~'self ~(name n)]
       ~@body)))

(defn expand-parser-hint [x]
  (if-let [parser (sym->f x)]
    `(~parser ~(dissoc-parsers x))
    x))
(defn expand-parser-hints [x]
  (walk/prewalk expand-parser-hint x))

(defn- cljs-quote [sym]
  (if false #_env/*compiler* sym `(quote ~sym)))
(defn get-syms [body]
  (->> body
       util/flatten-all
       (filter symbol?)
       distinct
       (mapv cljs-quote)))

(defmacro defcomponent
  {:clj-kondo/lint-as 'clojure.core/defn}
  [name args & body]
  (let [args (if (not-empty args)
               (update args 0 assoc-as)
               args)]
    `(def ~(vary-meta name assoc :syms (get-syms body)
                                 :arglist (mapv keyword-or-as args))
       ~(->> body
             expand-parser-hints
             (with-stack name args)
             (make-f name args)))))
(defmacro defn-assets
  {:clj-kondo/lint-as 'clojure.core/defn}
  [name args & body]
  `(defn ~(vary-meta name assoc :syms (get-syms body))
     ~args
     ~@body))

(defmacro defui
  {:clj-kondo/lint-as 'clojure.core/defn}
  [name args & body]
  `(defn
    ~(vary-meta name assoc :syms (get-syms body) :ui true)
    ~args
    ~@body))

(defn- mapmerge [f s]
  (apply merge (map f s)))

(defn ns-resolve-clj [ns sym]
  (when-let [v (clojure.core/ns-resolve ns sym)]
    (as-> (meta v) m
          (assoc m :ns-name (some-> m :ns ns-name)))))

(defn ns-resolve-cljs [ns sym]
  (let [all-info {} ;(:cljs.analyzer/namespaces @env/*compiler*)
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
  ((if false #_env/*compiler* ns-resolve-cljs ns-resolve-clj) ns sym))

(defn extract-endpoints
  ([sym]
   (extract-endpoints
     (if false #_env/*compiler* (ns-name *ns*) *ns*)
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

(defn- full-symbol [ns-name name]
  (symbol (str ns-name) (str name)))

(defn extract-ui
  ([syms]
   (mapmerge
    (fn [sym]
     (extract-ui
      (if false #_env/*compiler* (ns-name *ns*) *ns*)
      sym
      #{}))
    syms))
  ([ns sym exclusions]
   (when-let [{:keys [ns ns-name name syms ui arglists]} (ns-resolve ns sym)]
     (let [exclusions (conj exclusions name)
           mappings (->> syms
                         (remove exclusions)
                         (mapmerge #(extract-ui ns % exclusions)))]
       (if ui
         (assoc mappings
                (full-symbol ns-name name)
                (util/max-by count arglists))
         mappings)))))

(defmacro make-effects [broadcast-fn & starting-syms]
  {:pre [(every? symbol? starting-syms)]}
  (vec
   (for [[f arglist] (extract-ui starting-syms)]
     {:inputs (mapv keyword arglist)
      :handler `(fn [{{:keys ~arglist} :inputs}]
                 (~broadcast-fn
                  (~f ~@arglist)))})))

(defn extract-endpoints-root [f]
  (->> f
       util/flatten-all
       (filter symbol?)
       distinct
       (mapmerge extract-endpoints)))

(defn- compiled-str [s]
  (-> s name
      (.replace "-" "_")
      (.replace "?" "_QMARK_")
      (.replace "!" "_BANG_")))

(defn- extract-endpoints-all [prefix f extra-args]
  (let [extra-args (zipmap (map keyword extra-args) extra-args)]
    (for [[name ns-name] (extract-endpoints-root f)]
      [(str "/" name) `(fn [x#] (->> x# (merge ~extra-args) ~(full-symbol ns-name name) (render/snippet-response ~prefix x#)))])))

(defn strip-slash [root]
  (if (.endsWith root "/")
    (.substring root 0 (dec (count root)))
    root))

(defn make-routes-fn [root f extra-args]
  `(let [short# (strip-slash ~root)]
    [short#
      ["" {:get rt/redirect}]
      ["/" {:get ~f}]
      ~@(extract-endpoints-all "" f extra-args)]))

(defmacro make-routes-simple [prefix extra-args & starting-syms]
  `(do
     ~(vec starting-syms) ;; just to ensure they exist!
     ["" {:middleware [middleware/wrap-src-params]}
      ~(vec (extract-endpoints-all prefix starting-syms extra-args))]))

(defmacro make-routes
  ([root f] (make-routes-fn root f []))
  ([root extra-args f] (make-routes-fn root f extra-args)))

;; alternative approach
(defmacro defcheck
  {:clj-kondo/lint-as 'clj-kondo.lint-as/def-catch-all}
  [sym]
  (let [kw (->> sym str (re-find #"[a-z]+") keyword)]
    `(defn ~sym [req#] (-> req# :request-method (= ~kw)))))

(defcheck get?)
(defcheck post?)
(defcheck put?)
(defcheck patch?)
(defcheck delete?)

(defmacro metas [& syms]
  (mapv (fn [sym] `(meta (var ~sym))) syms))

(defmacro apply-component [f & args]
  (->> f
       resolve
       meta
       :arglist
       (map (constantly nil))
       (util/restcat args)
       (concat [f])))

(defmacro apply-component-map [f m & args]
  (let [m-sym (gensym)]
    `(let [~m-sym ~m]
       ~(->> f
             resolve
             meta
             :arglist
             (map #(list % m-sym))
             (util/restcat args)
             (concat [f])))))

(defn- base-assignments [req]
  (let [req (symbol-or-as req)]
    {'get? ['get? `(-> ~req :request-method (= :get))]
     'put? ['put? `(-> ~req :request-method (= :put))]
     'patch? ['patch? `(-> ~req :request-method (= :patch))]
     'post? ['post? `(-> ~req :request-method (= :post))]
     'delete? ['delete? `(-> ~req :request-method (= :delete))]}))
(defn- filter-symbol [s]
  (when-let [command (and (string? s) (second (.split s ":")))]
    (let [command-pred (symbol (str command "?"))
          pred `(= ~'command ~command)]
      [command-pred [command-pred pred]])))

(defmacro with-commands
  {:clj-kondo/ignore [:unresolved-symbol]}
  [req & body]
  (let [sym->assignment (->> body
                             (tree-seq coll? seq)
                             (map filter-symbol)
                             (into (base-assignments req)))]
    `(let [~@(->> body (tree-seq coll? seq) distinct (mapcat sym->assignment))]
       ~@body)))
