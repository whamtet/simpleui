(ns ctmx.render
  (:require
    #?(:clj [clojure.data.json :as json])
    [clojure.string :as string]
    [clojure.walk :as walk]
    [ctmx.config :as config]
    [ctmx.render.command :as command]
    [ctmx.render.oob :as oob]
    [ctmx.response :as response]
    #?(:clj [hiccup2.core :as hiccup]
       :cljs [hiccups.runtime :as hiccupsrt]))
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

(defn walk-attr [{:keys [_ style hx-vals class] :as s}]
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
          s)))
(defn render-commands [m]
  (if config/render-commands?
    (command/assoc-commands m)
    m))

(defn walk-attrs [m]
  (walk/postwalk #(if (map? %) (-> % render-commands walk-attr) %) m))

(def html #(-> % walk-attrs hiccup/html str)) ;; can call directly if needed

(defn snippet-response [body]
  (cond
    (not body) response/no-content
    (map? body) body
    (and config/render-oob? (list? body)) (-> body oob/assoc-oob html response/html-response)
    :else (-> body html response/html-response)))
