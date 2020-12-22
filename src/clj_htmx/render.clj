(ns clj-htmx.render
  (:require
    [clj-htmx.response :as response]
    [hiccup.core :as hiccup]))

(defn snippet-response [body]
  (cond
    (not body) response/no-content
    (map? body) body
    :else (-> body hiccup/html response/html-response)))
