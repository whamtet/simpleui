(ns ctmx.render
  (:require
    [clojure.walk :as walk]
    [ctmx.handler :as handler]
    [ctmx.response :as response]
    [hiccups.runtime :as hiccupsrt])
  (:require-macros [hiccups.core :as hiccups]))

(defn snippet-response [body]
  (cond
    (not body) response/no-content
    (map? body) body
    :else (-> body expand-hx-vals hiccups/html response/html-response)))

(defn wrap-response [f]
  #(->> %
        js->clj
        walk/keywordize-keys
        handler/coerce-static
        f
        hiccups/html))
