(ns ctmx.render
  (:require
    [clojure.walk :as walk]
    [ctmx.response :as response]
    [hiccups.runtime :as hiccupsrt])
  (:require-macros [hiccups.core :as hiccups]))

(defn write-str [s]
  (-> s clj->js js/JSON.stringify))

(defn expand-hx-val [v]
  (if (and
        (vector? v)
        (-> v first (= :hx-vals)))
    (update v 1 write-str)
    v))
(defn expand-hx-vals [body]
  (walk/postwalk expand-hx-val body))

(defn snippet-response [body]
  (cond
    (not body) response/no-content
    (map? body) body
    :else (-> body expand-hx-vals hiccups/html response/html-response)))

(defn html5 [& body]
  (hiccups/html5
    body
    [:script {:src "https://unpkg.com/htmx.org@1.1.0"}]
    [:script {:src "https://unpkg.com/htmx.org@1.1.0/dist/ext/json-enc.js"}]))
