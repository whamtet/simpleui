(ns clj-htmx.core
  (:refer-clojure :exclude [map-indexed])
  (:require
    [clj-htmx.form :as form]
    [clj-htmx.render :as render]
    [clojure.string :as string]))

(def parse-int #(if (string? %) (Integer/parseInt %) %))
(def parse-lower #(some-> % .trim .toLowerCase))
(def parse-trim #(some-> % .trim))
(def parse-string #(or % ""))
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
   :lower `parse-lower
   :trim `parse-trim
   :string `parse-string
   :boolean `parse-boolean
   :boolean-true `parse-boolean-true})

(defn sym->f [sym]
  (some (fn [[k f]]
          (when (-> sym meta k)
            f))
        parsers))

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
                 `(~(keyword arg) ~'params)))))
       (~args
         (let [~@(for [sym (rest args)
                       :let [f (sym->f sym)]
                       :when f
                       x [sym `(~f ~sym)]]
                   x)]
           ~expanded)))))

(def ^:dynamic *stack* [])

(defn concat-stack [concat]
  (reduce
    (fn [stack x]
      (if (= ".." x)
        (pop stack)
        (conj stack x)))
    *stack*
    concat))

(defn conj-stack [n req]
  (let [target (get-in req [:headers "hx-target"])]
    (if (and (empty? *stack*) target)
      (-> target (.split "_") vec)
      (conj *stack* n))))

(defn path [& args]
  (->> args concat-stack (string/join "_")))
(defn path-hash [& args]
  (str "#" (->> args concat-stack (string/join "_"))))

(defn with-stack [n [req] body]
  `(binding [*stack* (conj-stack ~(name n) ~req)]
     (let [~'id (path)
           ~'path path
           ~'hash path-hash
           {:keys [~'params]} ~req
           ~'value (fn [x#] (-> x# ~'path keyword ~'params))]
       ~@body)))

(defn map-indexed [f s]
  (doall
    (clojure.core/map-indexed #(binding [*stack* (conj *stack* %1)] (f %1 %2)) s)))

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
     ~(make-f args (with-stack name args body))))

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
