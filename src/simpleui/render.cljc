(ns simpleui.render
  (:require
    #?(:clj [clojure.data.json :as json])
    [clojure.string :as string]
    [clojure.walk :as walk]
    [simpleui.config :as config]
    [simpleui.render.command :as command]
    [simpleui.render.oob :as oob]
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

(defn html [s]
  #?(:cljs (-> s walk-attrs hiccup/html)
     :clj (if config/render-safe?
             (-> s walk-attrs hiccup2/html str)
             (-> s walk-attrs hiccup/html))))

(defn html-safe [s]
  (cond-> s
          (and config/render-oob? (list? s)) oob/assoc-oob
          (coll? s) html))

(defn snippet-response [body]
  (cond
    (nil? body) response/no-content
    (map? body) (update body :body html-safe)
    (and config/render-oob? (list? body)) (-> body oob/assoc-oob html response/html-response)
    :else (-> body html response/html-response)))
