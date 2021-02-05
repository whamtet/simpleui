(ns ctmx.render
  (:require
    [ctmx.response :as response]
    [hiccups.runtime :as hiccupsrt])
  (:require-macros [hiccups.core :as hiccups]))

(defn snippet-response [body]
  (cond
    (not body) response/no-content
    (map? body) body
    :else (-> body hiccups/html response/html-response)))

(defn html5 [& body]
  (hiccups/html5
    body
    [:script {:src "https://unpkg.com/htmx.org@1.1.0"}]
    [:script {:src "https://unpkg.com/htmx.org@1.1.0/dist/ext/json-enc.js"}]))
