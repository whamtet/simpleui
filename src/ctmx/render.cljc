(ns ctmx.render
  (:require
    #?(:clj [clojure.data.json :as json])
    [clojure.string :as string]
    [clojure.walk :as walk]
    [ctmx.config :as config]
    [ctmx.render.command :as command]
    [ctmx.response :as response]
    #?(:clj [hiccup.core :as hiccup]
       :cljs [hiccups.runtime :as hiccupsrt]))
  #?(:cljs (:require-macros [hiccups.core :as hiccup])))

(defn fmt-style [style]
  (string/join "; "
               (for [[k v] style :when v]
                 (str (name k) ": " v))))

(def fmt-json
  #?(:clj json/write-str
     :cljs #(-> % clj->js js/JSON.stringify)))

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

(def html #(-> % walk-attrs hiccup/html)) ;; can call directly if needed

(defn snippet-response [body]
  (cond
    (not body) response/no-content
    (map? body) body
    :else (-> body html response/html-response)))
