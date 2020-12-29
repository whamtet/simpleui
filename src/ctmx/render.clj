(ns ctmx.render
  (:require
    [ctmx.response :as response]
    [hiccup.core :as hiccup]))

(defn snippet-response [body]
  (cond
    (not body) response/no-content
    (map? body) body
    :else (-> body hiccup/html response/html-response)))
