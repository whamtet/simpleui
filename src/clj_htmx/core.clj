(ns clj-htmx.core
  (:require
    [clj-htmx.form :as form]
    [clj-htmx.render :as render]
    [clojure.string :as string]
    [clojure.walk :as walk]))

(defn- component-macro? [x]
  (and
    (symbol? x)
    (resolve x)
    (let [evaluated (-> x resolve var-get)]
      (and
        (map? evaluated)
        (contains? evaluated :fn)
        (contains? evaluated :endpoints)))))

(defn expand-components [x]
  (if (component-macro? x)
    `(:fn ~x)
    x))

(defn- mapmerge [f s]
  (apply merge (map f s)))

(defn extract-endpoints [m]
  (cond
    (coll? m) (mapmerge extract-endpoints m)
    (component-macro? m) (-> m eval :endpoints)))

(def parsers
  {:int #(list 'Integer/parseInt %)
   :lower #(list 'some-> % '.trim '.toLowerCase)
   :trim #(list 'some-> % '.trim)
   :string #(list 'or % "")
   :boolean #(list 'contains? #{"true" "on"} %)})

(defn sym->f [sym]
  (or
    (some (fn [[k f]]
            (when (-> sym meta k)
              f))
          parsers)
    identity))

(defn- make-f [args expanded]
  (case (count args)
    0 (throw (Exception. "zero args not supported"))
    1 `(fn ~args ~expanded)
    `(fn this#
       (~(subvec args 0 1)
         (let [~'params (-> ~(args 0) :params form/trim-keys)]
           (this#
             ~(args 0)
             ~@(for [arg (rest args)]
                 ((sym->f arg) `(~(keyword arg) ~'params))))))
       (~args ~expanded))))

(def ^:dynamic *stack* [])

(defn set-id [req]
  (string/join
    "_"
    (if-let [target (get-in req [:headers "hx-target"])]
      (assoc *stack* 0 target)
      *stack*)))

(defn with-stack [n [req] body]
  `(binding [*stack* (conj *stack* ~(name n))]
     (let [~'id (set-id ~req)
           {:keys [~'params]} ~req
           ~'params-json (form/json-params ~'params)]
       ~@body)))

(defn map-indexed-stack [f s]
  (doall
    (map-indexed #(binding [*stack* (conj *stack* %1)] (f %1 %2)) s)))

(defmacro defcomponent [name args & body]
  (let [expanded (with-stack name args (walk/postwalk expand-components body))
        f (gensym)]
    `(def ~name
       (let [~f ~(make-f args expanded)]
         {:fn ~f
          :endpoints ~(extract-endpoints body)}))))

(defmacro defendpoint [name args & body]
  (let [expanded (with-stack name args (walk/postwalk expand-components body))
        f (gensym)]
    `(def ~name
       (let [~f ~(make-f args expanded)]
         {:fn ~f
          :endpoints ~(assoc (extract-endpoints body) (keyword name) f)}))))

(defn extract-endpoints-all [f]
  (for [[k f] (extract-endpoints f)]
    [(str "/" (name k)) `(fn [x#] (-> x# ~f render/snippet-response))]))

(defmacro make-routes [root f]
  `[[~root {:get ~(walk/postwalk expand-components f)}]
    ~@(extract-endpoints-all f)])

(defmacro with-req [req & body]
  `(let [{:keys [~'request-method]} ~req
         ~'get? (= :get ~'request-method)
         ~'post? (= :post ~'request-method)
         ~'put? (= :put ~'request-method)
         ~'patch? (= :patch ~'request-method)
         ~'delete? (= :delete ~'request-method)]
     ~@body))
