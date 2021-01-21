(ns ctmx.render
  (:require
    [ctmx.response :as response]
    [hiccup.core :as hiccup]
    [hiccup.page :as page]))

(defn snippet-response [body]
  (cond
    (not body) response/no-content
    (map? body) body
    :else (-> body hiccup/html response/html-response)))

(defn html5 [& content]
  (page/html5 content))
