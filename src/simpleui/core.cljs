(ns simpleui.core
  (:require
    [simpleui.walk :as walk]
    [simpleui.form :as form]
    [simpleui.rt :as rt]
    [simpleui.util :as util]))

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

(defn symbol-or-as [s]
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

(defn- parse-args [args expanded]
  `(let [~@(for [sym (util/flatten-all args)
                 :let [f (sym->f sym)]
                 :when f
                 x [sym `(~f ~sym)]]
             x)]
     ~expanded))

(defn- make-f [n args expanded]
  (let [prebind (some-prebind n)
        r (-> args (get 0) symbol-or-as)]
    (case (count args)
      0 (throw (js/Error. "zero args not supported"))
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
         (~args ~(parse-args args expanded))))))

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

(defmacro defn-parse [name args & body]
  `(defn ~name ~args
     ~(parse-args args `(do ~@body))))

(defmacro defcomponent [name args & body]
  (let [args (if (not-empty args)
               (update args 0 assoc-as)
               args)]
    `(def ~name
       ~(->> body
             expand-parser-hints
             (with-stack name args)
             (make-f name args)))))

(defn strip-slash [root]
  (if (.endsWith root "/")
    (.substring root 0 (dec (count root)))
    root))
