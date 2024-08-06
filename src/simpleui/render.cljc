(ns simpleui.render
  (:require
    #?(:clj [clojure.data.json :as json])
    [clojure.string :as string]
    [clojure.walk :as walk]
    [simpleui.config :as config]
    [simpleui.render.command :as command]
    [simpleui.render.oob :as oob]
    [simpleui.render.prefix :as prefix]
    [simpleui.render.si-set :as si-set]
    [simpleui.response :as response]
    #?(:clj [hiccup2.core :as hiccup2]
       :cljs [hiccups.runtime :as hiccupsrt])
    #?(:clj [hiccup.core :as hiccup]))
  #?(:cljs (:require-macros [hiccups.core :as hiccup])))

(defn fmt-style [style]
  (string/join "; "
               (for [[k v] style :when v]
                 (str (name k) ": " v))))
(defn write-key-fn
  "Modified version of clojure.data.json/default-write-key-fn that serializes namespaced keys with their namespace. That is :foo/bar -> 'foo/bar'"
  [x]
  #?(:clj
     (cond
       (keyword? x)
       (subs (str x) 1)
       (instance? clojure.lang.Named x)
       (name x)
       (nil? x)
       (throw (Exception. "JSON object properties may not be nil"))
       :else (str x))
     :cljs (subs (str x) 1)))

(def fmt-json
  #?(:clj #(json/write-str % :key-fn write-key-fn)
     :cljs #(-> % (clj->js :keyword-fn write-key-fn) js/JSON.stringify)))

(defn walk-attr [{:keys [_ style hx-vals hx-headers class] :as s}]
  (as-> s s
        (if (and config/render-hs? (vector? _))
          (->> _ (filter identity) (map name) (string/join " ") (assoc s :_))
          s)
        (if (and config/render-class? (vector? class))
          (->> class (filter identity) (map name) (string/join " ") (assoc s :class))
          s)
        (if (and config/render-style? (map? style))
          (->> style fmt-style (assoc s :style))
          s)
        (if (and config/render-vals? (map? hx-vals))
          (->> hx-vals fmt-json (assoc s :hx-vals))
          s)
        (if (and config/render-headers? (map? hx-headers))
          (->> hx-headers fmt-json (assoc s :hx-headers))
          s)))
(defn- render-commands [m]
  (if config/render-commands?
    (command/assoc-commands m)
    m))
(defn- render-si-set [m]
  (if config/render-si-set?
    (si-set/promote-set m)
    m))
(defn- render-si-set-form [m]
  (if config/render-si-set?
    (si-set/promote-set-form m)
    m))
(defn- prefix-verbs [prefix m]
  (if (empty? prefix)
    m
    (prefix/prefix-verbs prefix m)))

(defn walk-attrs
  ([m] (walk-attrs "" m))
  ([prefix m]
   (walk/prewalk 
    #(cond 
       (vector? %) (render-si-set-form %)
       (map? %) (->> % render-commands render-si-set (prefix-verbs prefix) walk-attr) 
       :else %)
    m)))

(defn html 
  ([s] (html "" s))
  ([prefix s]
    #?(:cljs (->> s (walk-attrs prefix) hiccup/html)
       :clj (if config/render-safe?
               (->> s (walk-attrs prefix) hiccup2/html str)
               (->> s (walk-attrs prefix) hiccup/html)))))

(defn- render-body [prefix req s]
  (cond->> s
    (and config/render-oob? 
         (not= (get-in req [:headers "skip-oob"]) "true")
         (seq? s)) oob/assoc-oob
    config/render-si-set? (si-set/concat-set-clear req)
    (coll? s) (html prefix)
    ;; else just let it pass through (body might be string, etc)
    ))

(defn- render-map [prefix req m]
  (if (:body m)
    (-> {:status 200
         :headers {"Content-Type" "text/html"}}
        (merge m)
        (update :body #(render-body prefix req %)))
    ;; assume m is just a session
    (assoc response/no-content :session m)))

(defn snippet-response
  ([body] (snippet-response "" {} body))
  ([prefix body] (snippet-response prefix {} body))
  ([prefix req body]
   (cond
     (nil? body) response/no-content
     (map? body) (render-map prefix req body)
     :else
     (cond->> body
       (and config/render-oob? (not= (get-in req [:headers "skip-oob"]) "true") (seq? body)) oob/assoc-oob
       config/render-si-set? (si-set/concat-set-clear req)
       true (html prefix)
       true response/html-response))))
